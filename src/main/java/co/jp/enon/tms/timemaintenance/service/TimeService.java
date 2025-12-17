package co.jp.enon.tms.timemaintenance.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.jp.enon.tms.common.BaseService;
import co.jp.enon.tms.timemaintenance.dao.PtWorkBreakDao;
import co.jp.enon.tms.timemaintenance.dao.PtWorkReportDao;
import co.jp.enon.tms.timemaintenance.dao.PtWorkSessionDao;
import co.jp.enon.tms.timemaintenance.dao.PvUserWorkReportDao;
import co.jp.enon.tms.timemaintenance.dao.PvUserWorkSessionBreakDao;
import co.jp.enon.tms.timemaintenance.dao.PvUserWorkSessionDao;
import co.jp.enon.tms.timemaintenance.dto.CurrentUserBreakInfoDto;
import co.jp.enon.tms.timemaintenance.dto.LatestUserSessionInfoDto;
import co.jp.enon.tms.timemaintenance.dto.UserSessionsTodayDto;
import co.jp.enon.tms.timemaintenance.dto.UserWorkReportDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakChangeDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakInsertDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakUpdateDto;
import co.jp.enon.tms.timemaintenance.dto.WorkReportInsertDto;
import co.jp.enon.tms.timemaintenance.dto.WorkReportUpdateDto;
import co.jp.enon.tms.timemaintenance.entity.PtWorkBreak;
import co.jp.enon.tms.timemaintenance.entity.PtWorkReport;
import co.jp.enon.tms.timemaintenance.entity.PtWorkSession;
import co.jp.enon.tms.timemaintenance.entity.PvUserWorkReport;
import co.jp.enon.tms.timemaintenance.entity.PvUserWorkSession;
import co.jp.enon.tms.timemaintenance.entity.PvUserWorkSessionBreak;

@Service
public class TimeService extends BaseService {
	final static Logger logger = LoggerFactory.getLogger(TimeService.class);

	@Autowired
    private PtWorkReportDao ptWorkReportDao;
	
	@Autowired
	private PtWorkSessionDao ptWorkSessionDao;
	
	@Autowired
	private PtWorkBreakDao ptWorkBreakDao;
	
	@Autowired
	PvUserWorkReportDao pvUserWorkReportDao;
	
	@Autowired
	PvUserWorkSessionDao pvUserWorkSessionDao;
	
	@Autowired
	PvUserWorkSessionBreakDao pvUserWorkSessionBreakDao;
	
	public void saveWorkReportWithSession(WorkReportInsertDto workReportInsertDto) throws Exception {
	    var reqHd = workReportInsertDto.getReqHd();
	    LocalDate workDate = LocalDate.parse(reqHd.getWorkDate());
	    int workReportId = 0;
	    try { 	
	    	//if pt-work_report already has a record for given user_id and date, then get the work_reprt_id
	    	PtWorkReport ptWorkReportOld = ptWorkReportDao.getWorkReportByUserIdAndDate(reqHd.getUserId(), workDate);
	    	if (ptWorkReportOld == null) {
	    		 PtWorkReport ptWorkReport = new PtWorkReport();
	    		 ptWorkReport.setUserId(reqHd.getUserId());
	    		 ptWorkReport.setWorkDate(workDate);
		    	//Insert into pt_work_report 
		    	workReportId = ptWorkReportDao.save(ptWorkReport);
	    	} else {
	    		workReportId = ptWorkReportOld.getWorkReportId();
	    	}
	    	
	    	// Insert into pt_work_session
	        PtWorkSession ptWorkSession = new PtWorkSession();
	        ptWorkSession.setWorkReportId(workReportId);
	        ptWorkSession.setStartTime(reqHd.getStartTime());
	        ptWorkSession.setEndTime(null); // if starting session only
	        ptWorkSession.setWorkTime(0);
	        ptWorkSession.setBreakTime(0);
	        ptWorkSession.setStatus("WORKING");

	        int workSessionId = ptWorkSessionDao.save(ptWorkSession);
	        
	        var responseHd = workReportInsertDto.getResponseHd();
	        responseHd.setWorkReportId(workReportId);
	        responseHd.setWorkSessionId(workSessionId);
	        responseHd.setStatus("WORKING");
	        
	        workReportInsertDto.setResultCode("000");
	    } catch (Exception ex) {
	    	
	    	workReportInsertDto.setResultCode("002");
	    	workReportInsertDto.setResultMessage("（Method：saveWorkReportWithSession, Table Name：pt_work_report / pt_work_session ,Exception：" + ex.getMessage() + "）");
	    }
	    return;
	}
	
