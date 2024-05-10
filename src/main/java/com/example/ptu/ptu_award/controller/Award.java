package com.example.ptu.ptu_award.controller;

import com.example.ptu.ptu_award.models.AwardEntity;
import com.example.ptu.ptu_award.models.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class Award {

    private final JdbcTemplate jdbcTemplate;

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
        return jdbcTemplate.queryForList("SELECT * FROM ptuAward.award");
    }

    // db 리스트 조회 단 반환타입 지정 o JPA
    @GetMapping("/after-awards")
    public List<AwardEntity> afterFindAll() {
        String sql = "SELECT * FROM ptuAward.award";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AwardEntity(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("department_name"),
                rs.getString("description"),
                rs.getInt("point"),
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
        String sql = "INSERT INTO ptuAward.award (title, department_name, description, point, date_period, contact_info) " +
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
    public AwardEntity findAwardById(@PathVariable("id") int id) {
        String sql = "SELECT * FROM ptuAward.award WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new AwardEntity(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("department_name"),
                rs.getString("description"),
                rs.getInt("point"),
                rs.getString("date_period"),
                rs.getString("contact_info")
        ));
    }
}