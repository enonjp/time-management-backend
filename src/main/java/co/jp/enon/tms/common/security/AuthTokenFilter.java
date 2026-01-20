package co.jp.enon.tms.common.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import co.jp.enon.tms.usermaintenance.service.LoginUserService;

//extend OncePerRequestFilter class to Overwrite the filter that handles authorisation 
public class AuthTokenFilter extends OncePerRequestFilter {

	// JWT authorisation processing class in Spring
	@Autowired
	private JwtUtils jwtUtils;

	// this class retrieves the logged-in user information from the DB in Spring
	@Autowired
	//private ImplementsUserDetailsService userDetailsService;
    private LoginUserService loginUserService;
	//private RoleService roleServie;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	// Filter that 
	// "/api/auth/signin"にリンクされたLoginController.authenticateUser()を実行する前に、このdoFilterInternal()が呼び出される。
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {

			String jwt = parseJwt(request);

			// Controllerへの接続がログインのときはjwtに値がnullなので、ここでは特に何も処理しない。
			// （ログイン時の認証処理はLoginControllerで行い、その後、認可処理のためのトークンの作成を行う。）
			// ログインでない他の全ての処理ではjwtに値がセットされ、次のvalidateJwtToken()でログイン時に返したトークンであるか検証する。
			if (jwt != null) {

				// 次ののvalidateJwtToken()がセッションステートレスでの認可処理になる。
				if (jwtUtils.validateJwtToken(jwt)) {

					// jwtが正しい場合、ユーザ名をjwtから取り出してDBを検索してRoleを取得する。
					String email= jwtUtils.getUserNameFromJwtToken(jwt);

					UserDetails userDetails = loginUserService.loadUserByUsername(email);
					
					logger.info("loadUserByUsername called with email: {}", email);
					logger.info("DB password (hashed) = {}",userDetails.getPassword());

					// 第1引数のuserDetailsは、DBから取得した値を渡すがログイン時のそれぞれの値と異ってもエラーにならない。
					// 第3引数のuserDetails.getAuthorities()は、DBから取得したRoleをListにして渡す。
					// Controllerの@PreAuthorize("hasRole('ADMIN')")など'ADMIN'の先頭に'ROLE_'を挿入した値が
					// 第3引数のListに存在しないと、500:Internal Server Errorになる。
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());

					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				/*	if (userDetails.getUsername().equalsIgnoreCase("tanaka")) {
						DataSourceContextHolder.setDataSourceType(DataSourceType.KPMS);
					}else {
						DataSourceContextHolder.setDataSourceType(DataSourceType.KDRS);
					}*/


				} else {
					logger.error("jwtUtils.validateJwtToken: false");	// 認可処理でのエラー
				}
			} else {
				logger.info("String jwt = parseJwt(request); jwt is null");	// ログイン時には、jwtはnullになる。
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
		}

		filterChain.doFilter(request, response);

	}
	// HttpServletRequestからJwtの文字列を取り出す
	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}

}
