package co.jp.enon.tms.timemaintenance.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import co.jp.enon.tms.common.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class UserWorkReportDto extends BaseDto {
	
	private static final long serialVersionUID = 1L;

    public UserWorkReportDto() {
        reqHd = new RequestHd();
        resHd = new ResponseHd();
        resDt = new ArrayList<ResponseDt>();
        resDtTitle = new ResponseDtTitle();
        super.setTranId(this.getClass().getName());
    }
    private RequestHd reqHd;
    private ResponseHd resHd;
    private List<ResponseDt> resDt;
    private Object resDtTitle;

    @Data
    public static class RequestHd implements Serializable {
        private static final long serialVersionUID = 1L;

        public RequestHd() {}

        private Integer userId;
        private String firstName;
        private String lastName;
        private LocalDate startDate;
        private LocalDate endDate;
    }
    @Data
    public static class ResponseHd implements Serializable {
        private static final long serialVersionUID = 1L;

        public ResponseHd() {}

        private Integer userId;
        private String firstName;
        private String lastName;
        
        private List<WorkInfo> workInfos;
        
        @Data
        public static class WorkInfo implements Serializable {
            private static final long serialVersionUID = 1L;

            public WorkInfo() {}

            private LocalDate workDate;
            private Integer dailyWorkTime;   // total work time in minutes
            private Integer dailyBreakTime;  // total break time in minutes
        }
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
        
        private List<BreakInfo> breaks;
        
        @Data
        public static class BreakInfo implements Serializable {
            private static final long serialVersionUID = 1L;

            public BreakInfo() {}

            private Integer workBreakId;
            private LocalTime breakStart;
            private LocalTime breakEnd;
            private Integer breakDuration; // minutes
        }
    }
    
	@Data
	public static class ResponseDtTitle implements Serializable {
	    private static final long serialVersionUID = 1L;

	    public ResponseDtTitle() {}

		private final String userId = "User ID";
		private final String firstName = "First Name";
		private final String lastName = "Last Name";
		private final String workDate = "Work Date";
		private final String dailyWorkTime = "Work Time";
		private final String dailyBreakTime = "Break Time";
		private final String workSessionId = "Session Id";
		private final String sessionStart = "Session Start Time";
		private final String sessionEnd = "Session End Time";
		private final String sessionWorkTime = "Session Work Time";
		private final String sessionBreakTime = "Session Break Time";
		private final String workBreakId = "Break Id";
		private final String breakStart = "Break Start Time";
		private final String breakEnd = "Break End Time";
		private final String breakDuration = "Break Duration";
	}

    
}