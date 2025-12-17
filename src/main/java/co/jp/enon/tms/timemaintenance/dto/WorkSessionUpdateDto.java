package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;
import java.time.LocalTime;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class WorkSessionUpdateDto extends BaseDto {
	
	private static final long serialVersionUID = 1L;

	// Defining a no-arg constructor
	public WorkSessionUpdateDto() {
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
	    private int userId;
	    private int workSessionId;
	    private LocalTime sessionStart;
	    private LocalTime sessionEnd;
	    
	}

}
