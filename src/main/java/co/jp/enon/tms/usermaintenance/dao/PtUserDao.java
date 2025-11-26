package co.jp.enon.tms.usermaintenance.dao;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.jp.enon.tms.usermaintenance.entity.PtUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Repository
public class PtUserDao {
	private final JdbcTemplate jdbcTemplate;

    public PtUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // RowMapper to map ResultSet to PtUser
    private RowMapper<PtUser> userRowMapper = new RowMapper<PtUser>() {
        @Override
        public PtUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            PtUser user = new PtUser();
            user.setUserId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setResetPasswordToken(rs.getString("reset_password_token"));
//            user.setResetTokenExpiry(rs.getTimestamp("reset_token_expiry").toLocalDateTime());  
            user.setRole(rs.getByte("role"));
            user.setActive(rs.getByte("active"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return user;
        }
    };
    public List<PtUser> findAll() {
        String sql = "SELECT * FROM pt_user";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    //Find user by email
    public PtUser findByEmail(String email) {
        String sql = "SELECT * FROM pt_user WHERE email = ?";
        List<PtUser> users = jdbcTemplate.query(sql, userRowMapper, email);
        return  users.isEmpty() ? null : users.get(0);
    }
    
    //Find user by reset_password_token
    public PtUser findByResetPasswordToken(String token) {
        String sql = " SELECT * FROM pt_user WHERE reset_password_token = ? AND DATE_ADD(reset_token_expiry, INTERVAL 1 HOUR) > NOW(); ";  
        List<PtUser> users = jdbcTemplate.query(sql, userRowMapper, token);
        return users.isEmpty() ? null : users.get(0);
    }

    // Insert new user
    public int save(PtUser user) {
        String sql = "INSERT INTO pt_user (email, first_name, last_name, password, role, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        return jdbcTemplate.update(sql, user.getEmail(), user.getFirstName(), user.getLastName(), user.getPassword(), user.getRole(), user.getActive());
    }
    
    public int updateToken(PtUser user) {
        String sql = "UPDATE pt_user SET reset_password_token = ?, reset_token_expiry = ? WHERE email = ?";
        // 1. Get the LocalDateTime
        LocalDateTime expiry = user.getResetTokenExpiry();
        // 2. Convert LocalDateTime to java.sql.Timestamp
        java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(expiry);
        int result = jdbcTemplate.update(sql, user.getResetPasswordToken(), sqlTimestamp, user.getEmail());
        jdbcTemplate.execute("COMMIT");
        return result;
    }
    
    public int updatePassword (PtUser user) {
    	String sql = "UPDATE pt_user SET password = ?, reset_password_token = ?, reset_token_expiry = ?  WHERE email = ?";
        int result = jdbcTemplate.update(sql, user.getPassword(), user.getResetPasswordToken(), user.getResetTokenExpiry(), user.getEmail());
        jdbcTemplate.execute("COMMIT");
        return result;        
    }
    
    public int delete(String email) {
        String sql = "UPDATE pt_user SET active = ? WHERE email = ?";
        return jdbcTemplate.update(sql, 1, email);
    }
}
