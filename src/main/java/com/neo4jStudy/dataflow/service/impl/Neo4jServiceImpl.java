package com.neo4jStudy.dataflow.service.impl;

import com.neo4jStudy.dataflow.model.EntBaseInfo;
import com.neo4jStudy.dataflow.model.EntInfo;
import com.neo4jStudy.dataflow.service.Neo4jService;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author xyf
 * @Data Created in 16:58 2018/6/11
 * @Descriptions
 */
public class Neo4jServiceImpl implements Neo4jService {

    private static final Log LOG = LogFactory.getLog(Neo4jService.class);

    @Autowired
    private Session session;

    private static Set<String> MAIN_POSITION = new HashSet<>();

    //静态代码块
    static{
        MAIN_POSITION.add("410A");
        MAIN_POSITION.add("410B");
        MAIN_POSITION.add("410C");
        MAIN_POSITION.add("410D");
        MAIN_POSITION.add("410E");
        MAIN_POSITION.add("410F");
        MAIN_POSITION.add("410G");
        MAIN_POSITION.add("410Z");
        MAIN_POSITION.add("430A");
        MAIN_POSITION.add("T509");
        MAIN_POSITION.add("T512");
        MAIN_POSITION.add("431A");
        MAIN_POSITION.add("431B");
        MAIN_POSITION.add("432A");
        MAIN_POSITION.add("432K");
        MAIN_POSITION.add("433A");
        MAIN_POSITION.add("433B");
        MAIN_POSITION.add("434Q");
        MAIN_POSITION.add("434R");
        MAIN_POSITION.add("436A");
        MAIN_POSITION.add("441A");
        MAIN_POSITION.add("441B");
        MAIN_POSITION.add("441C");
        MAIN_POSITION.add("441D");
        MAIN_POSITION.add("441E");
        MAIN_POSITION.add("441F");
        MAIN_POSITION.add("441G");
        MAIN_POSITION.add("442G");
        MAIN_POSITION.add("451D");
        MAIN_POSITION.add("490A");
        MAIN_POSITION.add("491A");
    }

    @Override
    public void saveOrUpdateEntAndAssociaInfo(EntInfo entInfo) {

        //1.企业信息查询
        queryEntAndAssosiaInfo(entInfo);
        //1.处理企业和法人信息
        entAndLegalInfoHandle(entInfo);
    }

     /**
       * @Data  Created in 18:27 2018/6/11
       * @Descriptions  根据新来数据查询数据库中企业信息
       * @param     entInfo 新来企业数据
       * @return
       */
    private void queryEntAndAssosiaInfo(EntInfo entInfo) {


    }

    /**
       * @Data  Created in 18:17 2018/6/11
       * @Descriptions  处理企业和法人信息
       * @param    entInfo 新来信息
       * @return
       */
    private void entAndLegalInfoHandle(EntInfo entInfo) {
        EntBaseInfo entBaseInfo = entInfo.getEntBaseInfo();
        HashMap<String, String> param = new HashMap<>();

    }
}
