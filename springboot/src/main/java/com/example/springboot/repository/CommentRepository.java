package com.example.springboot.repository;

import com.example.springboot.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
            String targetType, Long targetId, Pageable pageable);

    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
