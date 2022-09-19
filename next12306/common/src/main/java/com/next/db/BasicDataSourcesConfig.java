package com.next.db;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * @Title: BasicDataSourcesConfig
 * @Description: 此配置对应的是基础数据源配置
 * @author: tjx
 * @date :2022/9/8 20:24
 */
@Configuration
@MapperScan(basePackages = "com.next.dao",sqlSessionTemplateRef = "sqlSessionTemplate")
public class BasicDataSourcesConfig {

    /**
     * 创建主从数据源，并使用@Primary注解指定默认加载主数据源
     * @return
     */
    @Primary
    @Bean(name = DataSources.MASTER_DB)
    @ConfigurationProperties(prefix = "spring.datasource-master")
    public DataSource masterDB(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = DataSources.SLAVE_DB)
    @ConfigurationProperties(prefix = "spring.datasource-slave")
    public DataSource slaveDB(){
        return DataSourceBuilder.create().build();
    }

    /**
     * 根据shardingsphere配置主从数据源
     * @param masterDB
     * @param slaveDB
     * @return
     * @throws SQLException
     */
    @Bean(name = "masterSlaveDataSource")
    public DataSource masterSlaveDataSource(@Qualifier(DataSources.MASTER_DB) DataSource masterDB,@Qualifier(DataSources.SLAVE_DB) DataSource slaveDB) throws SQLException {
        Map<String,DataSource> dataSourceMap = Maps.newHashMap();
        dataSourceMap.put(DataSources.MASTER_DB,masterDB);
        dataSourceMap.put(DataSources.SLAVE_DB,slaveDB);

        //创建主从数据源配置，第一个参数至第四个参数为：数据源配置名称、主数据源、从数据源(如果有多个可以一起放入到集合中)、采用轮询策略调度数据源
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration =
                new MasterSlaveRuleConfiguration("ds_master_slave",DataSources.MASTER_DB,
                Lists.newArrayList(DataSources.SLAVE_DB), new RoundRobinMasterSlaveLoadBalanceAlgorithm());

        return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap,masterSlaveRuleConfiguration,Maps.newHashMap(),new Properties());
    }

    /**
     * 为Master数据源创建事务
     * @param masterDB
     * @return
     */
    @Primary
    @Bean(name = "transactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier(DataSources.MASTER_DB) DataSource masterDB){
        return new DataSourceTransactionManager(masterDB);
    }

    @Primary
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier(DataSources.MASTER_DB) DataSource masterDB) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(masterDB);
        //主数据源需要扫面的路径 //TODO getResource()和getResources()区别
        //PathMatchingResourcePatternResolver().getResource() 表示加载文件中的系统资源(D:/xxx/xx.xml)
        //PathMatchingResourcePatternResolver().getResources() 表示加载当前类路径中所有匹配的资源
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mappers/*.xml"));
        SqlSessionFactory object = bean.getObject();
        return object;
    }

    @Primary
    @Bean(name = "sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
