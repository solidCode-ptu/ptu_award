package com.example.ptu.ptu_award.models;

public record AwardEntity (
        int id,
        String title,
        String department_name,
        String description,
        String point,
        String date_period,
        String contact_info
        ) {}