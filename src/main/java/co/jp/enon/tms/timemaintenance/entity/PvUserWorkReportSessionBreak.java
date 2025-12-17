package co.jp.enon.tms.timemaintenance.entity;

import lombok.Data;

@Data
public class PvUserWorkReportSessionBreak {
	
	private Integer workReportId;
	private Integer workSessionId;   
    private Integer workBreakId;
    private String status; // 'WORKING' | 'BREAK' 

}
