package com.next.config;

import com.alibaba.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;

/**
 * @Title: RedisConfig
 * @Description:
 * @author: tjx
 * @date :2022/9/28 23:33
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfig {
    private String host;

    private Integer port;

    @Bean
    public ShardedJedisPool shardedJedisPool(){
        JedisShardInfo jedisShardInfo = new JedisShardInfo(host,port); //如果是集群可以添加多个JedisShardInfo连接
        List<JedisShardInfo> jedisShardInfoList = Lists.newArrayList(jedisShardInfo);
        GenericObjectPoolConfig config =new GenericObjectPoolConfig();
        return new ShardedJedisPool(config,jedisShardInfoList);
    }

}
