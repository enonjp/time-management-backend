package co.jp.enon.tms.timemaintenance.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.jp.enon.tms.timemaintenance.entity.PvUserWorkReportSessionBreak;

@Repository
public class PvUserWorkReportSessionBreakDao {
	
	private final JdbcTemplate jdbcTemplate;

    public PvUserWorkReportSessionBreakDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("this.jdbcTemplate " + this.jdbcTemplate);
    }
    
    private RowMapper<PvUserWorkReportSessionBreak> pvUserWorkReportSessionBreak = new RowMapper<PvUserWorkReportSessionBreak>() {
        @Override
        public PvUserWorkReportSessionBreak mapRow(ResultSet rs, int rowNum) throws SQLException {
        	PvUserWorkReportSessionBreak pvUserWorkReportSessionBreak = new PvUserWorkReportSessionBreak();

        	pvUserWorkReportSessionBreak.setWorkSessionId(rs.getInt("work_session_id"));
        	pvUserWorkReportSessionBreak.setWorkReportId(rs.getInt("work_report_id"));
        	pvUserWorkReportSessionBreak.setWorkBreakId((Integer) rs.getObject("work_break_id")); // handling null
        	pvUserWorkReportSessionBreak.setStatus(rs.getString("status"));

            return pvUserWorkReportSessionBreak;
        }
    };
    
    public PvUserWorkReportSessionBreak getUserWorkReportSessionBreakInfo(int userId) {
        String sql = "SELECT wr.work_report_id, ws.work_session_id," +
        		" CASE  WHEN ws.status = 'BREAK' THEN wb.work_break_id ELSE NULL END AS work_break_id, ws.status " +
                     " FROM pt_work_report wr " +
                     " JOIN pt_work_session ws ON ws.work_report_id = wr.work_report_id "+
                     " LEFT JOIN pt_work_break wb ON wb.work_session_id = ws.work_session_id AND wb.break_end IS NULL " +
                     " WHERE wr.user_id = ? AND wr.work_date = CURDATE() " +
                     " AND ws.status IN ('WORKING', 'BREAK')";

        return jdbcTemplate.query(sql, ps -> {
            ps.setInt(1, userId);
        }, rs -> {
            if (rs.next()) {
                return pvUserWorkReportSessionBreak.mapRow(rs, 1);
            } else {
                return null;
            }
        });
    }

}
