package co.jp.enon.tms.timemaintenance.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class PvUserWorkReport {
	
	private Integer userId;
	private String firstName;
	private String lastName;
	private Integer workReportId;
	private LocalDate workDate;        // work_date DATE NOT NULL
	private Integer dailyWorkTime = 0; // total_work_time INT DEFAULT 0
	private Integer dailyBreakTime = 0;
	private Integer workSessionId;     // work_session_id INT AUTO_INCREMENT PRIMARY KEY
    private LocalTime sessionStart;       // start_time TIME NOT NULL
    private LocalTime sessionEnd;         // end_time TIME NULL
    private Integer sessionWorkTime = 0;      // work_time INT DEFAULT 0
    private Integer sessionBreakTime = 0;
    private String status; //'WORKING' | 'BREAK' | 'FINISHED' | 'NOT_STARTED'
    
    private Integer workBreakId;
	private LocalTime breakStart;       // corresponds to break_start, NOT NULL
    private LocalTime breakEnd = LocalTime.of(0, 0); // corresponds to break_end, default 0:00
    private Integer breakDuration = 0;  

}
