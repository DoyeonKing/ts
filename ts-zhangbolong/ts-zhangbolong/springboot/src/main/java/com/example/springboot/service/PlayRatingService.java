package com.example.springboot.service;

import com.example.springboot.entity.PlayRating;
import com.example.springboot.repository.PlayRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayRatingService {

    private final PlayRatingRepository ratingRepository;

    @Transactional
    public PlayRating saveOrUpdate(Long userId, Long playId, Integer rating, String content) {
        PlayRating r = ratingRepository.findByUserIdAndPlayId(userId, playId)
                .orElse(new PlayRating());
        r.setUserId(userId);
        r.setPlayId(playId);
        r.setRating(rating);
        r.setContent(content);
        return ratingRepository.save(r);
    }

    public List<Map<String, Object>> getByPlayId(Long playId, int page, int size) {
        return ratingRepository.findByPlayIdOrderByCreatedAtDesc(playId, PageRequest.of(page, size))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public Map<String, Object> getPlayScoreSummary(Long playId) {
        Double avg = ratingRepository.getAverageRatingByPlayId(playId);
        long count = ratingRepository.countByPlayId(playId);
        Map<String, Object> map = new HashMap<>();
        map.put("playId", playId);
        map.put("averageRating", avg != null ? Math.round(avg * 10) / 10.0 : null);
        map.put("ratingCount", count);
        return map;
    }

    /** 供 RecBole 等导出：user_id, item_id(play_id), rating */
    public List<PlayRating> findAllForExport() {
        return ratingRepository.findAll();
    }

    private Map<String, Object> toMap(PlayRating r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("userId", r.getUserId());
        m.put("playId", r.getPlayId());
        m.put("rating", r.getRating());
        m.put("content", r.getContent());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }
}
