package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

@ExtendWith(ComparisonBase.class)
public class ListPopTest {

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void whenUsingListPop_ensureCheckForType(Jedis jedis) {
        String key = "pop_key";
        jedis.set(key, "notalist");

        JedisDataException exception = Assertions.assertThrows(JedisDataException.class, () -> jedis.rpop(key));
        Assertions.assertTrue(exception.getMessage().startsWith("WRONGTYPE"));

        exception = Assertions.assertThrows(JedisDataException.class, () -> jedis.lpop(key));
        Assertions.assertTrue(exception.getMessage().startsWith("WRONGTYPE"));
    }
}
