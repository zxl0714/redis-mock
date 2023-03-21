package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ComparisonBase.class)
public class LSetTest {
    private static final String key = "lset_list_key";
    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
        jedis.rpush(key, "1", "2", "3");
    }

    @TestTemplate
    @DisplayName("Change element")
    public void whenUsingLSet_EnsureElementIsSet(Jedis jedis) {
        jedis.lset(key, 1, "5");
        assertEquals(jedis.lindex(key, 1), "5");
    }

    @TestTemplate
    @DisplayName("Change element by negative index")
    public void whenUsingLSet_EnsureNegativeIndexWorks(Jedis jedis) {
        jedis.lset(key, -2, "5");
        assertEquals(jedis.lindex(key, 1), "5");
    }

    @TestTemplate
    @DisplayName("Check index out of bound returns error")
    public void whenUsingLSet_EnsureErrorOnIndexOutOfBounds(Jedis jedis) {
        Assertions.assertThrows(JedisDataException.class, () -> jedis.lset(key, 10, "5"), "ERR index out of range");
        Assertions.assertThrows(JedisDataException.class, () -> jedis.lset(key, -10, "5"), "ERR index out of range");
    }
}
