# spring_neo4j
spring boot操作neo4j

1.common包：用来存放一些公共的东西，这里存放了图上的各种关系，其实应该写成枚举类型的；
2.config包：数据库连接初始化包；
3.controller,service包就是web项目中的controller和service层，分别用来提供rest访问和对dao层进行操作；
4.entities包：图中顶点和边的实体类，注意里边的不同注解代表不同含义；
5.model包：其它实例对象，pojo;
6.reposities包：里边的接口实现了GraphRepository类，然后通过定义Cypher来对数据库进行操作，service层只需要调用这个接口中的方法就可以了；
7.util包：工具类包；
8.Neo4jProcessorApplication类是spring boot的入口类；
9.resources包中的application.yml是spring boot的配置文件，可以是yml文件或者是properties文件。
