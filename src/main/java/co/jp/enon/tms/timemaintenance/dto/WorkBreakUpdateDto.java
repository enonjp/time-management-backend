package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;
import java.time.LocalTime;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class WorkBreakUpdateDto  extends BaseDto {
	private static final long serialVersionUID = 1L;

	// Defining a no-arg constructor
	public WorkBreakUpdateDto() {
		reqHd = new RequestHd();
    	super.setTranId(this.getClass().getName());
    }

	//Declaring properties (member variables)
	private RequestHd reqHd;

	@Data
	public static class RequestHd implements Serializable {
	    // static final long serialVersionUIDが必要
	    private static final long serialVersionUID = 1L;

	    // Defining a no-arg constructor
	    public RequestHd() {}

		// Declaring properties (member variables)
	    private int workBreakId;
	    private int workSessionId;
	    private LocalTime breakStart;
	    private LocalTime breakEnd;
	    private int breakTime;
	}
}
