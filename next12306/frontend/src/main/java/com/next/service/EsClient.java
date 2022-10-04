package com.next.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Title: EsClient
 * @Description: Es配置工具类
 * @author: tjx
 * @date :2022/10/1 19:22
 */
@Component
@Slf4j
public class EsClient implements ApplicationListener<ApplicationContextEvent> {

    @Value("${tjx.host}")
    private String host;

    private static final Integer CONNECT_TIMEOUT = 100;
    private static final Integer SOCKET_TIMEOUT = 60;
    private static final Integer REQUEST_TIMEOUT = SOCKET_TIMEOUT;
    private RestHighLevelClient restHighLevelClient;
//    private BasicHeader[] basicHeaders;

    @Override
    public void onApplicationEvent(ApplicationContextEvent applicationContextEvent) {
        try {
            initClient();
        } catch (Exception e) {
            log.error("es client init exception ",e);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            initClient();
        }
    }

    public void initClient(){
         log.info("es client init start");
         //将请求响应格式指定为json
//         basicHeaders = new BasicHeader[]{new BasicHeader("Accept","application/json charset=UTF-8")};
         RestClientBuilder clientBuilder = RestClient.builder(new HttpHost(host, 9200, "http"));
         clientBuilder.setRequestConfigCallback((RequestConfig.Builder configBuilder)->{
                          configBuilder.setConnectTimeout(CONNECT_TIMEOUT);
                          configBuilder.setSocketTimeout(SOCKET_TIMEOUT);
                          configBuilder.setConnectionRequestTimeout(REQUEST_TIMEOUT);
                          return configBuilder;
                      });
         restHighLevelClient = new RestHighLevelClient(clientBuilder);
         log.info("es client init end");
    }

    public IndexResponse index(IndexRequest indexRequest) throws IOException {
        try {
            return restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es update exception IndexRequest:{}",indexRequest,e);
            throw e;
        }
    }

    public UpdateResponse update(UpdateRequest updateRequest) throws IOException {
        try {
            return restHighLevelClient.update(updateRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es update exception , updateRequest: {}",updateRequest,e);
            throw e;
        }
    }

    public GetResponse get(GetRequest getRequest) throws IOException {
        try {
            return restHighLevelClient.get(getRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es get exception , getRequest: {}",getRequest,e);
            throw e;
        }
    }

    public MultiGetResponse multiGet(MultiGetRequest multiGetRequest) throws IOException {
        try {
            return restHighLevelClient.multiGet(multiGetRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es multiGet exception, multiGetRequest:{}", multiGetRequest, e);
            throw e;
        }
    }

    public BulkResponse bulk(BulkRequest bulkRequest) throws IOException {
        try {
            return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es bulk exception, bulkRequest:{}", bulkRequest, e);
            throw e;
        }
    }
}
