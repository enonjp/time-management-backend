package co.jp.enon.tms.timemaintenance.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.jp.enon.tms.timemaintenance.entity.PtWorkSession;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Repository
public class PtWorkSessionDao {
	private final JdbcTemplate jdbcTemplate;

    public PtWorkSessionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("this.jdbcTemplate " + this.jdbcTemplate);
    }
    public int save(PtWorkSession session) {
        String sql = "INSERT INTO pt_work_session (work_report_id, start_time, end_time, work_time, break_time, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, session.getWorkReportId());
            ps.setTime(2, Time.valueOf(session.getStartTime())); // convert LocalTime to java.sql.Time
            if (session.getEndTime() != null) {
                ps.setTime(3, Time.valueOf(session.getEndTime()));
            } else {
                ps.setNull(3, java.sql.Types.TIME);
            }
            ps.setInt(4, session.getWorkTime());
            ps.setInt(5, session.getBreakTime());
            ps.setString(6, session.getStatus());
            return ps;
        }, keyHolder);

        // Retrieve the generated primary key
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.intValue();
        } else {
            throw new RuntimeException("Failed to retrieve generated work_report_id.");
        }
    }
    
    public LocalTime getStartTime(int workSessionId, int workReportId) {
        String sql = "SELECT start_time FROM pt_work_session WHERE work_session_id = ? AND work_report_id = ?";

        return jdbcTemplate.queryForObject(
            sql,
            (rs, rowNum) -> {
                Time time = rs.getTime("start_time");
                return time != null ? time.toLocalTime() : null;
            },
            workSessionId, workReportId
        );
    }
    
    public int updateWorkSession(PtWorkSession ptWorkSession) {
        String sql = "UPDATE pt_work_session " +
                     "SET end_time = ?, work_time = ?, break_time = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE work_session_id = ? AND work_report_id = ?";

        return jdbcTemplate.update(
            sql,
            ptWorkSession.getEndTime() != null ? Time.valueOf( ptWorkSession.getEndTime()) : null,
            		ptWorkSession.getWorkTime(),
            		ptWorkSession.getBreakTime(),
            		ptWorkSession.getStatus(),
            		ptWorkSession.getWorkSessionId(),
            		ptWorkSession.getWorkReportId()
        );
    }
    
    public int updateWorkSessionStatus(PtWorkSession ptWorkSession) {
    	String sql = "UPDATE pt_work_session " +
                "SET status = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE work_session_id = ? ";
    	 return jdbcTemplate.update(
    	            sql,
    	            ptWorkSession.getStatus(),		
    	            ptWorkSession.getWorkSessionId()
    	        );
    }
    public int getTotalWorkTime(int workReportId) {
        String sql = "SELECT SUM(work_time) AS total_work_time FROM pt_work_session WHERE work_report_id = ?";

        Integer totalWorkTime = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            workReportId
        );

        return totalWorkTime != null ? totalWorkTime : 0;
    }
    
    public int getTotalBreakTime(int workReportId) {
        String sql = "SELECT SUM(break_time) AS total_break_time FROM pt_work_session WHERE work_report_id = ?";

        Integer totalBreakTime = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            workReportId
        );

        return totalBreakTime != null ? totalBreakTime : 0;
    }

}
