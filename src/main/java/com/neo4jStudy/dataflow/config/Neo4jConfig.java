package com.neo4jStudy.dataflow.config;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.neo4j.ogm.session.Session;

/**
 * @author xyf
 * @Data Created in 11:42 2018/6/11
 * @Descriptions    neo4j图数据库配置
 */
@org.springframework.context.annotation.Configuration//用于定义配置类，可替换xml配置文件，被注解的类内部包含一个或多个被@Bean注解的方法，这些Bean将被扫描，然后被实例化
@EnableNeo4jRepositories(basePackages="com.chinadaas.dataflow.repositories")
@EnableTransactionManagement
public class Neo4jConfig {

    @Value("${spring.data.neo4j.uri:bolt://192.168.100.83:7666}")
    private String uri;

    private Configuration getConfiguration(){
        Configuration conf = new Configuration();
        conf.driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver")
                .setURI(uri).setCredentials("neo4j","neo4j#2018");
        return conf;
    }

    private SessionFactory getSessionFactory(){
        return new SessionFactory(getConfiguration(),"com.chinadaas.dataflow.entities");
    }

    @Bean
    public Session session(){
        return getSessionFactory().openSession();
    }

}






















