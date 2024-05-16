package com.example.ptu.ptu_award.controller;

import com.example.ptu.ptu_award.models.AwardEntity;
import com.example.ptu.ptu_award.models.DetailAward;
import com.example.ptu.ptu_award.models.FilterAward;
import com.example.ptu.ptu_award.models.Entity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import java.io.InputStream;
@RestController
public class Award {

    private final JdbcTemplate jdbcTemplate;


    @Autowired
    private ResourceLoader resourceLoader;


    public Award(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 일반 리스트 조회
    @GetMapping("/h1")
    public List<Entity> getPeople() {
        List<Entity> people = new ArrayList<Entity>();
        people.add(new Entity(1, "김민서"));
        people.add(new Entity(2, "이재윤"));
        return people;
    }

    // db 리스트 조회 단 반환타입 지정 x
    @GetMapping("/before-awards")
    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList("SELECT id, title, date_period, filter_point FROM sys.award");
    }

    // db 리스트 조회 단 반환타입 지정 o JPA
    @GetMapping("/after-awards")
    public List<AwardEntity> afterFindAll() {
        String sql = "SELECT id, title, date_period, filter_point FROM sys.award";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AwardEntity(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("department_name"),
                rs.getString("description"),
                rs.getString("point"),
                rs.getString("date_period"),
                rs.getString("contact_info")
        ));
    }

    // db 데이터 추가
    @GetMapping("/insert")
    public ResponseEntity<String> insertData() {
        /**
         *  1. url에 data를 실어보낸다
         *  2. api body값안에 data를 실어보낸다
         *
         */
        String sql = "INSERT INTO sys.award (title, department_name, description, point, date_period, contact_info) " +
                "VALUES ('자바로 넣었음 제목', '부서명', '설명문', 5000, '2024-03-01~2024-05-01', '학생관4층')";

        try {
            jdbcTemplate.update(sql);
            return ResponseEntity.ok("Data inserted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inserting data");
        }
    }

    // 단일 목록 조회
    @GetMapping("/award/{id}")
    public DetailAward findAwardById(@PathVariable("id") int id) {
        String sql = "SELECT department_name, title, date_period, description, point, contact_info, link FROM sys.award WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new DetailAward(
                rs.getString("title"),
                rs.getString("department_name"),
                rs.getString("description"),
                rs.getString("point"),
                rs.getString("date_period"),
                rs.getString("contact_info"),
                rs.getString("link")
        ));
    }

    // 필터 목록 조회
    @GetMapping("/filter-award/{filterType}/{value}")
    public List<FilterAward> filterAwardByValue(@PathVariable("filterType") String filterType, @PathVariable("value") String value) {
        String sql = "";
        Object[] params = null;

        if (filterType.equals("point")) {
            sql = "SELECT department_name, title, date_period, description, filter_point, point, contact_info, link " +
                    "FROM sys.award " +
                    "WHERE CAST(REGEXP_REPLACE(filter_point, '[^0-9]', '') AS UNSIGNED) >= ?";
            params = new Object[]{Integer.parseInt(value)};
        } else if (filterType.equals("date")) {
            sql = "SELECT department_name, title, date_period, description, filter_point, point, contact_info, link " +
                    "FROM sys.award " +
                    "WHERE STR_TO_DATE(SUBSTRING_INDEX(SUBSTRING_INDEX(date_period, '~', -1), '(', 1), '%Y.%m.%d') >= ?";
            params = new Object[]{value};
        } else {
            throw new IllegalArgumentException("Invalid filter type: " + filterType);
        }

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new FilterAward(
                rs.getString("title"),
                rs.getString("department_name"),
                rs.getString("description"),
                rs.getString("filter_point"),
                rs.getString("point"),
                rs.getString("date_period"),
                rs.getString("contact_info"),
                rs.getString("link")
        ));
    }

    @GetMapping("/filter-award/both/{value1}/{value2}")
    public List<FilterAward> filterAwardByBothValues(@PathVariable("value1") String value1, @PathVariable("value2") String value2) {
        String sql = "SELECT department_name, title, date_period, description, filter_point, point, contact_info, link " +
                "FROM sys.award " +
                "WHERE CAST(REGEXP_REPLACE(filter_point, '[^0-9]', '') AS UNSIGNED) >= ? " +
                "AND STR_TO_DATE(SUBSTRING_INDEX(SUBSTRING_INDEX(date_period, '~', -1), '(', 1), '%Y.%m.%d') >= ?";
        Object[] params = new Object[]{Integer.parseInt(value1), value2};

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new FilterAward(
                rs.getString("title"),
                rs.getString("department_name"),
                rs.getString("description"),
                rs.getString("filter_point"),
                rs.getString("point"),
                rs.getString("date_period"),
                rs.getString("contact_info"),
                rs.getString("link")
        ));
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<Resource> getJsonFile() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:csvjson.json");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .body(resource);
    }

    @GetMapping("/insert-new")
    public ResponseEntity<String> insertDataNew() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 파일을 읽어옵니다. 여기서는 classpath에 위치한 json 파일을 읽어오는 예시입니다.
            InputStream inputStream = getClass().getResourceAsStream("/csvjson.json");

            // JSON 파일을 자바 객체로 변환합니다.
            List<Map<String, Object>> jsonData = objectMapper.readValue(inputStream, List.class);

            // 각 객체를 데이터베이스에 추가합니다.
            for (Map<String, Object> data : jsonData) {
                String sql = "INSERT INTO sys.award (title, department_name, description, point,filter_point,date_period, contact_info,link) " +
                        "VALUES (?, ?, ?, ?, ?, ?,?,?)";

                jdbcTemplate.update(sql,
                        data.get("프로그램명"),
                        data.get("부서명"),
                        data.get("설명"),
                        data.get("포인트"),
                        data.get("필터포인트"),
                        data.get("신청기간"),
                        data.get("문의처"),
                        data.get("상세")
                );
            }

            return ResponseEntity.ok("Data inserted successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading JSON file");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inserting data");
        }
    }
}