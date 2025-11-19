package co.jp.enon.tms.timemaintenance.entity;

import java.time.LocalTime;

import lombok.Data;

@Data
public class PvUserWorkSessionBreak {
	
	private Integer userId;
	private Integer workSessionId;    
	private Integer workBreakId;
    private LocalTime breakStart;       // start_time TIME NOT NULL
    private LocalTime breakEnd;         // end_time TIME NULL
    private Integer breakTime = 0;

}
