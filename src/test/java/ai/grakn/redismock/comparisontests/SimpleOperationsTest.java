package ai.grakn.redismock.comparisontests;


import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(Theories.class)
public class SimpleOperationsTest extends ComparisonBase {

    @Theory
    public void whenSettingKeyAndRetrievingIt_CorrectResultIsReturned(Jedis jedis) {
        String key = "key";
        String value = "value";

        assertNull(jedis.get(key));
        jedis.set(key, value);
        assertEquals(value, jedis.get(key));
    }

    @Theory
    public void whenUsingRpop_EnsureTheLastElementPushedIsReturned(Jedis jedis){
        String key = "Another key";
        jedis.rpush(key, "1", "2", "3");
        assertEquals(jedis.rpop(key), "3");
    }

    @Theory
    public void whenUsingRpoplpush_CorrectResultsAreReturned(Jedis jedis){
        String list1key = "list 1";
        String list2key = "list 2";

        jedis.rpush(list1key, "1", "2", "3");
        jedis.rpush(list2key, "a", "b", "c");

        //Check the lists are in order
        List<String> results1 = jedis.lrange(list1key, 0, -1);
        List<String> results2 = jedis.lrange(list2key, 0, -1);

        assertTrue(results1.contains("1"));
        assertTrue(results1.contains("2"));
        assertTrue(results1.contains("3"));

        assertTrue(results2.contains("a"));
        assertTrue(results2.contains("b"));
        assertTrue(results2.contains("c"));

        //Check that the one list has been pushed into the other
        String result = jedis.rpoplpush(list1key, list2key);
        assertEquals("3", result);

        results1 = jedis.lrange(list1key, 0, -1);
        results2 = jedis.lrange(list2key, 0, -1);

        assertTrue(results1.contains("1"));
        assertTrue(results1.contains("2"));
        assertFalse(results1.contains("3"));

        assertTrue(results2.contains("3"));
        assertTrue(results2.contains("a"));
        assertTrue(results2.contains("b"));
        assertTrue(results2.contains("c"));
    }

    @Theory
    public void whenUsingFlushall_EnsureEverythingIsDeleted(Jedis jedis){
        String key = "my-super-special-key";
        String value = "my-not-so-special-value";

        jedis.set(key, value);
        assertEquals(value, jedis.get(key));

        jedis.flushAll();
        assertNull(jedis.get(key));
    }

    @Theory
    public void whenUsingLrem_EnsureDeletionsWorkAsExpected(Jedis jedis){
        String key = "my-super-special-sexy-key";
        String hello = "hello";
        String foo = "foo";

        jedis.rpush(key, hello);
        jedis.rpush(key, hello);
        jedis.rpush(key, foo);
        jedis.rpush(key, hello);

        //Everything in order
        List<String> list = jedis.lrange(key, 0, -1);
        assertEquals(hello, list.get(0));
        assertEquals(hello, list.get(1));
        assertEquals(foo, list.get(2));
        assertEquals(hello, list.get(3));

        long numRemoved = jedis.lrem(key, -2, hello);
        assertEquals(2L, numRemoved);

        //Check order again
        list = jedis.lrange(key, 0, -1);
        assertEquals(hello, list.get(0));
        assertEquals(foo, list.get(1));
    }

    @Theory
    public void whenUsingQuit_EnsureTheConnectionIsClosed(Jedis jedis){
        //Create a new connection
        Client client = jedis.getClient();
        Jedis newJedis = new Jedis(client.getHost(), client.getPort());
        newJedis.set("A happy lucky key", "A sad value");
        assertEquals("OK", newJedis.quit());

        expectedException.expect(JedisConnectionException.class);

        newJedis.set("A happy lucky key", "A sad value 2");
    }

    @Theory
    public void whenConcurrentlyIncrementingAndDecrementingCount_EnsureFinalCountIsCorrect(Jedis jedis) throws ExecutionException, InterruptedException {
        String key = "my-count-tracker";
        int [] count = new int[]{1, 5, 6, 2, -9, -2, 10, 11, 5, -2, -2};

        jedis.set(key, "0");
        assertEquals(0, Integer.parseInt(jedis.get(key)));

        //Increase counts concurrently
        ExecutorService pool = Executors.newCachedThreadPool();
        Set<Future> futues = new HashSet<>();
        for (int i : count) {
            futues.add(pool.submit(() -> {
                Jedis client = new Jedis(jedis.getClient().getHost(), jedis.getClient().getPort());
                client.incrBy(key, i);
                client.close();
            }));
        }


        for (Future futue : futues) {
            futue.get();
        }

        //Check final count
        assertEquals(25, Integer.parseInt(jedis.get(key)));
    }

    @Theory
    public void whenPinging_Pong(Jedis jedis){
        assertEquals("PONG", jedis.ping());
    }

    @Theory
    public void whenGettingKeys_EnsureCorrectKeysAreReturned(Jedis jedis){
        jedis.flushAll();
        jedis.mset("one", "1", "two", "2", "three", "3", "four", "4");

        //Check simple pattern
        Set<String> results = jedis.keys("*o*");
        assertEquals(3, results.size());
        assertTrue(results.contains("one") && results.contains("two") && results.contains("four"));

        //Another simple regex
        results = jedis.keys("t??");
        assertEquals(1, results.size());
        assertTrue(results.contains("two"));

        //All Keys
        results = jedis.keys("*");
        assertEquals(4, results.size());
        assertTrue(results.contains("one") && results.contains("two") && results.contains("three") && results.contains("four"));
    }

    @Theory
    public void whenAddingToASet_EnsureTheSetIsUpdated(Jedis jedis){
        String key = "my-set-key";
        Set<String> mySet = new HashSet<>(Arrays.asList("a", "b", "c", "d"));

        //Add everything from the set
        mySet.forEach(value -> jedis.sadd(key, value));

        //Get it all back
        assertEquals(mySet, jedis.smembers(key));
    }

