package co.jp.enon.tms.common.security.dto;

public abstract class Response {

	private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = new Status(status);
    }

}
