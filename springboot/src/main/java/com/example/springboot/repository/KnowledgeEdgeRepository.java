package com.example.springboot.repository;

import com.example.springboot.entity.KnowledgeEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeEdgeRepository extends JpaRepository<KnowledgeEdge, Long> {

    List<KnowledgeEdge> findBySourceNodeIdOrTargetNodeId(Long sourceId, Long targetId);

    boolean existsBySourceNodeIdAndTargetNodeIdAndRelationType(Long sourceNodeId, Long targetNodeId, String relationType);

    @Query("""
        SELECT e FROM KnowledgeEdge e
        WHERE e.sourceNodeId IN :nodeIds AND e.targetNodeId IN :nodeIds
    """)
    List<KnowledgeEdge> findEdgesAmongNodes(@Param("nodeIds") List<Long> nodeIds);

    @Query("""
        SELECT e FROM KnowledgeEdge e
        WHERE e.sourceNodeId = :nodeId OR e.targetNodeId = :nodeId
    """)
    List<KnowledgeEdge> findEdgesByNodeId(@Param("nodeId") Long nodeId);

    List<KnowledgeEdge> findByRelationType(String relationType);
}
