package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class UserSessionsTodayDto extends BaseDto {

	private static final long serialVersionUID = 1L;
	
	public UserSessionsTodayDto() {
        reqHd = new RequestHd();
        resDt = new ArrayList<ResponseDt>();
      //  resDtTitle = new ResponseDtTitle();
        super.setTranId(this.getClass().getName());
    }
	 private RequestHd reqHd;
	 private List<ResponseDt> resDt;
	// private Object resDtTitle;
	 
	@Data
    public static class RequestHd implements Serializable {
        private static final long serialVersionUID = 1L;
        public RequestHd() {}
        private Integer userId;
    }
	 
	@Data
    public static class ResponseDt implements Serializable {
        private static final long serialVersionUID = 1L;

        public ResponseDt() {}

        private Integer workSessionId;
        private LocalTime sessionStart;
        private LocalTime sessionEnd;
        private Integer sessionWorkTime;  // minutes
        private Integer sessionBreakTime; // minutes
        
	   
	} 
	 
}
