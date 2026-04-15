package com.example.springboot.service;

import com.example.springboot.entity.Comment;
import com.example.springboot.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public Comment create(Long userId, String targetType, Long targetId, Long parentId, String content) {
        Comment c = new Comment();
        c.setUserId(userId);
        c.setTargetType(targetType);
        c.setTargetId(targetId);
        c.setParentId(parentId);
        c.setContent(content);
        return commentRepository.save(c);
    }

    /** 分页查一级评论，并带上每条评论的楼中楼（回复） */
    public List<Map<String, Object>> getByTarget(String targetType, Long targetId, int page, int size) {
        List<Comment> roots = commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
                targetType, targetId, PageRequest.of(page, size));
        if (roots.isEmpty()) return List.of();

        List<Long> rootIds = roots.stream().map(Comment::getId).toList();
        List<Comment> allReplies = rootIds.stream()
                .flatMap(pid -> commentRepository.findByParentIdOrderByCreatedAtAsc(pid).stream())
                .toList();
        Map<Long, List<Map<String, Object>>> replyMap = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId,
                        Collectors.mapping(this::toMap, Collectors.toList())));

        return roots.stream().map(r -> {
            Map<String, Object> m = toMap(r);
            m.put("replies", replyMap.getOrDefault(r.getId(), List.of()));
            return m;
        }).toList();
    }

    public long countByTarget(String targetType, Long targetId) {
        return commentRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    private Map<String, Object> toMap(Comment c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("userId", c.getUserId());
        m.put("targetType", c.getTargetType());
        m.put("targetId", c.getTargetId());
        m.put("parentId", c.getParentId());
        m.put("content", c.getContent());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }
}