	public void updateWorkReportWithSession(WorkReportUpdateDto workReportUpdateDto) throws Exception {
		 var reqHd = workReportUpdateDto.getReqHd();	 
		 try { 	 
			 // get all break times pt_work_break using session id where endtime is null
			 // calculate break time using start and end times
			 // update the end time and breakTime
			 PtWorkBreak ptWorkBreakOld = ptWorkBreakDao.getActiveBreakTimeUsingSessionId(reqHd.getWorkSessionId());
			 int totalBreakInMinutes = 0; 
			 if (ptWorkBreakOld != null) {
				if (!ptWorkBreakOld.getBreakStart().equals(LocalTime.MIDNIGHT)) {
					totalBreakInMinutes = calculateBreakMinutes(ptWorkBreakOld.getBreakStart(), reqHd.getEndTime());
				}
				PtWorkBreak ptWorkBreak = new PtWorkBreak();
			        
		        ptWorkBreak.setWorkBreakId(ptWorkBreakOld.getWorkBreakId());
		        ptWorkBreak.setWorkSessionId(ptWorkBreakOld.getWorkSessionId()); 
		        ptWorkBreak.setBreakEnd(reqHd.getEndTime());
		        ptWorkBreak.setBreakTime(totalBreakInMinutes);
		        
		        ptWorkBreakDao.update(ptWorkBreak);
			 }
			 // get sum of all the break times from the ptworkBreak table using workSession id
			 int breakTimeSum = ptWorkBreakDao.getTotalBreakTime(reqHd.getWorkSessionId());
			 // get start time from work session table
			 LocalTime sessionStartTime = ptWorkSessionDao.getStartTime(reqHd.getWorkSessionId(), reqHd.getWorkReportId());
			 
			// get the sum of all the work times and update the session table
			 int sessionWorkTime = calculateBreakMinutes(sessionStartTime, reqHd.getEndTime());
			// Update into pt_work_session
			 
	         PtWorkSession ptWorkSession = new PtWorkSession();
	         ptWorkSession.setWorkReportId(reqHd.getWorkReportId());
	         ptWorkSession.setWorkSessionId(reqHd.getWorkSessionId());
	         ptWorkSession.setEndTime(reqHd.getEndTime()); //  session end
	         ptWorkSession.setWorkTime(sessionWorkTime); // in minutes
	         ptWorkSession.setBreakTime(breakTimeSum);
	         ptWorkSession.setStatus("FINISHED");
	         
	         ptWorkSessionDao.updateWorkSession(ptWorkSession);
			 
			// get all the session for given work_report_id and add up and update the work_report table
	         int workTime = ptWorkSessionDao.getTotalWorkTime(reqHd.getWorkReportId());
	         int totalBreakTime = ptWorkSessionDao.getTotalBreakTime(reqHd.getWorkReportId());
	         int totalWorkTime = workTime - totalBreakTime;
	         if (totalWorkTime <= 0) {
	        	 totalWorkTime = 0;
	         } 	 
		    // get workReportId from pt_work_report 
			LocalDate workDate = LocalDate.parse(reqHd.getWorkDate());
			PtWorkReport ptWorkReport = new PtWorkReport();
			
			ptWorkReport.setUserId(reqHd.getUserId());
		    ptWorkReport.setWorkDate(workDate);
		    ptWorkReport.setWorkReportId(reqHd.getWorkReportId());
		    ptWorkReport.setTotalWorkTime(totalWorkTime);
		    ptWorkReport.setTotalBreakTime(totalBreakTime);
		  
		    ptWorkReportDao.updateWorkReport(ptWorkReport);
	        workReportUpdateDto.setResultCode("000");
		 } catch (Exception ex) {
			 workReportUpdateDto.setResultCode("002");
			 workReportUpdateDto.setResultMessage("（Method：updateWorkReportWithSession, Table Name：pt_work_report / pt_work_session ,Exception：" + ex.getMessage() + "）");
	    }
		return;
	}
	
