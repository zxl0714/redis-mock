package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.args.ListPosition;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ComparisonBase.class)
public class LInsertTest {

    private final static String key = "linsert_key";
    private final static String nonExistingkey = "linsert_key2";

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
        jedis.rpush(key, "1", "2", "3", "3", "4");
    }

    @TestTemplate
    @DisplayName("Check basic linsert case")
    public void whenUsingLInsert_EnsureCorrectlyInserted(Jedis jedis) {
        assertEquals(jedis.linsert(key, ListPosition.AFTER, "1", "10"), 6);
        assertEquals(jedis.lrange(key, 0, -1), Arrays.asList("1", "10", "2", "3", "3", "4"));
    }

    @TestTemplate
    @DisplayName("Check insert before first")
    public void whenUsingLInsert_EnsureCorrectlyInsertedBeforeFirst(Jedis jedis) {
        assertEquals(jedis.linsert(key, ListPosition.BEFORE, "1", "10"), 6);
        assertEquals(jedis.lrange(key, 0, -1), Arrays.asList("10", "1", "2", "3", "3", "4"));
    }

    @TestTemplate
    @DisplayName("Check insert after last")
    public void whenUsingLInsert_EnsureCorrectlyInsertedAfterLast(Jedis jedis) {
        assertEquals(jedis.linsert(key, ListPosition.AFTER, "4", "10"), 6);
        assertEquals(jedis.lrange(key, 0, -1), Arrays.asList("1", "2", "3", "3", "4", "10"));
    }

    @TestTemplate
    @DisplayName("Check choosing leftmost pivot")
    public void whenUsingLInsert_EnsureCorrectlyChosenLeftmostPivot(Jedis jedis) {
        assertEquals(jedis.linsert(key, ListPosition.AFTER, "3", "10"), 6);
        assertEquals(jedis.lrange(key, 0, -1), Arrays.asList("1", "2", "3", "10", "3", "4"));
    }

    @TestTemplate
    @DisplayName("Check non existing key")
    public void whenUsingLInsert_EnsureReturnsZeroOnNonExistingKey(Jedis jedis) {
        assertEquals(jedis.linsert(nonExistingkey, ListPosition.AFTER, "1", "1"), 0);
    }

    @TestTemplate
    @DisplayName("Check for no pivot")
    public void whenUsingLInsert_EnsureReturnsNegOneOnPivotNotFound(Jedis jedis) {
        assertEquals(jedis.linsert(key, ListPosition.AFTER, "5", "10"), -1);
    }
}
