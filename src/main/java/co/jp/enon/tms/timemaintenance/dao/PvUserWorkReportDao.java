package co.jp.enon.tms.timemaintenance.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;

import co.jp.enon.tms.timemaintenance.entity.PvUserWorkReport;

@Repository
public class PvUserWorkReportDao {
	private final JdbcTemplate jdbcTemplate;

    public PvUserWorkReportDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("this.jdbcTemplate " + this.jdbcTemplate);
    }
    
    private RowMapper<PvUserWorkReport> workReportDetailRowMapper = new RowMapper<PvUserWorkReport>() {
        @Override
        public PvUserWorkReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        	PvUserWorkReport detail = new PvUserWorkReport();

            // User info
            detail.setUserId(rs.getInt("user_id"));
            detail.setFirstName(rs.getString("first_name"));
            detail.setLastName(rs.getString("last_name"));

            // Work report info
            detail.setWorkReportId(rs.getInt("work_report_id"));
            detail.setWorkDate(rs.getDate("work_date") != null ? rs.getDate("work_date").toLocalDate() : null);
            detail.setDailyWorkTime(rs.getInt("daily_work_time"));
            detail.setDailyBreakTime(rs.getInt("daily_break_time"));

            // Work session info
            detail.setWorkSessionId(rs.getInt("work_session_id"));
            detail.setSessionStart(rs.getTime("session_start") != null ? rs.getTime("session_start").toLocalTime() : null);
            detail.setSessionEnd(rs.getTime("session_end") != null ? rs.getTime("session_end").toLocalTime() : null);
            detail.setSessionWorkTime(rs.getInt("session_work_time"));
            detail.setSessionBreakTime(rs.getInt("session_break_time"));

            // Break info
            detail.setWorkBreakId(rs.getInt("work_break_id"));
            detail.setBreakStart(rs.getTime("break_start") != null ? rs.getTime("break_start").toLocalTime() : null);
            detail.setBreakEnd(rs.getTime("break_end") != null ? rs.getTime("break_end").toLocalTime() : LocalTime.of(0, 0));
            detail.setBreakDuration(rs.getInt("break_duration"));

            return detail;
        }
    };
    
    public List<PvUserWorkReport> getUserWorkReport(Integer userId, String firstName, String lastName, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                u.user_id,
                u.first_name,
                u.last_name,
                wr.work_report_id,
                wr.work_date,
                wr.total_work_time AS daily_work_time,
                wr.total_break_time AS daily_break_time,
                ws.work_session_id,
                ws.start_time AS session_start,
                ws.end_time AS session_end,
                ws.work_time AS session_work_time,
                ws.break_time AS session_break_time,
                wb.work_break_id,
                wb.break_start,
                wb.break_end,
                wb.break_time AS break_duration
            FROM pt_user u
            JOIN pt_work_report wr ON u.user_id = wr.user_id
            LEFT JOIN pt_work_session ws ON wr.work_report_id = ws.work_report_id
            LEFT JOIN pt_work_break wb ON ws.work_session_id = wb.work_session_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        // add if userId exists
        if (userId != null) {
            sql.append(" AND u.user_id = ?");
            params.add(userId);
        }
        if (firstName != null) {
            sql.append(" AND u.first_name = ?");
            params.add(firstName);
        }
        if (lastName != null) {
            sql.append(" AND u.last_name = ?");
            params.add(lastName);
        }
      
        if (startDate != null && endDate != null) {
            sql.append(" AND wr.work_date BETWEEN ? AND ?");
            params.add(java.sql.Date.valueOf(startDate));
            params.add(java.sql.Date.valueOf(endDate));
        } else if (startDate != null) {
            sql.append(" AND wr.work_date >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        } else if (endDate != null) {
            sql.append(" AND wr.work_date <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }

        sql.append(" ORDER BY wr.work_date ASC, ws.work_session_id ASC, wb.break_start ASC");

        try {
            return jdbcTemplate.query(sql.toString(), workReportDetailRowMapper, params.toArray());
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList(); // return empty list if no data
        }
    }

    
//    public List<PvUserWorkReport> getUserWorkReport(int userId, LocalDate startDate, LocalDate endDate) {
//        String sql = """
//            SELECT 
//                u.user_id,
//                u.first_name,
//                u.last_name,
//                wr.work_report_id,
//                wr.work_date,
//                wr.total_work_time AS daily_work_time,
//                wr.total_break_time AS daily_break_time,
//                ws.work_session_id,
//                ws.start_time AS session_start,
//                ws.end_time AS session_end,
//                ws.work_time AS session_work_time,
//                ws.break_time AS session_break_time,
//                wb.work_break_id,
//                wb.break_start,
//                wb.break_end,
//                wb.break_time AS break_duration
//            FROM pt_user u
//            JOIN pt_work_report wr ON u.user_id = wr.user_id
//            LEFT JOIN pt_work_session ws ON wr.work_report_id = ws.work_report_id
//            LEFT JOIN pt_work_break wb ON ws.work_session_id = wb.work_session_id
//            WHERE u.user_id = ?
//              AND wr.work_date BETWEEN ? AND ?
//            ORDER BY wr.work_date ASC, ws.work_session_id ASC, wb.break_start ASC
//        """;
//
//        try {
//            return jdbcTemplate.query(
//                sql,
//                workReportDetailRowMapper,
//                userId,
//                java.sql.Date.valueOf(startDate),
//                java.sql.Date.valueOf(endDate)
//            );
//        } catch (EmptyResultDataAccessException e) {
//            return Collections.emptyList(); // return empty list if no data
//        }
//    }
    
}
    
    

