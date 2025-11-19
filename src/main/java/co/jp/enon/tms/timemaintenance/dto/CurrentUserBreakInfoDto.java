package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;
import java.time.LocalTime;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class CurrentUserBreakInfoDto extends BaseDto{
	private static final long serialVersionUID = 1L;

    public CurrentUserBreakInfoDto() {
        reqHd = new RequestHd();
        resHd = new ResponseHd();
        super.setTranId(this.getClass().getName());
    }
    private RequestHd reqHd;
    private ResponseHd resHd;
    
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

        private Integer workSessionId;
        private Integer workBreakId;
        private LocalTime breakStart;
        private LocalTime breakEnd;
        private Integer breakTime; // minutes
    }      
}
