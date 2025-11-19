package co.jp.enon.tms.usermaintenance.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class PtUser {

	private Integer userId;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String resetPasswordToken;
	private Byte role; // 0=user 1=admin
	private Byte active; // 0=active 1=inactive 
	
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime resetTokenExpiry;
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

}