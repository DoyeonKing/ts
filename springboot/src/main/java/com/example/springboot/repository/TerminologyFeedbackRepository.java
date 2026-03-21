package com.example.springboot.repository;

import com.example.springboot.entity.TerminologyFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerminologyFeedbackRepository extends JpaRepository<TerminologyFeedback, Long> {

    Optional<TerminologyFeedback> findByUserIdAndTerminologyId(Long userId, Long terminologyId);
}