	public void saveWorkBreakStart(WorkBreakInsertDto workBreakInsertDto) throws Exception {
		var reqHd = workBreakInsertDto.getReqHd();	
		try { 		    
			// save date into pt_work_break
	        PtWorkBreak ptWorkBreak = new PtWorkBreak();
	        
	        ptWorkBreak.setWorkSessionId(reqHd.getWorkSessionId()); 
	        ptWorkBreak.setBreakStart(reqHd.getBreakStart());
	        
	        int workBreakId = ptWorkBreakDao.save(ptWorkBreak);
	        
	        PtWorkSession ptWorkSession = new PtWorkSession();
	        ptWorkSession.setWorkSessionId(reqHd.getWorkSessionId());
	        ptWorkSession.setStatus("BREAK");
	        
	        ptWorkSessionDao.updateWorkSessionStatus(ptWorkSession);
	        
	        var responseHd = workBreakInsertDto.getResponseHd();	
	        responseHd.setWorkBreakId(workBreakId);
	        workBreakInsertDto.setResultCode("000");
		 } catch (Exception ex) {
			 workBreakInsertDto.setResultCode("002");
			 workBreakInsertDto.setResultMessage("（Method：saveWorkBreakStart, Table Name：pt_work_break ,Exception：" + ex.getMessage() + "）");
	    }
		return;		
	}
	
	public void updateWorkBreak(WorkBreakUpdateDto workBreakUpdateDto) throws Exception {
		var reqHd = workBreakUpdateDto.getReqHd();	
		try { 
			// get break start from  pt_work_break
			LocalTime breakStart = ptWorkBreakDao.getBreakStartTime(reqHd.getWorkBreakId(), reqHd.getWorkSessionId());
			
			int totalBreakInMinutes = calculateBreakMinutes(breakStart, reqHd.getBreakEnd());
			
			// update break end and break time in pt_work_break
	        PtWorkBreak ptWorkBreak = new PtWorkBreak();
	        
	        ptWorkBreak.setWorkBreakId(reqHd.getWorkBreakId());
	        ptWorkBreak.setWorkSessionId(reqHd.getWorkSessionId()); 
	        ptWorkBreak.setBreakEnd(reqHd.getBreakEnd());
	        ptWorkBreak.setBreakTime(totalBreakInMinutes);
	        
	        ptWorkBreakDao.update(ptWorkBreak);
	        
	        PtWorkSession ptWorkSession = new PtWorkSession();
	        ptWorkSession.setWorkSessionId(reqHd.getWorkSessionId());
	        ptWorkSession.setStatus("WORKING");
	        
	        ptWorkSessionDao.updateWorkSessionStatus(ptWorkSession);
	        
	        workBreakUpdateDto.setResultCode("000");
	       
		 } catch (Exception ex) {
			 ex.printStackTrace();
			 workBreakUpdateDto.setResultCode("002");
			 workBreakUpdateDto.setResultMessage("（Method：updateWorkBreak, Table Name：pt_work_break ,Exception：" + ex.getMessage() + "）");
	    }
		return;		
	}
	
