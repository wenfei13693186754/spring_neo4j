package com.neo4jStudy.dataflow.repositories;

import com.neo4jStudy.dataflow.config.Neo4jConfig;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/*******************************************************************************
 * - Copyright (c)  2018  chinadaas.com
 * - File Name: GenealogyService
 * - @Author: Guo Guangjun
 * - Description:
 *      关联洞察neo4j查询
 *
 * - Function List:
 * 1.entGenealogy 企业全貌族谱查询
 * 2.entControlPath 企业控制路径查询
 * 3.entRealRelation 企业实质关联查询
 * 4.entRiskgraph 企业关联风险视图查询
 * 5.entMultippleNodesRelation 企业多节点关联关系查询
 * 6.getZsidByCode 根据人员标示码查询zsid
 * 7.superCorporationDiscern 集团母公司识别
 * 8.superCorporationMembers 集团母公司成员
 * 9.entControlPathWithAllRelation 企业控制路径查询（投资和对外投资关系）
 * 10.entControlPath4EntOutInvest 企业控制路径：仅查询企业对外投资控股路径
 * 11.entControlPathWithoutEntOutInvest  企业控制路径：仅查询投资主体企业的控股路径
 * 12.getIds 根据zsid查出节点id
 *
 * - History:
 * Date         Author          Modification
 * 2018/5/9     Guo Guangjun    Create the current class
 * 2018/5/17    Guo Guangjun    add method entControlPath
 * 2018/5/17    Guo Guangjun    add method entRealRelation
 * 2018/5/17    Guo Guangjun    delete method getThisEnt
 * 2018/5/18    Guo Guangjun    delete method entRiskgraph
 * 2018/5/18    Seven Liu       Add method entMultipleNodesRelation,getZsidByCode
 * 2018/5/22    Guo Guangjun    add method queryEntUltimateControlNodeId
 * 2018/5/22    Guo Guangjun    add method entControlPathWithAllRelation
 * 2018/5/31    Guo Guangjun    add method entControlPath4EntOutInvest and entControlPathWithoutEntOutInvest
 * 2018/5/31    Guo Guangjun    Add method superCorporationDiscern and superCorporationMembers
 * 2018/6/1     Seven Liu       add method getIds
 *
 *******************************************************************************/
