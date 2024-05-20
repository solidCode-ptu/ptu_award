package com.example.ptu.ptu_award.controller;

import com.example.ptu.ptu_award.models.BreakfastEntity;
import com.example.ptu.ptu_award.models.DetailBreakfast;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class Breakfast {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    public Breakfast(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/before-breakfast")
    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList("SELECT id, date, menu FROM sys.breakfast");
    }

    @GetMapping("/after-breakfast")
    public List<BreakfastEntity> afterFindAll() {
        String sql = "SELECT id, date, manu FROM sys.breakfast";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new BreakfastEntity(
                rs.getInt("id"),
                rs.getString("date"),
                rs.getString("menu")
        ));
    }


    @GetMapping("/insert-breakfast")
    public ResponseEntity<String> insertbfdata(){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            InputStream inputStream = getClass().getResourceAsStream("/Breakfast_May.json");

            if (inputStream == null) {
                throw new FileNotFoundException("Breakfast_May.json file not found");
            }

            List<Map<String,Object>> jsonData = objectMapper.readValue(inputStream, List.class);

            for (Map<String, Object> data : jsonData){
                String sql = "INSERT INTO sys.breakfast (date, menu)" +
                        "VALUES ( ?, ? )";
                jdbcTemplate.update(sql,
                        data.get("요일"),
                        data.get("메뉴")
                );
            }

            return ResponseEntity.ok("Breakfast Data inserted successfull");
        } catch (IOException e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading JSON file");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inserting data");
        }
    }

    @GetMapping("/breakfast/{id}")
    public DetailBreakfast findbreakfastById(@PathVariable("id") int id) {
        String sql = "SELECT date, menu FROM sys.breakfast WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new DetailBreakfast(
                rs.getString("date"),
                rs.getString("menu")
        ));
    }

    @GetMapping("/get-json")
    public ResponseEntity<Resource> getJsonFile() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:csvjson.json");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .body(resource);
    }

    @GetMapping("/today-menu")
    public List<Map<String, Object>> findtodayMenu(){
        LocalDate now = LocalDate.now();
        String day = String.valueOf(now.getDayOfMonth());
        List<Map<String, Object>> menuList = jdbcTemplate.queryForList("SELECT date,menu FROM sys.breakfast WHERE date LIKE \'% "+day+"일%\'");

        for (Map<String, Object> row : menuList) {
            // "메뉴" 키의 값이 없거나 빈 문자열이면 "미지정"으로 설정
            String menu = (String) row.get("menu");
            if (menu == "" || menu.trim().isEmpty()) {
                row.put("menu", "미지정");
            }
        }
       return menuList;
    }

    @GetMapping("/thisweek-menu")
    public List<Map<String, Object>> findthisweekMenu(){
        LocalDate now = LocalDate.now();
        LocalDate monday = now.with(DayOfWeek.MONDAY);
        LocalDate friday = now.with(DayOfWeek.FRIDAY);
        String menuQuery = "SELECT date, menu FROM sys.breakfast WHERE id BETWEEN (SELECT id FROM sys.breakfast WHERE date LIKE \'% " +monday.getDayOfMonth()+
                "일%\') AND (SELECT id FROM sys.breakfast WHERE date LIKE \'% " + friday.getDayOfMonth()+
                "일%\')";
        List<Map<String, Object>> menuList = jdbcTemplate.queryForList(menuQuery);

        for (Map<String, Object> row : menuList) {
            // "메뉴" 키의 값이 없거나 빈 문자열이면 "미지정"으로 설정
            String menu = (String) row.get("menu");
            if (menu == "" || menu.trim().isEmpty()) {
                row.put("menu", "미운영");
            }
        }
        return menuList;
    }

    @GetMapping("/weekly-menu")
    public List<Map<String, Object>> showWeeklyMenu(){
        LocalDate now = LocalDate.now();
        LocalDate end = now.with(DayOfWeek.SUNDAY);
        int startDay = now.getDayOfMonth();
        int endDay = end.getDayOfMonth();
        String sql = "SELECT date, menu FROM sys.breakfast WHERE id BETWEEN (SELECT id FROM sys.breakfast WHERE date LIKE \'% " +startDay+ "일%\') AND (SELECT id FROM sys.breakfast WHERE date LIKE \'% " + endDay+ "일%\')";
        List<Map<String, Object>> menuList = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : menuList) {
            String menu = (String) row.get("menu");
            if (menu == "" || menu.trim().isEmpty()) {
                row.put("menu", "미운영");
            }
        }


        return menuList;
    }


}
