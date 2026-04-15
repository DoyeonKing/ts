package com.example.springboot.repository;

import com.example.springboot.entity.KnowledgeNode;
import com.example.springboot.entity.KnowledgeNode.NodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeNodeRepository extends JpaRepository<KnowledgeNode, Long> {

    List<KnowledgeNode> findByNodeType(NodeType nodeType);

    List<KnowledgeNode> findByNameContaining(String keyword);

    Optional<KnowledgeNode> findByNameAndNodeType(String name, NodeType nodeType);

    @Query("SELECT n FROM KnowledgeNode n WHERE n.id IN :ids")
    List<KnowledgeNode> findByIds(@Param("ids") List<Long> ids);

    @Query("""
        SELECT DISTINCT n FROM KnowledgeNode n
        WHERE n.id IN (
            SELECT e.targetNodeId FROM KnowledgeEdge e WHERE e.sourceNodeId = :nodeId
            UNION
            SELECT e.sourceNodeId FROM KnowledgeEdge e WHERE e.targetNodeId = :nodeId
        )
    """)
    List<KnowledgeNode> findNeighbors(@Param("nodeId") Long nodeId);
}