    @Theory
    public void whenPoppingFromASet_EnsureTheSetIsUpdated(Jedis jedis){
        String key = "my-set-key";
        Set<String> mySet = new HashSet<>(Arrays.asList("a", "b", "c", "d"));

        //Add everything from the set
        mySet.forEach(value -> jedis.sadd(key, value));

        String poppedValue;
        do {
            poppedValue = jedis.spop(key);
            if(poppedValue != null) assertTrue("Popped value not in set", mySet.contains(poppedValue));
        } while (poppedValue != null);
    }

    @Theory
    public void whenMultiAddingToAHash_EnsureTheHashIsUpdated(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Get it all back
        assertEquals(myHash, jedis.hgetAll(key));
    }

    @Theory
    public void whenMultiAddingToAnExistingHash_EnsureTheHashIsUpdated(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        Map<String, String> myHash2 = new HashMap<>();
        myHash2.put("c", "1");
        myHash2.put("d", "2");
        myHash2.put("e", "3");
        myHash2.put("f", "4");

        // Update the hash with new values
        jedis.hmset(key, myHash);

        // Get it all back
        Map<String, String> expected = new HashMap<>();
        expected.put("a", "1");
        expected.put("b", "2");
        expected.put("c", "1");
        expected.put("d", "2");
        expected.put("e", "3");
        expected.put("f", "4");

        assertEquals(myHash, jedis.hgetAll(key));
    }

    @Theory
    public void whenDeletingFromAHash_EnsureTheHashIsUpdated(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Delete an entry
        assertEquals(Long.valueOf(1), jedis.hdel(key, "c"));

        // Check the result
        Map<String, String> expected = new HashMap<>();
        expected.put("a", "1");
        expected.put("b", "2");
        expected.put("d", "4");
        assertEquals(expected, jedis.hgetAll(key));
    }

    @Theory
    public void whenDeletingMissingKeyFromAHash_EnsureThatNothingHappens(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Delete an entry
        assertEquals(Long.valueOf(0), jedis.hdel(key, "e"));

        // Check the result
        assertEquals(myHash, jedis.hgetAll(key));
    }

    @Theory
    public void whenRequestingKeys_EnsureThatCorrectResultIsReturned(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Check the result
        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c", "d")), jedis.hkeys(key));
    }
    
    @Theory
    public void whenRequestingValues_EnsureThatCorrectResultIsReturned(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Check the result
        List<String> result = jedis.hvals(key);
        Collections.sort(result); // Make test repeatable
        assertEquals(Arrays.asList("1", "2", "3", "4"), result);
    }
    
    @Theory
    public void whenRequestingMultipleValues_EnsureThatCorrectResultIsReturned(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Check the result
        List<String> result = jedis.hmget(key, "b", "d");
        Collections.sort(result); // Make test repeatable
        assertEquals(Arrays.asList("2", "4"), result);
    }
    
    @Theory
    public void whenRequestingLength_EnsureThatCorrectResultIsReturned(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");
        myHash.put("b", "2");
        myHash.put("c", "3");
        myHash.put("d", "4");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Check the result
        assertEquals(Long.valueOf(4), jedis.hlen(key));
    }

    @Theory
    public void whenKeyExists_EnsureThatCorrectResultIsReturned(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Check the result
        assertEquals(true, jedis.hexists(key, "a"));
    }

    @Theory
    public void whenKeyDoesNotExist_EnsureThatCorrectResultIsReturned(Jedis jedis) {
        String key = "my-hash-key2";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Check the result
        assertEquals(false, jedis.hexists(key, "b"));
    }

    @Theory
    public void whenGetByKey_EnsureThatCorrectResultIsReturned(Jedis jedis) {
        String key = "my-hash-key";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Check the result
        assertEquals("1", jedis.hget(key, "a"));
    }

    @Theory
    public void whenGetByMissingKey_EnsureThatNullIsReturned(Jedis jedis) {
        String key = "my-hash-key";

        // Check the result
        assertEquals(null, jedis.hget(key, "zzz"));
    }

    @Theory
    public void whenSetNewKey_EnsureThat1IsReturned(Jedis jedis) {
        String key = "my-hash-key3";

        // Add to the set
        assertEquals(Long.valueOf(1), jedis.hset(key, "b", "2"));

        // Check the result
        assertEquals("2", jedis.hget(key, "b"));
    }

    @Theory
    public void whenSetExistingKey_EnsureThat0IsReturned(Jedis jedis) {
        String key = "my-hash-key3";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Add to the set
        assertEquals(Long.valueOf(0), jedis.hset(key, "a", "2"));

        // Check the result
        assertEquals("2", jedis.hget(key, "a"));
    }
    
    @Theory
    public void whenSetWhenNotExistsNewKey_EnsureThat1IsReturned(Jedis jedis) {
        String key = "my-hash-key4";

        // Add to the set
        assertEquals(Long.valueOf(1), jedis.hsetnx(key, "b", "2"));

        // Check the result
        assertEquals("2", jedis.hget(key, "b"));
    }

    @Theory
    public void whenSetWhenNotExistsExistingKey_EnsureThat0IsReturned(Jedis jedis) {
        String key = "my-hash-key4";
        Map<String, String> myHash = new HashMap<>();
        myHash.put("a", "1");

        // Add everything from the set
        jedis.hmset(key, myHash);

        // Add to the set
        assertEquals(Long.valueOf(0), jedis.hsetnx(key, "a", "2"));

        // Check the result
        assertEquals("1", jedis.hget(key, "a"));
    }

}
