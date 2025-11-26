package co.jp.enon.tms.usermaintenance.service;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.jp.enon.tms.common.security.ImplementsUserDetails;
import co.jp.enon.tms.usermaintenance.dao.PtUserDao;
import co.jp.enon.tms.usermaintenance.dto.UserInsertDto;
import co.jp.enon.tms.usermaintenance.entity.PtUser;

@Service
public class LoginUserService implements UserDetailsService {
	final static Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
    private PtUserDao ptUserDao;
	
	@Autowired
	private PasswordEncoder passwordEncoder; // BCryptPasswordEncoder injected automatically

	@Override
	//@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());

		// This method is called by authenticationManager.authenticate()
	    // during login to validate the user's password.
	    // For non-login requests, roles can be loaded via RoleService if needed.	
		PtUser user = ptUserDao.findByEmail(email);
		if (user == null) {
	        throw new UsernameNotFoundException("User not found with email: " + email);
	    }
		// Get request context (if available)
	    String loginEmail = email; // default
	    try {
	        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	        String certEmail = getCert(req); // your existing method for certificate extraction
	        if (certEmail != null && !certEmail.isEmpty()) {
	            loginEmail = certEmail;
	        }
	        logger.debug("LoginUserService.loadUserByUsername loginEmail=" + loginEmail + ", email=" + email);
	    } catch (Exception e) {
	        logger.warn("Could not retrieve request context for loadUserByUsername", e);
	    }

	    // Build authorities (roles)
	    List<GrantedAuthority> authorities = new ArrayList<>();
	    authorities.add(new SimpleGrantedAuthority(getRoleName(user.getRole())));

	    // 4Return a fully populated ImplementsUserDetails
	    return ImplementsUserDetails.build(
	            user.getUserId(),
	            loginEmail,
	            user.getPassword(),   // <-- must be the hashed password from DB
	            authorities
	    );
	}
	
	private String getRoleName(Byte roleCode) {
	    if (roleCode == 1) return "ROLE_ADMIN";
	    else return "ROLE_USER";
	}

	private String getCert(HttpServletRequest request) {
	    // Retrieve client certificate chain (only available over HTTPS)
	    X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
	    if (certs == null || certs.length == 0) {
	        return "";
	    }
	    try {
	        // Get subject from the first certificate
	        Principal principal = certs[0].getSubjectX500Principal();
	        String principalName = principal.getName(); // e.g. "CN=john@example.com, OU=Users, O=Example, C=JP"

	        // Parse "CN=" value (Common Name)
	        for (String part : principalName.split(",")) {
	            String[] pair = part.trim().split("=");
	            if (pair.length == 2 && pair[0].equalsIgnoreCase("CN")) {
	                return pair[1].trim();
	            }
	        }
	    } catch (Exception e) {
	        logger.warn("Failed to extract CN from client certificate", e);
	    }

	    return "";
	}
	
	public void registerUser(UserInsertDto userInsertDto) throws Exception {
	    var reqHd = userInsertDto.getReqHd();

	    PtUser user = new PtUser();
	    user.setEmail(reqHd.getEmail());
	    user.setFirstName(reqHd.getFirstName());
	    user.setLastName(reqHd.getLastName());
	    // Encode the password with BCrypt
        String encodedPassword = passwordEncoder.encode(reqHd.getPassword());
        user.setPassword(encodedPassword);
	    user.setRole((byte) 0);   // default role = user
	    user.setActive((byte) 0); // default active
	    try {
	        ptUserDao.save(user);    
	        userInsertDto.setResultCode("000");
	    } catch (DuplicateKeyException ex) {
	        userInsertDto.setResultCode("002");
	        userInsertDto.setResultMessage("（Method：insert, Table Name：user, Email：" + reqHd.getEmail() + "）");
	    }
	    return;
	}
	
	public String updateResetToken(String email) {
        PtUser user = ptUserDao.findByEmail(email);
        if (user == null) return null;

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        user.setResetPasswordToken(token);
        user.setResetTokenExpiry(expiry);

        int updatedRows = ptUserDao.updateToken(user);
        return updatedRows > 0 ? token : null;
    }
	// Verify token and reset password
    public String resetPassword(String token, String newPassword) {
        PtUser user = ptUserDao.findByResetPasswordToken(token);
        if (user == null) {
            return "Invalid or expired token.";
        }
        // Encode the password with BCrypt
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null); // clear token
        user.setResetTokenExpiry(null);
        ptUserDao.updatePassword(user);
        return "Password reset successful!";
    } 	

}
