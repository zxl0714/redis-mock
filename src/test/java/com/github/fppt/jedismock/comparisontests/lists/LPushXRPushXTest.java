package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ComparisonBase.class)
public class LPushXRPushXTest {
    private final static String lpushxKey = "lpushx_test_key";
    private final static String rpushxKey = "rpushx_test_key";

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void whenUsingLPushX_EnsureReturnsZeroOnNonList(Jedis jedis) {
        assertEquals(0, jedis.lpushx(lpushxKey, "foo"));
    }

    @TestTemplate
    public void whenUsingLPushX_EnsurePushesCorrectly(Jedis jedis) {
        jedis.lpush(lpushxKey, "fooo");
        assertEquals(3, jedis.lpushx(lpushxKey, "bar", "foo"));
        assertEquals(Arrays.asList("foo", "bar"), jedis.lrange(lpushxKey, 0, 1));
    }

    @TestTemplate
    public void whenUsingRPushX_EnsureReturnsZeroOnNonList(Jedis jedis) {
        assertEquals(0, jedis.lpushx(rpushxKey, "foo"));
    }

    @TestTemplate
    public void whenUsingRPushX_EnsurePushesCorrectly(Jedis jedis) {
        jedis.rpush(rpushxKey, "fooo");
        assertEquals(3, jedis.rpushx(rpushxKey, "bar", "foo"));
        assertEquals(Arrays.asList("bar", "foo"), jedis.lrange(rpushxKey, 1, 2));
    }
}
