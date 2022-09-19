package com.next.db;

import com.google.common.collect.Maps;
import io.shardingsphere.api.algorithm.sharding.standard.RangeShardingAlgorithm;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * @Title: SeatDataSourcesConfig
 * @Description:
 * @author: tjx
 * @date :2022/9/19 15:38
 */
@Configuration
@MapperScan(basePackages = "com.next.seatDao",sqlSessionTemplateRef = "trainSeatSqlSessionTemplate")
public class SeatDataSourcesConfig {

    @Bean(name = DataSources.TRAIN_SEAT_DB_1)
    @ConfigurationProperties(prefix = "spring.datasource-seat-1")
    public DataSource trainSeatDB1(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = DataSources.TRAIN_SEAT_DB_2)
    @ConfigurationProperties(prefix = "spring.datasource-seat-2")
    public DataSource trainSeatDB2(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = DataSources.TRAIN_SEAT_DB_3)
    @ConfigurationProperties(prefix = "spring.datasource-seat-3")
    public DataSource trainSeatDB3(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = DataSources.TRAIN_SEAT_DB_4)
    @ConfigurationProperties(prefix = "spring.datasource-seat-4")
    public DataSource trainSeatDB4(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = DataSources.TRAIN_SEAT_DB_5)
    @ConfigurationProperties(prefix = "spring.datasource-seat-5")
    public DataSource trainSeatDB5(){
        return DataSourceBuilder.create().build();
    }


    @Bean(name = "trainSeatShardingDataSource")
    public DataSource trainSeatShardingDataSource(@Qualifier(DataSources.TRAIN_SEAT_DB_1) DataSource trainSeatDB1,
                                                  @Qualifier(DataSources.TRAIN_SEAT_DB_2) DataSource trainSeatDB2,
                                                  @Qualifier(DataSources.TRAIN_SEAT_DB_3) DataSource trainSeatDB3,
                                                  @Qualifier(DataSources.TRAIN_SEAT_DB_4) DataSource trainSeatDB4,
                                                  @Qualifier(DataSources.TRAIN_SEAT_DB_5) DataSource trainSeatDB5) throws SQLException {
        Map<String,DataSource> dataSourceMap = Maps.newHashMap();
        //设置分库映射
        dataSourceMap.put(DataSources.TRAIN_SEAT_DB_1,trainSeatDB1);
        dataSourceMap.put(DataSources.TRAIN_SEAT_DB_2,trainSeatDB2);
        dataSourceMap.put(DataSources.TRAIN_SEAT_DB_3,trainSeatDB3);
        dataSourceMap.put(DataSources.TRAIN_SEAT_DB_4,trainSeatDB4);
        dataSourceMap.put(DataSources.TRAIN_SEAT_DB_5,trainSeatDB5);
        ShardingRuleConfiguration ruleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration();
        //分表同一前缀
        tableRuleConfiguration.setLogicTable("train_seat");
        //配置数据库节点
        tableRuleConfiguration.setActualDataNodes(DataSources.TRAIN_SEAT_DB_1 + ".train_seat_1," +
                                                  DataSources.TRAIN_SEAT_DB_2 + ".train_seat_2," +
                                                  DataSources.TRAIN_SEAT_DB_3 + ".train_seat_3," +
                                                  DataSources.TRAIN_SEAT_DB_4 + ".train_seat_4," +
                                                  DataSources.TRAIN_SEAT_DB_5 + ".train_seat_5," +
                                                  DataSources.TRAIN_SEAT_DB_1 + ".train_seat_6," +
                                                  DataSources.TRAIN_SEAT_DB_2 + ".train_seat_7," +
                                                  DataSources.TRAIN_SEAT_DB_3 + ".train_seat_8," +
                                                  DataSources.TRAIN_SEAT_DB_4 + ".train_seat_9," +
                                                  DataSources.TRAIN_SEAT_DB_5 + ".train_seat_10");
        //设置分库策略
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("train_number_id",new TrainSeatDataBaseShardingAlgorithm()));
        //设置分表策略
        tableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("train_number_id",new TrainSeatTableShardingAlgorithm()));
        ruleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        return ShardingDataSourceFactory.createDataSource(dataSourceMap,ruleConfiguration,Maps.newHashMap(),new Properties());
    }

    @Bean(name = "trainSeatTransactionManager")
    public DataSourceTransactionManager trainSeatTransactionManager(@Qualifier("trainSeatShardingDataSource")DataSource trainSeatShardingDataSource){
        return new DataSourceTransactionManager(trainSeatShardingDataSource);
    }

    @Bean(name = "trainSeatSqlSessionFactory")
    public SqlSessionFactory trainSeatSqlSessionFactory(@Qualifier("trainSeatShardingDataSource")DataSource trainSeatShardingDataSource) throws Exception{
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(trainSeatShardingDataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:seatMappers/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "trainSeatSqlSessionTemplate")
    public SqlSessionTemplate trainSeatSqlSessionTemplate(@Qualifier("trainSeatSqlSessionFactory")SqlSessionFactory trainSeatSqlSessionFactory){
        return new SqlSessionTemplate(trainSeatSqlSessionFactory);
    }
}
