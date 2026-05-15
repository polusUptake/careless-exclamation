package com.proj1.oops_backend.login;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        Map<String, Object> facultyRow = findFacultyRow(req.username(), req.password());
        if (facultyRow == null) {
            return Map.of("success", false);
        }

        Long facultyId = asLong(firstValue(facultyRow, "faculty_id", "facultyid", "id"));
        String facultyName = asString(firstValue(facultyRow, "faculty_name", "name", "full_name"));
        String department = asString(firstValue(facultyRow, "department", "dept", "department_name"));

        List<Map<String, Object>> courses = facultyId == null ? List.of() : findCoursesForFaculty(facultyId);

        return Map.of(
                "success", true,
                "facultyId", facultyId,
                "facultyName", facultyName,
                "department", department,
                "courses", courses);
    }

    private Map<String, Object> findFacultyRow(String username, String password) {
        List<String> facultyQueries = List.of(
                "SELECT * FROM \"Faculty\" WHERE username = ? AND password = ? LIMIT 1",
                "SELECT * FROM faculty WHERE username = ? AND password = ? LIMIT 1");

        for (String sql : facultyQueries) {
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, username, password);
                if (!rows.isEmpty()) {
                    return rows.get(0);
                }
            } catch (DataAccessException ignored) {
                // Try next naming variant.
            }
        }
        return null;
    }

    private List<Map<String, Object>> findCoursesForFaculty(Long facultyId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> tableCandidates = List.of("\"Courses\"", "courses");
        List<String> fkCandidates = List.of("faculty_id", "facultyid", "facultyId");

        for (String table : tableCandidates) {
            for (String fk : fkCandidates) {
                String sql = "SELECT * FROM " + table + " WHERE " + fk + " = ?";
                try {
                    rows = jdbcTemplate.queryForList(sql, facultyId);
                    break;
                } catch (DataAccessException ignored) {
                    // Try next fk/table variant.
                }
            }
            if (!rows.isEmpty()) {
                break;
            }
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseId", asString(firstValue(row, "course_id", "id", "courseid")));
            item.put("courseName", asString(firstValue(row, "course_name", "course", "name", "title", "course_title")));
            response.add(item);
        }
        return response;
    }

    private Object firstValue(Map<String, Object> row, String... candidates) {
        for (String candidate : candidates) {
            Object value = findByKeyIgnoreCase(row, candidate);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Object findByKeyIgnoreCase(Map<String, Object> row, String target) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(target)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
