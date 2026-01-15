package co.jp.enon.tms.common.security.dto;

public class Status {

    public static String CODE_OK = "ok";
    public static String CODE_ERROR = "error";

    private String code;
    private String message;

    public Status(String code){
        this.code = code;
    }
    
    public Status(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
