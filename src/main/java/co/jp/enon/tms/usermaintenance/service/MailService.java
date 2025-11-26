package co.jp.enon.tms.usermaintenance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import co.jp.enon.tms.common.BaseService;

@Service
public class MailService extends BaseService {

	final static Logger logger = LoggerFactory.getLogger(MailService.class);
	
	@Autowired
    private JavaMailSender mailSender;
	
	@Value("${tms.app.frontendResetPasswordUrl}")
    private String frontendResetPasswordUrl;
	
	// Send password reset link to user
	// In order to send gmail, follow below steps
	// 1. Go to Google Account Security
	// 2. Enable 2-Step Verification
	// Generate an App Password
	// In the same security page, go to App Passwords
	// Select: App: Mail Device: Other (give a name like SpringBootApp)
	// copy the generated 16 digit password and set it in spring.mail.password of the application properties file
	public void sendResetEmail(String email, String token) {
		String resetLink = frontendResetPasswordUrl + "?token=" + token;
	    SimpleMailMessage message = new SimpleMailMessage();
	    message.setTo(email);
	    message.setSubject("Password Reset Request");
	    message.setText("Click the link to reset your password. This link will expire in 1 hour: " + resetLink);

	    try {
	        mailSender.send(message);
	    } catch (Exception ex) {
	        logger.error("Failed to send password reset email to {}", email, ex);
	    }   		 
    }

}