	public void changeWorkBreak(WorkBreakChangeDto workBreakChangeDto) throws Exception {
		var reqHd = workBreakChangeDto.getReqHd();	
		try { 
			// get data from pt_work_break table before changing
			PtWorkBreak ptWorkBreakOld = ptWorkBreakDao.getBreakInfo(reqHd.getWorkSessionId(), reqHd.getWorkBreakId());
			LocalTime breakStart = ptWorkBreakOld.getBreakStart();
			if (reqHd.getBreakStart() != null ) {
				breakStart = reqHd.getBreakStart();
			}
			LocalTime breakEnd = ptWorkBreakOld.getBreakEnd();
			if (reqHd.getBreakEnd() != null) {
				breakEnd = reqHd.getBreakEnd();
			}	
			int totalBreakInMinutes = calculateBreakMinutes(breakStart, breakEnd);
			boolean updateRequired = false;
			if (totalBreakInMinutes != ptWorkBreakOld.getBreakTime()) {
				updateRequired = true;
			}
		
			// change break start and break end and total break time in pt_work_break
	        PtWorkBreak ptWorkBreak = new PtWorkBreak();
	        
	        ptWorkBreak.setWorkBreakId(reqHd.getWorkBreakId());
	        ptWorkBreak.setWorkSessionId(reqHd.getWorkSessionId());
	        ptWorkBreak.setBreakStart(breakStart);
	        ptWorkBreak.setBreakEnd(breakEnd);
	        ptWorkBreak.setBreakTime(totalBreakInMinutes);
	        
	        ptWorkBreakDao.update(ptWorkBreak);
	        
	        if (updateRequired == true) {
	        	 // get sum of all the break times from the ptworkBreak table using workSession id
				 int breakTimeSum = ptWorkBreakDao.getTotalBreakTime(reqHd.getWorkSessionId());
				 
				 PtWorkSession ptWorkSession = new PtWorkSession();
				 ptWorkSession.setWorkSessionId(reqHd.getWorkSessionId());
				 ptWorkSession.setBreakTime(breakTimeSum);
				 
	        	// update pt_work_session table with the new workTime and break time
	        	ptWorkSessionDao.updateWorkSessionBreakTime(ptWorkSession);
	        	
	        	// update pt_work_report table with total work hours and break times 
	        	PtWorkSession ptWorkSessionUpdated = ptWorkSessionDao.selectBySessionId(reqHd.getWorkSessionId());
	        	
	        	// get all the session for given work_report_id and add up and update the work_report table
		         int workTime = ptWorkSessionDao.getTotalWorkTime(ptWorkSessionUpdated.getWorkReportId());
		         int totalBreakTime = ptWorkSessionDao.getTotalBreakTime(ptWorkSessionUpdated.getWorkReportId());
		         int totalWorkTime = workTime - totalBreakTime;
		         if (totalWorkTime <= 0) {
		        	 totalWorkTime = 0;
		         } 	 
			    // update pt_work_report 
		
				PtWorkReport ptWorkReport = new PtWorkReport();
				
				ptWorkReport.setUserId(reqHd.getUserId());
			    ptWorkReport.setWorkReportId(ptWorkSessionUpdated.getWorkReportId());
			    ptWorkReport.setTotalWorkTime(totalWorkTime);
			    ptWorkReport.setTotalBreakTime(totalBreakTime);
			  
			    ptWorkReportDao.updateWorkReport(ptWorkReport);  	
	        	
	        }       
	        workBreakChangeDto.setResultCode("000");		
		} catch (Exception ex) {
			 ex.printStackTrace();
			 workBreakChangeDto.setResultCode("002");
			 workBreakChangeDto.setResultMessage("（Method：changeWorkBreak, Table Name：pt_work_break ,Exception：" + ex.getMessage() + "）");
	    }
		return;	
	}
	
	public void getUserWorkReport(UserWorkReportDto userWorkReportDto) throws Exception {
		var reqHd = userWorkReportDto.getReqHd();
		try {
			List<PvUserWorkReport> listPvUserWorkReport = pvUserWorkReportDao.getUserWorkReport(reqHd.getUserId(), reqHd.getFirstName(), reqHd.getLastName(), reqHd.getStartDate(), reqHd.getEndDate());
			// Report data not found 
			if (listPvUserWorkReport == null || listPvUserWorkReport.isEmpty()) {
				userWorkReportDto.setResultMessage("No report data found");
			    userWorkReportDto.setResultCode("001");
			    userWorkReportDto.setResDt(Collections.emptyList());
			    userWorkReportDto.setResDtTitle(null);
			    return;  // <-- MUST exit here
		    }
			
			// All rows share same user info
		    PvUserWorkReport first = listPvUserWorkReport.get(0);
		    UserWorkReportDto.ResponseHd resHd = userWorkReportDto.getResHd();
		    resHd.setUserId(first.getUserId());
		    resHd.setFirstName(first.getFirstName());
		    resHd.setLastName(first.getLastName());
		    resHd.setWorkInfos(new ArrayList<>());
		    
		    List<UserWorkReportDto.ResponseDt> resDtList = new ArrayList<>();
		    UserWorkReportDto.ResponseDt currentSession = null;
		    Integer currentSessionId = null;
		    LocalDate currentDate = null;
		    
		    for (PvUserWorkReport row : listPvUserWorkReport) {
		        // If new date, update daily totals
		        if (currentDate == null || !currentDate.equals(row.getWorkDate())) {
		            currentDate = row.getWorkDate();
		            UserWorkReportDto.ResponseHd.WorkInfo workInfo = new UserWorkReportDto.ResponseHd.WorkInfo();
		            workInfo.setWorkDate(currentDate);
		            workInfo.setDailyWorkTime(row.getDailyWorkTime());
		            workInfo.setDailyBreakTime(row.getDailyBreakTime());
		            resHd.getWorkInfos().add(workInfo);
		        }
		        // If new session, create new ResponseDt
		        if (currentSessionId == null || !currentSessionId.equals(row.getWorkSessionId())) {
		            currentSession = new UserWorkReportDto.ResponseDt();
		            currentSessionId = row.getWorkSessionId();

		            currentSession.setWorkSessionId(row.getWorkSessionId());
		            currentSession.setSessionStart(row.getSessionStart());
		            currentSession.setSessionEnd(row.getSessionEnd());
		            currentSession.setSessionWorkTime(row.getSessionWorkTime());
		            currentSession.setSessionBreakTime(row.getSessionBreakTime());
		            currentSession.setStatus(row.getStatus());
		            currentSession.setBreaks(new ArrayList<>());

		            resDtList.add(currentSession);
		        }
		        // Add break if present
		        if (row.getWorkBreakId() != null) {
		            UserWorkReportDto.ResponseDt.BreakInfo breakInfo = new UserWorkReportDto.ResponseDt.BreakInfo();
		            breakInfo.setWorkBreakId(row.getWorkBreakId());
		            breakInfo.setBreakStart(row.getBreakStart());
		            breakInfo.setBreakEnd(row.getBreakEnd());
		            breakInfo.setBreakDuration(row.getBreakDuration());
		            currentSession.getBreaks().add(breakInfo);
		        }
		    }
		    userWorkReportDto.setResDt(resDtList); 
		    if (resDtList.size() > 0) {
		    	UserWorkReportDto.ResponseDtTitle responseDtTitle = new UserWorkReportDto.ResponseDtTitle();
		    	userWorkReportDto.setResDtTitle(responseDtTitle);
		    }
			userWorkReportDto.setResultCode("000");
		} catch (Exception ex) {
			ex.printStackTrace();
			userWorkReportDto.setResultCode("002");
			userWorkReportDto.setResultMessage("（Method：getUserWorkReport ,Exception while fetching user report Data：" + ex.getMessage() + "）");
	    }
		return;	
	}
	
