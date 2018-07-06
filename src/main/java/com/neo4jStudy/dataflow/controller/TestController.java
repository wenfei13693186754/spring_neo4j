package com.neo4jStudy.dataflow.controller;

import com.alibaba.fastjson.JSON;
import com.neo4jStudy.dataflow.model.EntInfo;
import com.neo4jStudy.dataflow.service.Neo4jService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xyf
 * @Data Created in 15:14 2018/7/5
 * @Descriptions
 */
@RestController
public class TestController {

    @Autowired
    private Neo4jService neo4jService;

    @RequestMapping(value = "/test", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String test(@RequestBody String json) {
        neo4jService.saveOrUpdateEntAndAssociaInfo(JSON.parseObject(json, EntInfo.class));
        return "";
    }
}