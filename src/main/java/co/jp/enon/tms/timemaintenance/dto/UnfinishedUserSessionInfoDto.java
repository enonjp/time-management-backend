package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class UnfinishedUserSessionInfoDto extends BaseDto {

	private static final long serialVersionUID = 1L;
	
	public UnfinishedUserSessionInfoDto() {
        reqHd = new RequestHd();
        resHd = new ResponseHd();
      //  resDtTitle = new ResponseDtTitle();
        super.setTranId(this.getClass().getName());
    }
	 private RequestHd reqHd;
	 private ResponseHd resHd;
	// private Object resDtTitle;
	 
	@Data
    public static class RequestHd implements Serializable {
        private static final long serialVersionUID = 1L;
        public RequestHd() {}
        private Integer userId;
    }
	 
	@Data
    public static class ResponseHd implements Serializable {
        private static final long serialVersionUID = 1L;

        public ResponseHd() {}
        private Integer workReportId;
        private Integer workSessionId;
        private Integer workBreakId;
        private String status; // 'WORKING', 'BREAK'
        	   
	} 
	 
}

