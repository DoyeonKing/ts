package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockUserPreference {
    private Long userId;
    private List<String> likedGenres;
    private List<String> likedTags;
    private String favoriteActor;
    private int budgetMin;
    private int budgetMax;
    private List<Long> recentlyViewedPlayIds;
}
