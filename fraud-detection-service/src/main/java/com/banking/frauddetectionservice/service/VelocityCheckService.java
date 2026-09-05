package com.banking.frauddetectionservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VelocityCheckService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_TRANSACTIONS = 5;
    private static final long WINDOW_MILLISECONDS = 60_000;

    private final DefaultRedisScript<Long> velocityScript =
            new DefaultRedisScript<>(
                    """
                    local now = tonumber(ARGV[1])
                    local windowStart = now - tonumber(ARGV[2])

                    redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, windowStart)

                    redis.call(
                        'ZADD',
                        KEYS[1],
                        now,
                        ARGV[3]
                    )

                    local count = redis.call(
                        'ZCOUNT',
                        KEYS[1],
                        windowStart,
                        now
                    )

                    redis.call('EXPIRE', KEYS[1], 65)

                    return count
                    """,
                    Long.class
            );

    public boolean isSuspicious(String accountNumber) {

        String key = "fraud:velocity:" + accountNumber;

        long now = System.currentTimeMillis();

        Long count = redisTemplate.execute(
                velocityScript,
                List.of(key),
                String.valueOf(now),
                String.valueOf(WINDOW_MILLISECONDS),
                UUID.randomUUID().toString()
        );

        return count != null && count >= MAX_TRANSACTIONS;
    }
}