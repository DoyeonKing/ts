package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockPlayProfile {
    private Long playId;
    private String playName;
    private String genre;
    private List<String> tags;
    private String leadActor;
    private double rating;
    private int heat;
    private int priceMin;
    private int priceMax;
}