	public void getTodaySessionInfo(UserSessionsTodayDto userSessionsTodayDto) throws Exception {
		var reqHd = userSessionsTodayDto.getReqHd();
		try {
			List<PvUserWorkSession> listPvUserWorkSession = pvUserWorkSessionDao.getAllUserSessionForToday(reqHd.getUserId());
			// Session data not found 
			if (listPvUserWorkSession == null || listPvUserWorkSession.isEmpty()) {
				userSessionsTodayDto.setResultMessage("Session data is null "); // No User Session data found
				userSessionsTodayDto.setResultCode("001"); // No User session data found
				return;
		    }
			List<UserSessionsTodayDto.ResponseDt> resDtList = new ArrayList<>();
			UserSessionsTodayDto.ResponseDt currentSession = null;
			for (PvUserWorkSession row : listPvUserWorkSession) {
				currentSession = new UserSessionsTodayDto.ResponseDt();

	            currentSession.setWorkSessionId(row.getWorkSessionId());
	            currentSession.setSessionStart(row.getSessionStart());
	            currentSession.setSessionEnd(row.getSessionEnd());
	            currentSession.setSessionWorkTime(row.getSessionWorkTime());
	            currentSession.setSessionBreakTime(row.getSessionBreakTime());
	            currentSession.setStatus(row.getStatus());
	            
	            resDtList.add(currentSession);
			}
			userSessionsTodayDto.setResDt(resDtList); 
			// userSessionsTodayDto.ResponseDtTitle responseDtTitle = new UserSessionsTodayDto.ResponseDtTitle();
			// userSessionsTodayDto.setResDtTitle(responseDtTitle);
			userSessionsTodayDto.setResultCode("000");
			
		} catch (Exception ex) {
			ex.printStackTrace();
			userSessionsTodayDto.setResultCode("002");
			userSessionsTodayDto.setResultMessage("（Method：getTodaySessionInfo ,Exception while fetching user sessions Data：" + ex.getMessage() + "）");
	    }
		return;	
	}
	
