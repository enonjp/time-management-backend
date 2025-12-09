package co.jp.enon.tms.timemaintenance.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.jp.enon.tms.timemaintenance.entity.PvUserWorkSession;

@Repository
public class PvUserWorkSessionDao {

	private final JdbcTemplate jdbcTemplate;

    public PvUserWorkSessionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("this.jdbcTemplate " + this.jdbcTemplate);
    }
    
    private RowMapper<PvUserWorkSession> userWorkSessionRowMapper = new RowMapper<PvUserWorkSession>() {
        @Override
        public PvUserWorkSession mapRow(ResultSet rs, int rowNum) throws SQLException {
        	PvUserWorkSession pvUserWorkSession = new PvUserWorkSession();
            // User info
        	pvUserWorkSession.setUserId(rs.getInt("user_id"));
            // Work session info
        	pvUserWorkSession.setWorkSessionId(rs.getInt("work_session_id"));
        	pvUserWorkSession.setSessionStart(rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null);
        	pvUserWorkSession.setSessionEnd(rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null);
        	pvUserWorkSession.setSessionWorkTime(rs.getInt("work_time"));
        	pvUserWorkSession.setSessionBreakTime(rs.getInt("break_time"));
        	pvUserWorkSession.setStatus(rs.getString("status"));
            return pvUserWorkSession;
        }
    };
    
    public PvUserWorkSession getLatestSessionForUser(int userId) {
        String sql = "SELECT ws.* , wr.user_id FROM pt_work_session ws " +
        		"JOIN pt_work_report wr ON ws.work_report_id = wr.work_report_id " +
                     "WHERE wr.user_id = ?  AND wr.work_date = CURDATE() " +
                     "ORDER BY ws.start_time DESC " +
                     "LIMIT 1";

        return jdbcTemplate.query(sql, ps -> {
            ps.setInt(1, userId);
        }, rs -> {
            if (rs.next()) {
                return userWorkSessionRowMapper.mapRow(rs, 1);
            } else {
                return null;
            }
        });
    }
    
    public  List<PvUserWorkSession> getAllUserSessionForToday(int userId) {
    	String sql = """
                SELECT 
                    ws.*, 
                    wr.user_id 
                FROM pt_work_session ws
                JOIN pt_work_report wr 
                    ON ws.work_report_id = wr.work_report_id
                WHERE wr.user_id = ? 
                  AND wr.work_date = CURDATE()
                ORDER BY ws.start_time ASC
                """;

        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps;
        }, userWorkSessionRowMapper);    
    }
    
}
