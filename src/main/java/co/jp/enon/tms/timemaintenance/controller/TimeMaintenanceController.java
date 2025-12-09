package co.jp.enon.tms.timemaintenance.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//import co.jp.enon.tms.timemaintenance.dto.CurrentUserBreakInfoDto;
import co.jp.enon.tms.timemaintenance.dto.CurrentUserSessionInfoDto;
import co.jp.enon.tms.timemaintenance.dto.UserWorkReportDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakInsertDto;
import co.jp.enon.tms.timemaintenance.dto.WorkBreakUpdateDto;
import co.jp.enon.tms.timemaintenance.dto.WorkReportInsertDto;
import co.jp.enon.tms.timemaintenance.dto.WorkReportUpdateDto;
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
	@PostMapping("/change-break")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
    public WorkBreakUpdateDto changeBreak(@RequestBody WorkBreakUpdateDto workBreakUpdateDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.changeWorkBreak(workBreakUpdateDto);
		
		return workBreakUpdateDto;     
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
	
	// get current session info
	@PostMapping("/get-current-session")
    public CurrentUserSessionInfoDto getCurrentSession(@RequestBody CurrentUserSessionInfoDto currentUserSessionInfoDto) throws Exception {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		timeService.getLatestUserSessionInfo(currentUserSessionInfoDto);
		
		return currentUserSessionInfoDto;     
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
