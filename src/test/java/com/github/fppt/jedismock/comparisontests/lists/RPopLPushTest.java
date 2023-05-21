package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

@ExtendWith(ComparisonBase.class)
public class RPopLPushTest {

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void whenUsingRRopLPush_ensureDontPopOnInvalidTargetType(Jedis jedis) {
        String listKey = "rpoplpush_1_list";
        String valueKey = "rpoplpush_1_value";
        jedis.rpush(listKey, "1", "2", "3");
        jedis.set(valueKey, "some_value");

        JedisDataException e = Assertions.assertThrows(JedisDataException.class, () -> jedis.rpoplpush(listKey, valueKey));

        Assertions.assertEquals("WRONGTYPE Operation against a key holding the wrong kind of value", e.getMessage());

        Assertions.assertEquals(3, jedis.llen(listKey));
    }
}
