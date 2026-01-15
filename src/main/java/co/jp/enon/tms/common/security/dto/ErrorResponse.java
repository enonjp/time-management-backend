package co.jp.enon.tms.common.security.dto;

public class ErrorResponse extends Response {
	
    private String error;
    private String message;
    private String path;
    
    public ErrorResponse() {}

    public ErrorResponse(String error, String message, String path) {
        this.setError(error);
        this.setMessage(message);
        this.setPath(path);
    }

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
 
    

}
