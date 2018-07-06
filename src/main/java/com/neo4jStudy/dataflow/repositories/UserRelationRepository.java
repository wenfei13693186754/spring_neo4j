package com.neo4jStudy.dataflow.repositories;

import com.neo4jStudy.dataflow.entities.nodes.PersonNode;
import com.neo4jStudy.dataflow.entities.relations.EntStaffRelationShip;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author xyf
 * @Data Created in 15:33 2018/6/11
 * @Descriptions
 */
public interface UserRelationRepository extends GraphRepository<EntStaffRelationShip> {
    @Query("MATCH (n:User) RETURN n ")
    List<PersonNode> getUserNodeList();

    @Query("create (n:User{age:{age},name:{name}}) RETURN n ")
    List<PersonNode> addUserNodeList(@Param("name") String name, @Param("age")int age);
}
