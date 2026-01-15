package co.jp.enon.tms.common.security.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.jp.enon.tms.common.security.ImplementsUserDetails;
import co.jp.enon.tms.common.security.JwtUtils;
import co.jp.enon.tms.common.security.dto.ErrorResponse;
import co.jp.enon.tms.common.security.dto.ErrorStatus;
import co.jp.enon.tms.common.security.dto.JwtResponseDto;
import co.jp.enon.tms.common.security.dto.LoginRequestDto;
import co.jp.enon.tms.common.security.dto.Response;
import co.jp.enon.tms.common.security.dto.ResponseFactory;
import co.jp.enon.tms.usermaintenance.dto.UserInsertDto;
import co.jp.enon.tms.usermaintenance.service.LoginUserService;
import co.jp.enon.tms.usermaintenance.service.MailService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	LoginUserService loginUserService;
	
	@Autowired
	MailService mailService;

	@Autowired
	JwtUtils jwtUtils;
	
	@PostMapping("/signin")
	public ResponseEntity<Response> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
	    logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());

	    String email = loginRequest.getEmail().trim();
	    String rawPassword = loginRequest.getPassword().trim();
	    try {
	        // Create authentication token (raw password)
	        UsernamePasswordAuthenticationToken authToken =
	                new UsernamePasswordAuthenticationToken(email, rawPassword);
	        // Authenticate via AuthenticationManager
	        Authentication authentication = authenticationManager.authenticate(authToken);
	        // Set SecurityContext for current thread
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        // Generate JWT token
	        String jwt = jwtUtils.generateJwtToken(authentication);
	        // Get user details and authorities
	        ImplementsUserDetails userDetails = (ImplementsUserDetails) authentication.getPrincipal();
	        List<String> authorities = userDetails.getAuthorities().stream()
	                .map(GrantedAuthority::getAuthority)
	                .collect(Collectors.toList());
	        // Build and return JWT response
	        JwtResponseDto jwtResponse = new JwtResponseDto(
	                jwt,
	                userDetails.getId(),
	                userDetails.getUsername(), // email
	                authorities
	        );

	        // Wrap in SuccessResponse
	        return ResponseEntity.ok(
	                ResponseFactory.success(jwtResponse)
	        );

	    } catch (Exception e) {
	        logger.info("Authentication failed for email {}: {}", email, e.getMessage());
	        
	        
	        ErrorResponse errorResponse = new ErrorResponse();
	        errorResponse.setStatus(new ErrorStatus(
	                HttpStatus.UNAUTHORIZED.name(),
	                "Incorrect email or password"
	        ));
	        errorResponse.setError("UNAUTHORIZED");
	        errorResponse.setMessage("Incorrect email or password");
	        errorResponse.setPath("/signin");
	        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	    }
	}
	
//	@PostMapping("/signup")
//	public ResponseEntity<String> registerUser(@Valid @RequestBody UserInsertDto userInsertDto) {
//		logger.debug(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName());
//		 try {
//			 loginUserService.registerUser(userInsertDto);
//            if ("000".equals(userInsertDto.getResultCode())) {
//                return ResponseEntity.ok("User registered successfully!");
//            } else if ("002".equals(userInsertDto.getResultCode())) {
//                return ResponseEntity.badRequest()
//                        .body("Email already exists: " + userInsertDto.getReqHd().getEmail());
//            } else {
//                return ResponseEntity.badRequest()
//                        .body("Failed to register user.");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body("Server error: " + e.getMessage());
//        }
//	}
	
	@PostMapping("/signup")
	public ResponseEntity<Response> registerUser(
	        @Valid @RequestBody UserInsertDto userInsertDto) {

	    logger.debug(
	        this.getClass().getName() + "." +
	        Thread.currentThread().getStackTrace()[1].getMethodName()
	    );

	    loginUserService.registerUser(userInsertDto);

	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(ResponseFactory.success(null));
	}
	
	@PostMapping("/forgot-password")
	public ResponseEntity<Response> forgotPassword(
	        @RequestParam String email) {

	    loginUserService.updateResetToken(email);

	    return ResponseEntity.ok(
	            ResponseFactory.success(
	                "If the email exists, a password reset link has been sent"
	            )
	    );
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<Response> resetPassword(
	        @RequestParam String token,
	        @RequestParam String newPassword) {

	    loginUserService.resetPassword(token, newPassword);

	    return ResponseEntity.ok(
	            ResponseFactory.success("Password reset successful")
	    );
	}
	
	 // Step 1: Request password reset
//    @PostMapping("/forgot-password")
//    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
//    	String token = loginUserService.updateResetToken(email);
//    	if (token != null) {
//    		//mailService.sendResetEmail(email, token);
//            return  ResponseEntity.ok ("Password reset link has been sent to email: " + email);
//        } else {
//            return ResponseEntity.internalServerError()
//                    .body("Server error: Something went wrong while generating the reset link. Please try again.");
//        }
//    }
    
  
//	
//	@PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(
//            @RequestParam String token,
//            @RequestParam String newPassword) {
//
//        String result = loginUserService.resetPassword(token, newPassword);
//        return ResponseEntity.ok(result);
//    }
//	

}
