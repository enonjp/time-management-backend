package co.jp.enon.tms.timemaintenance.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.jp.enon.tms.timemaintenance.entity.PvUserWorkSessionBreak;

@Repository
public class PvUserWorkSessionBreakDao {
	
	private final JdbcTemplate jdbcTemplate;

    public PvUserWorkSessionBreakDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("this.jdbcTemplate " + this.jdbcTemplate);
    }
    
    private RowMapper<PvUserWorkSessionBreak> userWorkSessionBreakRowMapper = new RowMapper<PvUserWorkSessionBreak>() {
        @Override
        public PvUserWorkSessionBreak mapRow(ResultSet rs, int rowNum) throws SQLException {
        	PvUserWorkSessionBreak pvUserWorkSessionBreak = new PvUserWorkSessionBreak();
            // User info
        	pvUserWorkSessionBreak.setUserId(rs.getInt("user_id"));
            // Work session info
        	pvUserWorkSessionBreak.setWorkSessionId(rs.getInt("work_session_id"));
        	// break Info
        	pvUserWorkSessionBreak.setWorkBreakId(rs.getInt("work_break_id"));
        	pvUserWorkSessionBreak.setBreakStart(rs.getTime("break_start") != null ? rs.getTime("break_start").toLocalTime() : null);
        	pvUserWorkSessionBreak.setBreakEnd(rs.getTime("break_end") != null ? rs.getTime("break_end").toLocalTime() : null);
        	pvUserWorkSessionBreak.setBreakTime(rs.getInt("break_time"));
            return pvUserWorkSessionBreak;
        }
    };
    
    public PvUserWorkSessionBreak getLatestBreakForUser(int userId) {
        String sql = "SELECT wb.*, wr.user_id " +
                     "FROM pt_work_break wb " +
                     "JOIN pt_work_session ws ON wb.work_session_id = ws.work_session_id " +
                     "JOIN pt_work_report wr ON ws.work_report_id = wr.work_report_id " +
                     "WHERE wr.user_id = ? AND wr.work_date = CURDATE() " +
                     "ORDER BY wb.break_start DESC " +
                     "LIMIT 1";

        return jdbcTemplate.query(sql, ps -> ps.setInt(1, userId), rs -> {
            if (rs.next()) {
                return userWorkSessionBreakRowMapper.mapRow(rs, 1);
            } else {
                return null;
            }
        });
    }
    

}
