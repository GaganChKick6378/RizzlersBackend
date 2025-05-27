package com.kdu.rizzlers.cache;

import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService implements DisposableBean {
    private RedisClusterClient redisClusterClient;
    private StatefulRedisClusterConnection<String, String> clusterConnection;
    private RedisAdvancedClusterCommands<String, String> clusterCommands;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> standaloneConnection;
    private RedisCommands<String, String> standaloneCommands;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final boolean isCluster;

    public CacheService(
        @Value("${cache.redis.host}") String redisHost,
        @Value("${cache.redis.port}") int redisPort,
        @Value("${cache.redis.ssl}") boolean redisSsl
    ) {
        String protocol = redisSsl ? "rediss" : "redis";
        String redisUrl = protocol + "://" + redisHost + ":" + redisPort;
        boolean clusterMode = !(redisHost.equals("localhost") || redisHost.equals("127.0.0.1"));
        this.isCluster = clusterMode;
        System.out.println("[CacheService] Connecting to Redis with URI: " + redisUrl + " | Cluster mode: " + clusterMode);
        try {
            RedisURI redisURI = RedisURI.create(redisUrl);
            if (clusterMode) {
                this.redisClusterClient = RedisClusterClient.create(redisURI);
                this.clusterConnection = redisClusterClient.connect(io.lettuce.core.codec.StringCodec.UTF8);
                this.clusterCommands = clusterConnection.sync();
            } else {
                this.redisClient = RedisClient.create(redisURI);
                this.standaloneConnection = redisClient.connect(io.lettuce.core.codec.StringCodec.UTF8);
                this.standaloneCommands = standaloneConnection.sync();
            }
        } catch (Exception e) {
            System.err.println("[CacheService] Failed to connect to Redis: " + redisUrl);
            e.printStackTrace();
            throw e;
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        String value = isCluster ? clusterCommands.get(key) : standaloneCommands.get(key);
        if (value == null) return null;
        try {
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing cache value", e);
        }
    }

    public void set(String key, Object value, long ttl, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            if (isCluster) {
                clusterCommands.set(key, json);
                clusterCommands.expire(key, unit.toSeconds(ttl));
            } else {
                standaloneCommands.set(key, json);
                standaloneCommands.expire(key, unit.toSeconds(ttl));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing cache value", e);
        }
    }

    public void evict(String key) {
        if (isCluster) {
            clusterCommands.del(key);
        } else {
            standaloneCommands.del(key);
        }
    }

    public void delete(String key) {
        evict(key);
    }

    public void put(String key, Object value) {
        set(key, value, 10, TimeUnit.MINUTES);
    }

    @Override
    public void destroy() {
        if (isCluster) {
            clusterConnection.close();
            redisClusterClient.shutdown();
        } else {
            standaloneConnection.close();
            redisClient.shutdown();
        }
    }
}
