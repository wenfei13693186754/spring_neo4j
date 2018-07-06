package com.neo4jStudy.dataflow.service;

import com.neo4jStudy.dataflow.model.EntInfo;

/**
 * @author xyf
 * @Data Created in 16:58 2018/6/11
 * @Descriptions
 */
public interface Neo4jService {
    void saveOrUpdateEntAndAssociaInfo(EntInfo entInfo);
}
