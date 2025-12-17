package co.jp.enon.tms.timemaintenance.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//import co.jp.enon.tms.timemaintenance.dto.CurrentUserBreakInfoDto;
import co.jp.enon.tms.timemaintenance.dto.LatestUserSessionInfoDto;
import co.jp.enon.tms.timemaintenance.dto.UnfinishedUserSessionInfoDto;
import co.jp.enon.tms.timemaintenance.dto.UserWorkReportDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakChangeDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakInsertDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakUpdateDto;
import co.jp.enon.tms.timemaintenance.dto.WorkReportInsertDto;
import co.jp.enon.tms.timemaintenance.dto.WorkReportUpdateDto;
import co.jp.enon.tms.timemaintenance.dto.WorkSessionUpdateDto;
import co.jp.enon.tms.timemaintenance.dto.UserSessionsTodayDto;
import co.jp.enon.tms.timemaintenance.service.TimeService;


@RestController
public class TimeMaintenanceController {
	
	final static Logger logger = LoggerFactory.getLogger(TimeMaintenanceController.class);
	
	@Autowired
	TimeService timeService;
	
	// Start button click
	@PostMapping("/start-work-session")
    public WorkReportInsertDto startSession(@RequestBody WorkReportInsertDto workReportInsertDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.saveWorkReportWithSession(workReportInsertDto);
		
		return workReportInsertDto;     
    }
	
	// end button click
	@PostMapping("/end-work-session")
    public WorkReportUpdateDto endSession(@RequestBody WorkReportUpdateDto workReportUpdateDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.updateWorkReportWithSession(workReportUpdateDto);
		
		return workReportUpdateDto;     
    }
	
	// Start button click
	@PostMapping("/start-break")
    public WorkBreakInsertDto startBreak(@RequestBody WorkBreakInsertDto workBreakInsertDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.saveWorkBreakStart(workBreakInsertDto);
		
		return workBreakInsertDto;     
    }
	// Start button click
	@PostMapping("/end-break")
    public WorkBreakUpdateDto endBreak(@RequestBody WorkBreakUpdateDto workBreakUpdateDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.updateWorkBreak(workBreakUpdateDto);
		
		return workBreakUpdateDto;     
    }
	
	// Start button click
	@PostMapping("/change-break-info")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
    public WorkBreakChangeDto changeBreakInfo(@RequestBody WorkBreakChangeDto workBreakChangeDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.changeWorkBreak(workBreakChangeDto);
		
		return workBreakChangeDto;     
    }
	
	// end button click
	@PostMapping("/change-session-info")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
    public WorkSessionUpdateDto changeSessionInfo(@RequestBody WorkSessionUpdateDto workSessionUpdateDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		//timeService.updateWorkSessionInfo(workSessionUpdateDto);
		
		return workSessionUpdateDto;     
    }
	
	// Report
	@PostMapping("/get-report")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
//	@PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserWorkReportDto getReport(@RequestBody UserWorkReportDto userWorkReportDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.getUserWorkReport(userWorkReportDto);
		
		return userWorkReportDto;     
    }
	
	// get all ids if the sessions of today are not closed
	@PostMapping("/get-unfinished-session-info-today")
    public UnfinishedUserSessionInfoDto getUnfinishedUserSessionInfo(@RequestBody UnfinishedUserSessionInfoDto unfinishedUserSessionInfoDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.getUnfinishedUserSessionInfo(unfinishedUserSessionInfoDto);
		
		return unfinishedUserSessionInfoDto;     
    }
	
	// get last session info of today with break info 
	@PostMapping("/get-latest-session")
    public LatestUserSessionInfoDto getLatestSession(@RequestBody LatestUserSessionInfoDto latestUserSessionInfoDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.getLatestUserSessionInfo(latestUserSessionInfoDto);
		
		return latestUserSessionInfoDto;     
    }
	
//	// get current session info
//	@PostMapping("/get-current-break")
//    public CurrentUserBreakInfoDto getCurrentBreak(@RequestBody CurrentUserBreakInfoDto currentUserBreakInfoDto) throws Exception {
//		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
//		
//		timeService.getLatestUserBreakInfo(currentUserBreakInfoDto);
//		
//		return currentUserBreakInfoDto;     
//    }
	
	// get current session info
	@PostMapping("/get-today-sessions")
    public UserSessionsTodayDto getTodaySession(@RequestBody UserSessionsTodayDto userSessionsTodayDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.getTodaySessionInfo(userSessionsTodayDto);
		
		return userSessionsTodayDto;     
    }
	

}
