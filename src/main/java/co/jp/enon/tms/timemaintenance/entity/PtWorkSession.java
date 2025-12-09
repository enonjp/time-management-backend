package co.jp.enon.tms.timemaintenance.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class PtWorkSession {
	
	private Integer workSessionId;     // work_session_id INT AUTO_INCREMENT PRIMARY KEY
    private Integer workReportId;      // work_report_id INT NOT NULL
    private LocalTime startTime;       // start_time TIME NOT NULL
    private LocalTime endTime;         // end_time TIME NULL
    private Integer workTime = 0;      // work_time INT DEFAULT 0
    private Integer breakTime = 0;  
    private String status; //'WORKING' | 'BREAK' | 'FINISHED' | 'NOT_STARTED'

	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;
	
}
