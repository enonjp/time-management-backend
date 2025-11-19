package co.jp.enon.tms.timemaintenance.entity;

import java.time.LocalTime;

import lombok.Data;

@Data
public class PvUserWorkSession {
	
	private Integer userId;
	private Integer workSessionId;     // work_session_id INT AUTO_INCREMENT PRIMARY KEY
    private LocalTime sessionStart;       // start_time TIME NOT NULL
    private LocalTime sessionEnd;         // end_time TIME NULL
    private Integer sessionWorkTime = 0;      // work_time INT DEFAULT 0
    private Integer sessionBreakTime = 0;
	
}
