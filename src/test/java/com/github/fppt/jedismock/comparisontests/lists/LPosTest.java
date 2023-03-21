package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static redis.clients.jedis.params.LPosParams.lPosParams;

@ExtendWith(ComparisonBase.class)
public class LPosTest {

    private static final String key = "lpos_key";

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();

        jedis.rpush(key, "a", "b", "c", "1", "2", "3", "c", "c");
    }

    @TestTemplate
    @DisplayName("Check for basic usage")
    public void whenUsingLPos_EnsureReturnsLeftmostEntry(Jedis jedis) {
        assertEquals(2, jedis.lpos(key, "c"));
        assertEquals(5, jedis.lpos(key, "3"));
        assertEquals(0, jedis.lpos(key, "a"));
        assertNull(jedis.lpos(key, "d"));
    }

    @TestTemplate
    @DisplayName("Check for rank param")
    public void whenUsingLPos_EnsureRankWorksCorrectly(Jedis jedis) {
        assertEquals(2, jedis.lpos(key, "c", lPosParams().rank(1)));
        assertEquals(6, jedis.lpos(key, "c", lPosParams().rank(2)));
        assertEquals(7, jedis.lpos(key, "c", lPosParams().rank(-1)));
        assertEquals(6, jedis.lpos(key, "c", lPosParams().rank(-2)));
        assertNull(jedis.lpos(key, "c", lPosParams().rank(4)));
        assertNull(jedis.lpos(key, "c", lPosParams().rank(-4)));

        assertThrows(JedisDataException.class, () -> jedis.lpos(key, "c", lPosParams().rank(0)));
    }

    @TestTemplate
    @DisplayName("Check for count param")
    public void whenUsingLPos_EnsureCountWorksCorrectly(Jedis jedis) {
        assertEquals(singletonList(2L), jedis.lpos(key, "c", lPosParams(), 1));
        assertEquals(asList(2L, 6L), jedis.lpos(key, "c", lPosParams(), 2));
        assertEquals(asList(2L, 6L, 7L), jedis.lpos(key, "c", lPosParams(), 3));
        assertEquals(asList(2L, 6L, 7L), jedis.lpos(key, "c", lPosParams(), 0));
    }

    @TestTemplate
    @DisplayName("Check for maxlen param")
    public void whenUsingLPos_EnsureMaxlenWorksCorrectly(Jedis jedis) {
        assertNull(jedis.lpos(key, "1", lPosParams().maxlen(3)));
        assertEquals(3L, jedis.lpos(key, "1", lPosParams().maxlen(4)));
        assertEquals(3L, jedis.lpos(key, "1", lPosParams().maxlen(0)));
    }

    @TestTemplate
    @DisplayName("Check for count and rank params combined")
    public void whenUsingLPos_EnsureCountAndRankWorkCorrectly(Jedis jedis) {
        assertEquals(asList(6L, 7L), jedis.lpos(key, "c", lPosParams().rank(2), 2));
        assertEquals(asList(7L, 6L), jedis.lpos(key, "c", lPosParams().rank(-1), 2));
        assertEquals(asList(7L, 6L, 2L), jedis.lpos(key, "c", lPosParams().rank(-1), 0));
    }

    @TestTemplate
    @DisplayName("Check for rank and maxlen params cobined")
    public void whenUsingLPos_EnsureRankAndMaxlenWorkCorrectly(Jedis jedis) {
        assertEquals(2L, jedis.lpos(key, "c", lPosParams().rank(1).maxlen(3)));
        assertNull(jedis.lpos(key, "c", lPosParams().rank(1).maxlen(2)));
        assertEquals(7L, jedis.lpos(key, "c", lPosParams().rank(-1).maxlen(1)));
        assertNull(jedis.lpos(key, "3", lPosParams().rank(-1).maxlen(1)));
        assertEquals(5L, jedis.lpos(key, "3", lPosParams().rank(-1).maxlen(3)));
    }

    @TestTemplate
    @DisplayName("Check for all params combined")
    public void whenUsingLPos_EnsureAllParamsWorkCorrectly(Jedis jedis) {
        assertEquals(singletonList(2L), jedis.lpos(key, "c", lPosParams().rank(1).maxlen(5), 2));
        assertEquals(asList(2L, 6L), jedis.lpos(key, "c", lPosParams().rank(1).maxlen(7), 2));
        assertEquals(singletonList(7L), jedis.lpos(key, "c", lPosParams().rank(-1).maxlen(1), 2));
        assertEquals(asList(7L, 6L), jedis.lpos(key, "c", lPosParams().rank(-1).maxlen(2), 2));
    }
}
