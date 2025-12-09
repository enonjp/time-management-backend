package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;
import java.time.LocalTime;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data

public class WorkReportInsertDto extends BaseDto {
	private static final long serialVersionUID = 1L;

	// Defining a no-arg constructor
	public WorkReportInsertDto() {
		reqHd = new RequestHd();
		responseHd = new ResponseHd();
    	super.setTranId(this.getClass().getName());
    }

	// Declaring properties (member variables)
	private RequestHd reqHd;
	private ResponseHd responseHd;

	@Data
	public static class RequestHd implements Serializable {
	    // static final long serialVersionUIDが必要
	    private static final long serialVersionUID = 1L;

	    // Defining a no-arg constructor
	    public RequestHd() {}

		// Declaring properties (member variables)
	    private int userId;
	    private String workDate;
	    private LocalTime startTime;
	    private LocalTime endTime;
	}
	@Data
	public static class ResponseHd implements Serializable {
	    // static final long serialVersionUIDが必要
	    private static final long serialVersionUID = 1L;

	    // Defining a no-arg constructor
	    public ResponseHd() {}

		// Declaring properties (member variables)
	    private int workReportId;
	    private int workSessionId;
	    private String status;
	}

}