	public void getLatestUserSessionInfo(LatestUserSessionInfoDto latestUserSessionInfoDto) throws Exception {
		var reqHd = latestUserSessionInfoDto.getReqHd();
		if (reqHd.getUserId() != null) {
			try {
				PvUserWorkSession pvUserWorkSession = pvUserWorkSessionDao.getLatestSessionForUser(reqHd.getUserId());
				if (pvUserWorkSession == null ) {
					latestUserSessionInfoDto.setResultMessage(" No User session data found for userId: " + reqHd.getUserId());
					latestUserSessionInfoDto.setResultCode("001"); // No User session data found
					return;
			    }
				List<PtWorkBreak> listPtWorkBreak = ptWorkBreakDao.getBreakInfosForGivenSession(pvUserWorkSession.getWorkSessionId());
				
				LatestUserSessionInfoDto.ResponseHd responseHd = latestUserSessionInfoDto.getResHd();
				responseHd.setWorkSessionId(pvUserWorkSession.getWorkSessionId());
				responseHd.setSessionStart(pvUserWorkSession.getSessionStart());
				responseHd.setSessionEnd(pvUserWorkSession.getSessionEnd());
				responseHd.setSessionWorkTime(pvUserWorkSession.getSessionWorkTime());
				responseHd.setSessionBreakTime(pvUserWorkSession.getSessionBreakTime());
				responseHd.setStatus(pvUserWorkSession.getStatus());
				responseHd.setTotalWorkTimeForTheDay(pvUserWorkSession.getTotalWorkTime());
				responseHd.setTotalBreakTimeForTheDay(pvUserWorkSession.getTotalBreakTime());
				responseHd.setBreaks(new ArrayList<>());
				
				// add breakInfo to the responseHd
				if (listPtWorkBreak != null) {
					for (PtWorkBreak row : listPtWorkBreak) { 
					 	LatestUserSessionInfoDto.ResponseHd.BreakInfo breakInfo = new LatestUserSessionInfoDto.ResponseHd.BreakInfo();
			            breakInfo.setWorkBreakId(row.getWorkBreakId());
			            breakInfo.setBreakStart(row.getBreakStart());
			            breakInfo.setBreakEnd(row.getBreakEnd());
			            breakInfo.setBreakDuration(row.getBreakTime());
			            
			            responseHd.getBreaks().add(breakInfo);
					}    
		         }				
				latestUserSessionInfoDto.setResultCode("000");
			} catch (Exception ex) {
				ex.printStackTrace();
				latestUserSessionInfoDto.setResultCode("002");
				latestUserSessionInfoDto.setResultMessage("（Method：getLatestUserSessionInfo ,Exception while fetching user session Data：" + ex.getMessage() + "）");
		    }
		} else {
			latestUserSessionInfoDto.setResultCode("000");
		}
		return;	
	}
	
	public void getLatestUserBreakInfo(CurrentUserBreakInfoDto currentUserBreakInfoDto) throws Exception {
		var reqHd = currentUserBreakInfoDto.getReqHd();
		try {
			PvUserWorkSessionBreak pvUserWorkSessionBreak = pvUserWorkSessionBreakDao.getLatestBreakForUser(reqHd.getUserId());
			if (pvUserWorkSessionBreak == null ) {
				currentUserBreakInfoDto.setResultMessage(" No User break data found for userId: " + reqHd.getUserId());
				currentUserBreakInfoDto.setResultCode("001"); // No User break data found
				return;
		    }
			CurrentUserBreakInfoDto.ResponseHd responseHd = currentUserBreakInfoDto.getResHd();
			responseHd.setWorkSessionId(pvUserWorkSessionBreak.getWorkSessionId());
			responseHd.setWorkBreakId(pvUserWorkSessionBreak.getWorkBreakId());
			responseHd.setBreakStart(pvUserWorkSessionBreak.getBreakStart());
			responseHd.setBreakEnd(pvUserWorkSessionBreak.getBreakEnd());
			responseHd.setBreakTime(pvUserWorkSessionBreak.getBreakTime());
			
			currentUserBreakInfoDto.setResultCode("000");
		} catch (Exception ex) {
			ex.printStackTrace();
			currentUserBreakInfoDto.setResultCode("002");
			currentUserBreakInfoDto.setResultMessage("（Method：getLatestUserBreakInfo ,Exception while fetching user break Data：" + ex.getMessage() + "）");
	    }
		return;
	}
	
	
	private int calculateBreakMinutes(LocalTime breakStart, LocalTime breakEnd) {
	    if (breakStart == null || breakEnd == null) {
	        return 0; // or handle appropriately
	    }
	    // Duration between the two times
	    Duration duration = Duration.between(breakStart, breakEnd);
	    // Return total minutes
	    return (int) duration.toMinutes();
	}
	
}
