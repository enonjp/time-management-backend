package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;
import java.time.LocalTime;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class WorkBreakInsertDto extends BaseDto {
	private static final long serialVersionUID = 1L;

	// Defining a no-arg constructor
	public WorkBreakInsertDto() {
		reqHd = new RequestHd();
		responseHd = new ResponseHd();
    	super.setTranId(this.getClass().getName());
    }

	//Declaring properties (member variables)
	private RequestHd reqHd;
	private ResponseHd responseHd;

	@Data
	public static class RequestHd implements Serializable {
	    // static final long serialVersionUIDが必要
	    private static final long serialVersionUID = 1L;

	    // Defining a no-arg constructor
	    public RequestHd() {}

		// Declaring properties (member variables)
	    private int workSessionId;
	    private LocalTime breakStart;
	}
	@Data
	public static class ResponseHd implements Serializable {
	    // static final long serialVersionUIDが必要
	    private static final long serialVersionUID = 1L;

	    //Defining a no-arg constructor
	    public ResponseHd() {}

		// Declaring properties (member variables)
	    private int workBreakId;
	}

}
