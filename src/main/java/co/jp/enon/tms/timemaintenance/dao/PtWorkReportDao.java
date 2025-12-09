package co.jp.enon.tms.timemaintenance.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import co.jp.enon.tms.timemaintenance.entity.PtWorkReport;

@Repository
public class PtWorkReportDao {
	
	private final JdbcTemplate jdbcTemplate;

    public PtWorkReportDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("this.jdbcTemplate " + this.jdbcTemplate);
    }
    
    private RowMapper<PtWorkReport> workReportRowMapper = new RowMapper<PtWorkReport>() {
        @Override
        public PtWorkReport mapRow(ResultSet rs, int rowNum) throws SQLException {
            PtWorkReport report = new PtWorkReport();
            report.setWorkReportId(rs.getInt("work_report_id"));
            report.setUserId(rs.getInt("user_id"));
            report.setWorkDate(rs.getDate("work_date").toLocalDate());
            report.setTotalWorkTime(rs.getInt("total_work_time"));
            report.setTotalBreakTime(rs.getInt("total_break_time"));
            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return report;
        }
    };
    
    // Insert new work report
    public int save(PtWorkReport ptWorkReport) {
        String sql = "INSERT INTO pt_work_report (user_id, work_date, created_at, updated_at) VALUES (?, ?, NOW(), NOW()) ";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ptWorkReport.getUserId());
            ps.setDate(2, java.sql.Date.valueOf(ptWorkReport.getWorkDate()));
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
    
    //select workReportId from pt_work_report table
    public PtWorkReport getWorkReportByUserIdAndDate(int userId, LocalDate workDate) {
        String sql = "SELECT * FROM pt_work_report WHERE user_id = ? AND work_date = ?";

        try {
            return jdbcTemplate.queryForObject(sql, workReportRowMapper, userId, java.sql.Date.valueOf(workDate));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public int updateWorkReport(PtWorkReport ptWorkReport) {
        String sql = "UPDATE pt_work_report " +
                     "SET total_work_time = ?, total_break_time = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE work_report_id = ? AND user_id = ? AND work_date = ?";

        int result = jdbcTemplate.update(
            sql,
            ptWorkReport.getTotalWorkTime(),
            ptWorkReport.getTotalBreakTime(),
            ptWorkReport.getWorkReportId(),
            ptWorkReport.getUserId(),
            java.sql.Date.valueOf(ptWorkReport.getWorkDate())
        );
	    jdbcTemplate.execute("COMMIT");
	    return result;   
    }    

    
}