@Repository
public class Neo4jRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jRepository.class);

    @Value("${query-neo4j-max-wait-time.sync:20}")
    private Long syncMaxWaitTime;

    @Value("${query-neo4j-max-wait-time.async:120}")
    private Long asyncMaxWaitTime;

    /**
     * cypher查询语句中返回数据的通用部分：要求整改查询路径的名字为a
     */
    private static final String COMMON_RETURN_CYPHER = " unwind nodes(a) as n unwind relationships(a) as rel with collect(distinct n) as node, collect(distinct rel) as link return node, link";

    @Autowired
    private Neo4jConfig neo4jConfig;

    /**
     * 查询企业全貌族谱逻辑：
     * 1.任职：查询主体企业直接相关的1层任职和法人信息，如果对应的高管或者法人与其他企业有直接的任职或者法人关系，则同时查询出该信息。
     * 2.疑似：查询和主体企业有直接疑似关系的信息
     * 3.投资：查询投资关系N层关系信息
     *
     * @param zsid      主体企业zsid
     * @param parameter 查询条件
     * @return 符合条件的节点和关系信息
     */
    public List<Record> entGenealogy(String zsid, GenealogyParameter parameter) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = "";
        List<RelationsEnum> relations = parameter.getRelations();
        // 如果传入了任职关系
        if (relations.contains(RelationsEnum.manager)) {
            cypher += " match path1 = (s:ent{zsid:$zsid})<-[:staff|:legal]-(m) " +
                    "   optional match path2 = (m)-[:staff|:legal]->(e:ent) where e.entstatus = '1' " +
                    "   with collect(path1) + collect(path2) as path unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了疑似地址和疑似电话
        String relationStr = "";
        if (relations.contains(RelationsEnum.samedom) && relations.contains(RelationsEnum.sametel)) {
            relationStr = ":entaddr|:enttel ";
        } else if (relations.contains(RelationsEnum.samedom)) {
            relationStr = ":entaddr ";
        } else if (relations.contains(RelationsEnum.sametel)) {
            relationStr = ":enttel ";
        }
        if (StringUtil.isNotBlank(relationStr)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += " match a = (s:ent{zsid:$zsid})-[" + relationStr + "]->(m)<-[" + relationStr + "]-(e:ent) where e.entstatus = '1' " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了投资
        if (relations.contains(RelationsEnum.invest)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += " match a = (s:ent{zsid:$zsid})-[r:teninvmerge *0.." + parameter.getLayer() + "]-(e:ent) where e.entstatus = '1' " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path unwind path as a " + COMMON_RETURN_CYPHER +
                    "   union match a = (s:ent{zsid:$zsid})-[r:teninvmerge *0.." + parameter.getLayer() + "]-(e:person) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了管理人员
        if (relations.contains(RelationsEnum.personManager)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += " match path1 = (s:ent{zsid:$zsid})<-[:staff]-(m) " +
                    "   optional match path2 = (m)-[:staff]->(e:ent) where e.entstatus = '1' " +
                    "   with collect(path1) + collect(path2) as path unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了企业股东
        if (relations.contains(RelationsEnum.entShareholder)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += " match a = (s:ent{zsid:$zsid})<-[r:teninvmerge *0.." + parameter.getLayer() + "]-(e:ent) where e.entstatus = '1' " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了自然人股东
        if (relations.contains(RelationsEnum.personShareholder)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += " match a = (s:ent{zsid:$zsid})<-[r:teninvmerge *0.." + parameter.getLayer() + "]-(e:person) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了企业对外投资
        if (relations.contains(RelationsEnum.entOutInvest)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += " match a = (s:ent{zsid:$zsid})-[r:teninvmerge *0.." + parameter.getLayer() + "]->(e:ent) where e.entstatus = '1' " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }

        LOGGER.debug("全貌族谱查询cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, parameter.getAsync());
    }


    /**
     * 企业控制路径查询逻辑：
     * 根据传入的关系去查询主体企业的对外投资或者投资主体企业的股东信息
     *
     * @param zsid      主体企业zsid
     * @param parameter 查询参数
     * @return 符合条件的节点和关系信息
     */
    public List<Record> entControlPath(String zsid, ControlPathParameter parameter) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = "";
        Set<RelationsEnum> relations = parameter.getRelations();
        // 如果传入了企业对外投资关系
        if (relations.contains(RelationsEnum.entOutInvest)) {
            cypher += "match a=(s:ent{zsid:$zsid})-[r:hold *]->(e:ent) where e.entstatus = '1' " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了企业股东
        if (relations.contains(RelationsEnum.entShareholder)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += "match a=(s:ent{zsid:$zsid})<-[r:hold *]-(e:ent) where e.entstatus = '1' " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了人员股东
        if (relations.contains(RelationsEnum.personShareholder)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += "match a=(s:ent{zsid:$zsid})<-[r:hold *]-(p:person) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        LOGGER.debug("企业控制路径查询cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, parameter.getAsync());
    }

    /**
     * 企业控制路径：仅查询企业对外投资控制路径
     *
     * @param zsid 主体企业zsid
     * @return
     */
    public List<Record> entControlPath4EntOutInvest(String zsid, boolean async) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = "match a=(s:ent{zsid:$zsid})-[r:hold *]->(e:ent) where e.entstatus = '1' " +
                "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                "       unwind path as a " + COMMON_RETURN_CYPHER;

        LOGGER.debug("企业控制路径查询-企业对外投资cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, async);
    }

    /**
     * 企业控制路径：查询投资主体企业的股东信息
     *
     * @param zsid      主体企业zsid
     * @param parameter 查询参数
     * @return 符合条件的节点和关系信息
     */
    public List<Record> entControlPathWithoutEntOutInvest(String zsid, ControlPathParameter parameter) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = "";
        Set<RelationsEnum> relations = parameter.getRelations();
        // 如果传入了企业股东
        if (relations.contains(RelationsEnum.entShareholder)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += "match a=(s:ent{zsid:$zsid})<-[r:hold *]-(e:ent) where e.entstatus = '1' " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        // 如果传入了人员股东
        if (relations.contains(RelationsEnum.personShareholder)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += "match a=(s:ent{zsid:$zsid})<-[r:hold *]-(p:person) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }
        LOGGER.debug("企业控制路径-查询投资主体企业的股东信息cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, parameter.getAsync());
    }

    /**
     * 企业控制路径查询（投资和对外投资）：查询主体企业的对外投资和投资主体企业的股东信息
     *
     * @param zsid  主体企业zsid
     * @param layer 查询层级
     * @return 符合条件的节点和关系信息
     */
    public List<Record> entControlPathWithAllRelation(String zsid, int layer, boolean async) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = " match a=(s:ent{zsid:$zsid})-[r:hold *0.." + layer + "]->(e:ent) " + COMMON_RETURN_CYPHER +
                " union match a=(s:ent{zsid:$zsid})<-[r:hold *0.." + layer + "]-(end) " + COMMON_RETURN_CYPHER;
        LOGGER.debug("企业控制路径（投资和对外投资）查询cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, async);
    }

    /**
     * 企业实质关联查询逻辑：
     * 1.主体企业的母公司：企业控股股东
     * 2.主体企业的子公司：企业控股对外投资
     * 3.与主体企业受同一母公司控制的其他企业
     * 4.与主体企业的直接控股自然人、法人、董高同一地址的关联人员及其控制公司
     * 5.满足一定条件的参股股东:对目标企业出资比例大于20%但无法形成控股或共同决策的股东，且在目标企业中担任或派驻相关管理人员。
     * 6.该企业最终实际控制人同一地址关联人及其控制公司
     *
     * @param zsid      主体企业zsid
     * @param parameter 查询参数
     * @return 符合条件的节点和关系信息
     */
    public List<Record> entRealRelation(String zsid, RealRelationParameter parameter) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        // 1.主体企业的母公司：企业控股股东
        String cypher = "match a=(s:ent{zsid:$zsid})<-[:tenholdmerge *0.." + parameter.getLayer() + "]-(e:person) " +
                "           with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                "           unwind path as a " + COMMON_RETURN_CYPHER +
                "       union match a=(s:ent{zsid:$zsid})<-[:tenholdmerge *0.." + parameter.getLayer() + "]-(e:ent)  where e.entstatus = '1' " +
                "           with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                "           unwind path as a " + COMMON_RETURN_CYPHER +

                // 2.主体企业的子公司：企业控股对外投资
                "       union match a=(s:ent{zsid:$zsid})-[:tenholdmerge *0.." + parameter.getLayer() + "]->(e:ent) where e.entstatus = '1' " +
                "           with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                "           unwind path as a " + COMMON_RETURN_CYPHER +

                // 3.与主体企业受同一母公司控制的其他企业
                "       union match a=(s:ent{zsid:$zsid})<-[:tenholdmerge]-(end:ent)-[:tenholdmerge]->(e:ent) where ()-[:teninvmerge]->(end) and e.entstatus = '1' " +
                "           with filter (p in collect(a) where all(n in nodes(p) where n.entstatus = '1')) as path " +
                "           unwind path as a " + COMMON_RETURN_CYPHER +
                "       union match a=(s:ent{zsid:$zsid})<-[:tenholdmerge]-(end:person)-[:tenholdmerge]->(e:ent) where e.entstatus = '1' " +
                "           with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                "           unwind path as a " + COMMON_RETURN_CYPHER +

                // 4.与主体企业的直接控股自然人、法人、董高同一地址的关联人员及其控制公司
                "       union match path1 = (s:ent{zsid:$zsid})<-[:staff|:tenholdmerge|:legal]-(p1:person) " +
                "           optional match path2 = (p1)-[:peraddr]->()<-[:peraddr]-(p2:person) " +
                "           optional match path3 = (p2)-[:tenholdmerge|:staff]->(e:ent) where e.entstatus = '1' " +
                "           with collect(path1) + collect(path2) + collect(path3) as path unwind path as a " + COMMON_RETURN_CYPHER +

                // 5.满足一定条件的参股股东
                "       union match a=(s:ent{zsid:$zsid})<-[:join]-(:person)-[:staff]->(e:ent{zsid:$zsid}) " + COMMON_RETURN_CYPHER +

                // 6.该企业最终实际控制人同一地址关联人及其控制公司
                "       union match path1 = (s:ent{zsid:$zsid})<-[:hold *]-(end) where not (end)<-[:hold]-() and labels(end)[0] = 'person' " +
                "           optional match path2 = (end)-[:peraddr]->()<-[:peraddr]-(p1:person) " +
                "           optional match path3= (p1)-[:tenholdmerge|:staff]->(e:ent) where e.entstatus = '1' " +
                "           with collect(path1) + collect(path2) + collect(path3) as path unwind path as a " + COMMON_RETURN_CYPHER;

        LOGGER.debug("企业实质关联查询cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, parameter.getAsync());
    }

    /**
     * 查询企业关联风险视图逻辑：
     * 1.任职：查询主体企业直接相关的1层任职和法人信息，如果对应的高管或者法人与其他企业有直接的任职或者法人关系，则同时查询出该信息。
     * 2.疑似：查询和主体企业有直接疑似关系的信息
     * 3.投资：查询投资关系N层关系信息，以及对应节点的风险系数信息
     *
     * @param zsid      主体企业zsid
     * @param parameter 查询条件
     * @return 符合条件的节点和关系信息
     */
    public List<Record> entRiskgraph(String zsid, RiskGraphParameter parameter) {
        StringBuilder commonCypher = new StringBuilder(" unwind tmp_nodes as nodes ")
                .append(" unwind tmp_rels as rels ")
                .append(" unwind tmp_moreinfo as moreinfo ")
                .append(" with collect(distinct nodes) as node, collect(distinct rels) as link, collect(distinct moreinfo) as moreinfo")
                .append(" return node, link, moreinfo ");

        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        StringBuilder cypher = new StringBuilder("");
        List<RelationsEnum> relations = parameter.getRelations();
        // 如果传入了任职关系
        if (relations.contains(RelationsEnum.manager)) {
            // 查询主体企业的法人或者高管
            cypher.append("match a=(n:ent{zsid:$zsid})<-[r:legal|:staff]-(p:person) ")
                    .append(prepareManagerRiskscoreOnlyPrimaryEnt(commonCypher));

            // 查询主体企业的法人或者高管且担任其他公司的法人或者高管
            cypher.append("union  match a=(n:ent{zsid:$zsid})<-[r:legal|:staff]-(p:person)-[ro:legal|:staff]->(other:ent) where other.entstatus = '1' ")
                    .append(prepareManagerRiskscore(commonCypher));
        }
        // 如果传入了管理人员
        if (relations.contains(RelationsEnum.personManager)) {
            if (StringUtil.isNotBlank(cypher.toString())) {
                cypher.append(" union ");
            }
            // 查询主体企业的高管
            cypher.append("match a=(n:ent{zsid:$zsid})<-[r:staff]-(p:person) ")
                    .append(prepareManagerRiskscoreOnlyPrimaryEnt(commonCypher));

            // 查询主体企业的高管且担任其他公司的高管
            cypher.append("union  match a=(n:ent{zsid:$zsid})<-[r:staff]-(p:person)-[ro:staff]->(other:ent) where other.entstatus = '1' ")
                    .append(prepareManagerRiskscore(commonCypher));
        }

        // 如果传入了疑似地址和疑似电话
        String relationStr = "";
        if (relations.contains(RelationsEnum.samedom) && relations.contains(RelationsEnum.sametel)) {
            relationStr = ":entaddr|:enttel";
        } else if (relations.contains(RelationsEnum.samedom)) {
            relationStr = ":entaddr";
        } else if (relations.contains(RelationsEnum.sametel)) {
            relationStr = ":enttel";
        }
        if (StringUtil.isNotBlank(relationStr)) {
            if (StringUtil.isNotBlank(cypher.toString())) {
                cypher.append(" union ");
            }
            cypher.append(" match a=(n:ent{zsid:$zsid})-[r1:enttel|:entaddr]->(m)<-[r2:enttel|:entaddr]-(other:ent) where other.entstatus = '1' ")
                    .append(" with nodes(a) as tmp_nodes, relationships(a) as tmp_rels, m,case when n.industryphy=other.industryphy then 0.5 else 0.3 end as score, other,id(other) as entid ")
                    .append(" with tmp_nodes, tmp_rels, collect({id:entid, sameriskscore:score}) as tmp_moreinfo ")
                    .append(commonCypher);
        }

        // 如果传入了投资
        if (relations.contains(RelationsEnum.invest)) {
            if (StringUtil.isNotBlank(cypher.toString())) {
                cypher.append(" union ");
            }
            cypher.append(" match a=(start:ent{zsid:$zsid})-[inv:teninvmerge*0..").append(parameter.getLayer()).append("]-(end:ent) where end.entstatus = '1' ")
                    .append(" with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path, inv as inv, end as end unwind path as a ")
                    .append(prepareInvestRiskscore(commonCypher))
                    .append(" union match a=(start:ent{zsid:$zsid})-[inv:teninvmerge*0..").append(parameter.getLayer()).append("]-(end:person) ")
                    .append(" with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path, inv as inv, end as end unwind path as a ")
                    .append(prepareInvestRiskscore(commonCypher));
        }
        // 如果传入了企业股东
        if (relations.contains(RelationsEnum.entShareholder)) {
            if (StringUtil.isNotBlank(cypher.toString())) {
                cypher.append(" union ");
            }
            cypher.append(" match a=(start:ent{zsid:$zsid})<-[inv:teninvmerge*0..").append(parameter.getLayer()).append("]-(end:ent) where end.entstatus = '1' ")
                    .append(" with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path, inv as inv, end as end  unwind path as a ")
                    .append(prepareInvestRiskscore(commonCypher));
        }
        // 如果传入了自然人股东
        if (relations.contains(RelationsEnum.personShareholder)) {
            if (StringUtil.isNotBlank(cypher.toString())) {
                cypher.append(" union ");
            }
            cypher.append(" match a=(start:ent{zsid:$zsid})<-[inv:teninvmerge*0..").append(parameter.getLayer()).append("]-(end:person) ")
                    .append(" with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path , inv as inv, end as end unwind path as a  ")
                    .append(prepareInvestRiskscore(commonCypher));
        }
        // 如果传入了企业对外投资
        if (relations.contains(RelationsEnum.entOutInvest)) {
            if (StringUtil.isNotBlank(cypher.toString())) {
                cypher.append(" union ");
            }
            cypher.append(" match a=(start:ent{zsid:$zsid})-[inv:teninvmerge*0..").append(parameter.getLayer()).append("]->(end:ent) where end.entstatus = '1' ")
                    .append(" with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path, inv as inv, end as end  unwind path as a ")
                    .append(prepareInvestRiskscore(commonCypher));
        }

        LOGGER.debug("全貌关联风险视图查询cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher.toString(), params, parameter.getAsync());
    }

    /**
     * 准备风险视图管理关系的风险系数计算cypher语句：仅包含主体企业的法人或者高管
     *
     * @param commonCypher
     * @return
     */
    private String prepareManagerRiskscoreOnlyPrimaryEnt(StringBuilder commonCypher) {
        return " with nodes(a) as tmp_nodes, relationships(a) as tmp_rels,  r, r.riskscore as pscore, p, id(p) as personid " +
                " with tmp_nodes, tmp_rels, collect({id:personid, managerriskscore: pscore}) as tmp_moreinfo " +
                commonCypher;
    }

    /**
     * 准备风险视图管理关系的风险系数计算cypher语句：主体企业的法人或者高管且在外担任法人或者高管
     *
     * @param commonCypher
     * @return
     */
    private String prepareManagerRiskscore(StringBuilder commonCypher) {
        return " with nodes(a) as tmp_nodes, relationships(a) as tmp_rels, r, ro, r.riskscore as pscore, p, toFloat(r.riskscore)*toFloat(ro.riskscore) as otherscore, other, id(p) as personid, id(other) as entid " +
                " with tmp_nodes, tmp_rels, collect({id:personid, managerriskscore: pscore})+collect({id:entid, managerriskscore:otherscore}) as tmp_moreinfo " +
                commonCypher;
    }

    /**
     * 准备风险视图投资关系的风险系数计算cypher语句
     *
     * @param commonCypher
     * @return
     */
    private String prepareInvestRiskscore(StringBuilder commonCypher) {
        return " with nodes(a) as tmp_nodes, relationships(a) as tmp_rels, " +
                "   reduce(final=0.0, e in collect(reduce(onePath=1.0, " +
                "       n IN inv|toFloat(onePath)*(case when n.conprop is not null then toFloat(n.conprop) else  toFloat(n.holderrto)/100 end)))|final+e) as invriskscore, " +
                "   id(end) as endid " +
                " with tmp_nodes, tmp_rels, collect({id:endid, invriskscore: invriskscore}) as tmp_moreinfo " +
                commonCypher;
    }

    /**
     * 企业多节点关联关系查询逻辑：
     * 查询输入节点之间N层之内可以关联到的最短路径
     *
     * @param zsids
     * @param parameter
     * @return
     */
    public List<Record> entMultipleNodesRelation(List<String> zsids, MultipleNodesRelationParameter parameter) {
        //根据zsid查出所有节点id
        Set<Long> ids = getIds(zsids);
        Map<String, Object> params = new HashMap<>(1);
        params.put("ids", ids);
        StringBuilder cypher = new StringBuilder();
        Set<RelationsEnum> relations = parameter.getRelations();
        StringBuilder relationStr = new StringBuilder();
        //追加投资关系
        if (relations.contains(RelationsEnum.invest)) {
            relationStr.append(":teninvmerge|");
        }
        //追加任职关系
        if (relations.contains(RelationsEnum.manager)) {
            relationStr.append(":staff|:legal|");
        }
        //追加办公地址疑似关联
        if (relations.contains(RelationsEnum.samedom)) {
            relationStr.append(":entaddr|");
        }
        //追加电话疑似关联
        if (relations.contains(RelationsEnum.sametel)) {
            relationStr.append(":enttel|");
        }
        relationStr.deleteCharAt(relationStr.length() - 1);

        cypher.append("match (e) where id(e) in $ids ")
                .append("with collect(e) as nodes ")
                .append("unwind nodes as j ")
                .append("unwind nodes as k ")
                .append("with * where id(j) < id(k) ")
                .append("match a = allShortestPaths((j)-[" + relationStr + "*.." + parameter.getLayer() + "]-(k)) ")
                .append("with min(length(a)) as minPath,collect(a) as path ")
                .append("with filter(p in path where length(p)=minPath) as t3, path,minPath ")
                .append("with extract(s in t3|[id(nodes(s)[0]),id(nodes(s)[-1]),minPath]) as short,path ")
                .append("with short, reduce(ns = [], p in path|ns+nodes(p)) as tmpNodes,reduce(rels = [], p in path|rels+relationships(p)) as tmpRelations ")
                .append("unwind tmpNodes as tmpNodes1 ")
                .append("unwind tmpRelations as tmpRelations1 ")
                .append("return  short,  collect(distinct tmpNodes1) as node ,collect(distinct tmpRelations1) as link");

        LOGGER.debug("企业多节点关联关系查询cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher.toString(), params, parameter.getAsync());

    }

    /**
     * 根据人员标示码查询zsid
     *
     * @param palgorithmid
     * @return
     */
    public Map<String, String> getZsidByCode(String palgorithmid) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("palgorithmid", palgorithmid);
        String cypher = "match (n:person) where n.encode_v1 = $palgorithmid return n.zsid";

        List<Record> recordList = executeQuery(cypher, params, false);
        Map<String, String> zsid = new HashMap<>(1);
        if (recordList != null && !recordList.isEmpty()) {
            zsid.put("zsid", recordList.get(0).get("n.zsid").asString());
            zsid.put("mk", palgorithmid);

        }
        return zsid;
    }


    /**
     * 查询主体企业的最终控制节点的id列表
     *
     * @param zsid  主体企业zsid
     * @param layer 查询层级
     * @return
     */
    public Set<Long> queryEntUltimateControlNodeId(String zsid, int layer, boolean async) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = "match (s:ent{zsid:$zsid})<-[:hold *0.." + layer + "]-(end) where not (end)<-[:hold]-() with collect(id(end)) as endid return endid";

        Set<Long> idSet = new HashSet<>(12);
        List<Record> recordList = executeQuery(cypher, params, async);
        if (recordList != null && !recordList.isEmpty()) {
            recordList.get(0).get("endid").asList().forEach(item -> {
                idSet.add((Long) item);
            });
        }
        return idSet;
    }

    /**
     * 查询主体企业的最终控制节点的id列表
     *
     * @param zsid 主体企业zsid
     * @return
     */
    public Set<Long> queryEntUltimateControlNodeId(String zsid, boolean async) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = "match (s:ent{zsid:$zsid})<-[:hold *]-(end) where not (end)<-[:hold]-() with collect(id(end)) as endid return endid";
        Set<Long> idSet = new HashSet<>(12);
        List<Record> recordList = executeQuery(cypher, params, async);
        if (recordList != null && !recordList.isEmpty()) {
            recordList.get(0).get("endid").asList().forEach(item -> {
                idSet.add((Long) item);
            });
        }
        return idSet;
    }

    /**
     * 根据节点id列表查询投资或者对外投资的1层关系
     *
     * @param nodeIds   节点id列表
     * @param direction 投资方向：true：对外投资；false：投资该节点
     * @return
     */
    public List<Record> queryInvRelationByNodeIds(List<Long> nodeIds, boolean direction, boolean async) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("ids", nodeIds);
        String relation = direction ? "-[:inv]->" : "<-[:inv]-";
        String cypher = "match a=(start)" + relation + "(end) where id(start) in $ids " +
                " with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                " unwind path as a " + COMMON_RETURN_CYPHER;

        LOGGER.debug("根据节点id列表查询投资或者对外投资的1层关系cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, async);
    }

    /**
     * 根据zsid 查出ent节点和person节点的id
     *
     * @param zsid
     */
    public Set<Long> getIds(List<String> zsid) {
        Set<Long> ids = new HashSet<>();
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);

        List<Record> recordList = executeQuery("MATCH (e:ent) where e.zsid in $zsid return id(e) as id", params, false);
        if (recordList != null && !recordList.isEmpty()) {
            recordList.forEach(record -> {
                ids.add(record.get("id").asLong());
            });

        }
        recordList = executeQuery("MATCH (n:person) where n.zsid in $zsid return id(n) as id", params, false);
        if (recordList != null && !recordList.isEmpty()) {
            recordList.forEach(record -> {
                ids.add(record.get("id").asLong());
            });
        }
        return ids;

    }

    /**
     * 企业集团母公司识别逻辑：
     * 1.找出输入企业的控制路径倒数第2层的节点，判断是否满足母公司判断条件
     * 2.如果不是，则查询输入企业是否满足母公司判断条件
     *
     * @param zsid 主体企业zsid
     * @return
     */
    public Map<String, Object> superCorporationDiscern(String zsid, SuperCorporationParameter parameter) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);

        // 1.查询出主体企业的母公司的zsid
        String cypher = "match (s:ent{zsid:$zsid})<-[:hold *]-(target)<-[:hold]-(end) where not (end)<-[:hold]-() and labels(target)[0] = 'ent' with collect(target.zsid) as zsid return zsid ";
        List<Record> recordList = executeQuery(cypher, params, parameter.getAsync());
        Set<String> zsidSet = new HashSet<>(2);
        if (recordList != null && !recordList.isEmpty()) {
            recordList.get(0).get("zsid").asList().forEach(item -> {
                zsidSet.add((String) item);
            });
        }
        if (zsidSet.isEmpty() || zsidSet.size() > 1) {
            // 3.查询输入企业是否满足集团母公司条件
            return superParentCorporationByZsid(zsid, parameter);
        }

        // 2.根据找出的zsid去判断对应的企业是否满足集团母公司的条件。如果不满足，则查询输入企业是否满足母公司4个条件
        Map<String, Object> map = superParentCorporationByZsid(zsidSet.iterator().next(), parameter);
        return map != null ? map : superParentCorporationByZsid(zsid, parameter);
    }

    /**
     * 根据企业zsid识别该企业是否一个集团母公司
     *
     * @param zsid
     * @return
     */
    private Map<String, Object> superParentCorporationByZsid(String zsid, SuperCorporationParameter parameter) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        String cypher = "match a=(s:ent{zsid:$zsid})-[r:hold]->(e:ent) " +
                " with s as s," +
                "      toFloat(s.regcap) + reduce(fa=0.0, n in collect(e) | fa + case when n.regcap is not null then toFloat(n.regcap) else 0.0 end) as total_regcap, " +
                "      count(distinct e) as sub_count " +
                " where toFloat(s.regcap) >=  " + parameter.getRegcap() +
                " and not (s.enttype =~'2.*' and s.enttype =~'7.*' and s.enttype =~'9.*' and s.enttype =~'43.*' " +
                "       and s.enttype =~'45.*' and s.enttype =~'54.*' and s.enttype =~'58.*' and s.enttype =~'64.*' and s.enttype =~'68.*') " +
                " and sub_count >=  " + parameter.getSubCount() +
                " and toFloat(total_regcap) >=  " + parameter.getTotalRegcap() +
                " return s ";

        Map<String, Object> map = new HashMap<>();
        List<Record> recordList = executeQuery(cypher, params, parameter.getAsync());
        if (recordList != null && !recordList.isEmpty()) {
            NodeValue node = (NodeValue) recordList.get(0).get("s");
            Map<String, Object> propertyMap = node.asMap();
            map.put("entname", propertyMap.get("name"));
            map.put("creditcode", propertyMap.get("creditcode"));
            map.put("regno", propertyMap.get("regno"));
        }

        LOGGER.debug("集团母公司识别cypher:{}, params:{}", cypher, params);
        return map.isEmpty() ? null : map;
    }

    /**
     * 集团族谱成员
     * 1.企业对外投资：母公司对外投资公司
     * 2.企业对外控股：该集团公司对外控股投资公司（控股穿透）以及控制路径上的每一个子公司的控制节点（为企业）的对外控股公司
     * 3.人员对外控股：该集团母公司的最终控制人对外控股公司以及该集团母公司和子公司的法人、高管对外控股公司
     * 4.人员对外任职：该集团公司和子公司法人、高管对外任职公司
     * 5.人员地址疑似：与该集团母公司的最终控制人使用同一地址关联自然人对外控股公司以及与该集团母公司和子公司法人、高管使用同一地址关联自然人对外控股公司
     *
     * ***
     * 第2条说明：意思是母公司控股的子公司，一层层穿透
     * 第5条说明：
     *
     * @param zsid 集团母公司zsid
     * @return
     */
    public List<Record> superCorporationMembers(String zsid, SuperCorporationMemberParameter parameter) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("zsid", zsid);
        List<RelationsEnum> relations = parameter.getRelations();
        String cypher = "";

        // 1.企业对外投资：母公司对外投资公司
        if (relations.contains(RelationsEnum.invest)) {
            cypher += " match a=(s:ent{zsid:$zsid})-[:inv]->(e:ent) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }

        // 2.企业对外控股：该集团公司对外控股投资公司（控股穿透）以及控制路径上的每一个子公司的控制节点（为企业）的对外控股公司
        if (relations.contains(RelationsEnum.entOutHold)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += " match path1=(s:ent{zsid:$zsid})-[:hold *]->(e:ent) " +
                    "       optional match path2=(e)<-[:hold]-(:ent)-[:hold]->(:ent) " +
                    "       with collect(path1) + collect(path2) as path unwind path as a " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where n.entstatus='1')) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }

        // 3.人员对外控股：该集团母公司的最终控制人对外控股公司以及该集团母公司和子公司的法人、高管对外控股公司
        if (relations.contains(RelationsEnum.personOutHold)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += "" +
                    // 3.1 该集团公司法人、关键管理人员对外控股公司
                    " match a=(s:ent{zsid:$zsid})<-[:staff|:legal]-(:person)-[:hold]->(:ent) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER +

                    // 3.2 该集团公司最终控制人对外控股公司
                    " union match path1=(s:ent{zsid:$zsid})<-[:hold *]-(end) where not (end)<-[:hold]-() and labels(end)[0] = 'person' " +
                    "       optional match path2=(end)-[:hold]->(:ent) " +
                    "       with collect(path1) + collect(path2) as path unwind path as a " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER +

                    // 3.3 该集团子公司的法人、高管对外控股公司
                    " union match path1=(s:ent{zsid:$zsid})-[:hold *]->(e:ent) " +
                    "       optional match path2=(e)<-[:legal|:staff]-(:person)-[:hold]->(:ent) " +
                    "       with collect(path1) + collect(path2) as path unwind path as a " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }

        // 4.人员对外任职：该集团公司和子公司法人、高管对外任职公司
        if (relations.contains(RelationsEnum.manager)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += "" +
                    // 4.1 该集团公司法人、关键管理人员对外任职公司
                    " match a=(s:ent{zsid:$zsid})<-[:staff|:legal]-(:person)-[:staff|:legal]->(:ent) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER +

                    // 4.2 该集团子公司的法人、高管对外任职公司
                    " union match path1=(s:ent{zsid:$zsid})-[:hold *]->(e:ent) " +
                    "       optional match path2=(e)<-[:legal|:staff]-(:person)-[:staff|:legal]->(:ent) " +
                    "       with collect(path1) + collect(path2) as path unwind path as a " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }

        // 5.人员地址疑似：与该集团母公司的最终控制人使用同一地址关联自然人对外控股公司以及与该集团母公司和子公司法人、高管使用同一地址关联自然人对外控股公司
        if (relations.contains(RelationsEnum.samedom)) {
            if (StringUtil.isNotBlank(cypher)) {
                cypher += " union ";
            }
            cypher += "" +
                    // 5.1 与该集团母公司的法人、高管使用同一地址关联自然人对外控股公司
                    " match a=(s:ent{zsid:$zsid})<-[:staff|:legal]-(:person)-[:peraddr]->()<-[:peraddr]-(:person)-[:hold]->(:ent) " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER +

                    // 5.1 与该集团母公司的最终实际控制人使用同一地址关联自然人对外控股公司
                    " union match path1 = (s:ent{zsid:$zsid})<-[:hold *]-(end) where not (end)<-[:hold]-() and labels(end)[0] = 'person' " +
                    "       optional match path2 = (end)-[:peraddr]->()<-[:peraddr]-(p1:person)-[:hold]->(e:ent) " +
                    "       with collect(path1) + collect(path2) as path unwind path as a " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER +

                    // 5.3 与该集团子公司的法人、高管使用同一地址关联自然人对外控股公司
                    " union match path1=(s:ent{zsid:$zsid})-[:hold *]->(e:ent) " +
                    "       optional match path2=(e)<-[:legal|:staff]-(:person)-[:peraddr]->()<-[:peraddr]-(:person)-[:hold]->(:ent) " +
                    "       with collect(path1) + collect(path2) as path unwind path as a " +
                    "       with filter (p in collect(a) where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))) as path " +
                    "       unwind path as a " + COMMON_RETURN_CYPHER;
        }

        LOGGER.debug("集团族谱成员cypher:{}, params:{}", cypher, params);
        return executeQuery(cypher, params, parameter.getAsync());
    }


    /**
     * 执行neo4j查询并获取返回接口
     *
     * @param cypher 查询语句
     * @param params 参数
     * @param async  是否是异步接口
     * @return
     */
    private List<Record> executeQuery(String cypher, Map<String, Object> params, boolean async) {
        if (StringUtil.isBlank(cypher)) {
            return null;
        }
        Session session = neo4jConfig.getDriver().session();
        Statement statement = new Statement(cypher, params);
        StatementResult result = session.run(statement);
        Future<List<Record>> future = ThreadPoolConfig.QUERY_NEO4J_THREAD_POOL.submit(() -> {
            List<Record> recordList = new LinkedList<>();
            while (result.hasNext()) {
                recordList.add(result.next());
            }
            return recordList;
        });
        long waitTime = async ? asyncMaxWaitTime : syncMaxWaitTime;
        try {
            return future.get(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn("查询异常，async:{}, cypher:{}， params:{}, msg:{}", async, cypher, params, e.getMessage());
            future.cancel(true);
            throw new RuntimeException();
        } catch (TimeoutException e) {
            LOGGER.warn("查询超时，async:{}, cypher:{}， params:{}", async, cypher, params);
            session.reset();
            future.cancel(true);
            throw new QueryNeo4jTimeOutException();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}
