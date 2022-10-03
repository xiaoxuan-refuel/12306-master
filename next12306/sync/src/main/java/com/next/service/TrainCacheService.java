package com.next.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * @Title: TrainCacheService
 * @Description: redis操作类
 * @author: tjx
 * @date :2022/9/28 23:44
 */
@Service
@Slf4j
public class TrainCacheService {

    @Autowired
    private ShardedJedisPool shardedJedisPool;

    private ShardedJedis getInstance(){
        return shardedJedisPool.getResource();
    }

    private void safeClose(ShardedJedis shardedJedis){
        try {
            if(shardedJedis != null){
                shardedJedis.close();
            }
        } catch (Exception e) {
          log.error("jedis close exception",e);
        }
    }

    public void set(String key,String value){
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = getInstance();
            shardedJedis.set(key, value);
        } catch (Exception e) {
            log.error("jedis set exception key:{},value:{}",key,value,e);
        } finally {
            safeClose(shardedJedis);
        }
    }

    public String get(String key){
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = getInstance();
            return shardedJedis.get(key);
        } catch (Exception e) {
            log.error("jedis get exception key:{},value:{}",key,e);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }

    public void hset(String key1,String key2,String value){
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = getInstance();
            shardedJedis.hset(key1, key2, value);
        } catch (Exception e) {
            log.error("jedis hset exception key1:{},key2:{},value:{}",key1,key2,value,e);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }

    public void hincrBy(String key1,String key2,Long value){
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = getInstance();
            shardedJedis.hincrBy(key1, key2, value);
        } catch (Exception e) {
            log.error("jedis hincrBy exception key1:{},key2:{},value:{}",key1,key2,value,e);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }
}
