package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import com.github.fppt.jedismock.comparisontests.TestErrorMessages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisDataException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(ComparisonBase.class)
public class BlockingOperationsTest {

    private ExecutorService blockingThread;
    private Jedis blockedClient;

    @BeforeEach
    public void setUp(Jedis jedis, HostAndPort hostAndPort) {
        jedis.flushAll();
        blockedClient = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());
        blockingThread = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    public void tearDown() {
        blockedClient.close();
        blockingThread.shutdownNow();
    }

    @TestTemplate
    public void whenUsingBrpoplpush_EnsureItBlocksAndCorrectResultsAreReturned(Jedis jedis) throws ExecutionException, InterruptedException {
        String list1key = "source list";
        String list2key = "target list";

        jedis.rpush(list2key, "a", "b", "c");

        //Block on performing the BRPOPLPUSH
        Future<?> future = blockingThread.submit(() -> {
            String result = blockedClient.brpoplpush(list1key, list2key, 500);
            assertEquals("3", result);
        });

        //Check the list is not modified
        List<String> results = jedis.lrange(list2key, 0, -1);
        assertEquals(3, results.size());

        //Push some stuff into the blocked list
        jedis.rpush(list1key, "1", "2", "3");

        future.get();

        //Check the list is modified
        results = jedis.lrange(list2key, 0, -1);
        assertEquals(4, results.size());
    }

    @TestTemplate
    public void whenUsingBrpoplpushAndReachingTimeout_Return(Jedis jedis) {
        String list1key = "another source list";
        String list2key = "another target list";

        String result = jedis.brpoplpush(list1key, list2key, 1);

        assertNull(result);
    }

    @TestTemplate
    public void whenUsingBrpoplpush_EnsureClientCanStillGetOtherResponsesInTimelyManner(Jedis jedis) {
        String list1key = "another another source list";
        String list2key = "another another target list";

        blockingThread.submit(() -> {
            String result = blockedClient.brpoplpush(list1key, list2key, 500);
            assertEquals("3", result);
        });

        //Issue random commands to make sure mock is still responsive
        jedis.set("k1", "v1");
        jedis.set("k2", "v2");
        jedis.set("k3", "v3");
        jedis.set("k4", "v4");
        jedis.set("k5", "v5");

        //Check random commands were processed
        assertEquals("v1", jedis.get("k1"));
        assertEquals("v2", jedis.get("k2"));
        assertEquals("v3", jedis.get("k3"));
        assertEquals("v4", jedis.get("k4"));
        assertEquals("v5", jedis.get("k5"));
    }

    @TestTemplate
    public void whenUsingBlpop_EnsureItBlocksAndCorrectResultsAreReturned(Jedis jedis) throws ExecutionException, InterruptedException {
        String key = "list1_kfubdjkfnv";
        jedis.rpush(key, "d", "e", "f");
        //Block on performing the BLPOP
        Future<?> future = blockingThread.submit(() -> {
            List<String> result = blockedClient.blpop(10, key);
            assertEquals(2, result.size());
            assertEquals(key, result.get(0));
            assertEquals("d", result.get(1));
        });
        future.get();
        //Check the list is modified
        List<String> results = jedis.lrange(key, 0, -1);
        assertEquals(2, results.size());
    }

    @TestTemplate
    public void whenUsingBlpop_EnsureItBlocksAndCorrectResultsAreReturnedOnMultipleList(Jedis jedis) throws ExecutionException, InterruptedException {
        String list1key = "list1_dkjfnvdk";
        String list2key = "list2_kjvnddkf";
        String list3key = "list3_oerurthv";


        //Block on performing the BLPOP
        Future<?> future = blockingThread.submit(() -> {
            List<String> result = blockedClient.blpop(10, list1key, list2key, list3key);
            assertEquals(list2key, result.get(0));
            assertEquals("a", result.get(1));
        });
        Thread.sleep(1000);
        jedis.rpush(list2key, "a", "b", "c");
        jedis.rpush(list3key, "d", "e", "f");
        future.get();

        //Check the list is modified
        List<String> results = jedis.lrange(list2key, 0, -1);
        assertEquals(2, results.size());
        results = jedis.lrange(list3key, 0, -1);
        assertEquals(3, results.size());
    }

    @TestTemplate
    public void whenUsingBlpop_EnsureItTimeout(Jedis jedis) throws ExecutionException, InterruptedException, TimeoutException {
        String list1key = "list1_kdjfnvdsu";
        String list2key = "list2_mbhkdushy";
        String list3key = "list3_qzkmpthju";

        // init redisbase
        jedis.lrange(list2key, 0, -1);

        //Block on performing the BLPOP
        Future<?> future = blockingThread.submit(() -> {
            List<String> result = blockedClient.blpop(1, list1key, list2key, list3key);
            assertNull(result);
        });
        //Check the list is not modified
        jedis.getClient().setSoTimeout(2000);
        List<String> results = jedis.lrange(list2key, 0, -1);
        assertEquals(0, results.size());
        future.get(4, TimeUnit.SECONDS);
    }

    @TestTemplate
    public void whenUsingBlpop_EnsureNotWokenByTransaction(Jedis jedis) throws ExecutionException, InterruptedException, TimeoutException {
        String listKey = "list_key";

        Future<?> future = blockingThread.submit(() -> {
            List<String> result = blockedClient.blpop(0, listKey);
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(listKey, result.get(0));
            assertEquals("b", result.get(1));
        });

        Transaction t = jedis.multi();
        t.lpush(listKey, "0");
        t.del(listKey);
        t.exec();
        jedis.del(listKey);
        jedis.lpush(listKey, "b");
        future.get(5, TimeUnit.SECONDS);
    }

    @TestTemplate
    public void whenUsingBlpop_EnsureDoesntBlockInsideTransaction(Jedis jedis) {
        String key = "blpop_transaction_key";

        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            jedis.del(key);
            jedis.lpush(key, "foo");
            jedis.lpush(key, "bar");

            Transaction t = jedis.multi();

            t.blpop(0, key);
            t.blpop(0, key);
            t.blpop(0, key);

            List<Object> result = t.exec();

            assertEquals(
                    Arrays.asList(
                            Arrays.asList(key, "bar"),
                            Arrays.asList(key, "foo"),
                            null
                    ), result);

        }, TestErrorMessages.DEADLOCK_ERROR_MESSAGE);
    }

    @TestTemplate
    public void whenUsingBlpop_EnsureThrowsErrorOnNegativeTimeout(Jedis jedis) {
        String key = "blpop_negative_timeout_key";

        JedisDataException exception = Assertions.assertThrows(JedisDataException.class, () -> jedis.blpop(-5, key));
        assertEquals("ERR timeout is negative", exception.getMessage());

        exception = Assertions.assertThrows(JedisDataException.class, () -> jedis.blpop(-0.1, key));
        assertEquals("ERR timeout is negative", exception.getMessage());
    }

    @TestTemplate
    public void whenUsingBlpop_EnsureStillWaitsIfKeyIsNotList(Jedis jedis) throws ExecutionException, InterruptedException, TimeoutException {
        String key = "blpop_not_a_list_key";


        Future<?> future = blockingThread.submit(() -> {
            List<String> result = blockedClient.blpop(0, key);
            assertEquals(2, result.size());
            assertEquals(key, result.get(0));
            assertEquals("foo", result.get(1));
        });

        Thread.sleep(300); // wait for blpop to execute

        Transaction t = jedis.multi();

        t.rpush(key, "bar");
        t.del(key);
        t.set(key, "bar2");
        t.exec();

        jedis.del(key);
        jedis.lpush(key, "foo");

        future.get(5, TimeUnit.SECONDS);
    }

    @TestTemplate
    public void whenUsingBRPopLPush_ensureBlocksIndefinitely(Jedis jedis) throws InterruptedException, ExecutionException {
        String fromKey = "brpoplpush_from";
        String toKey = "brpoplpush_to";


        Future<?> future = blockingThread.submit(() -> {
            String value = blockedClient.brpoplpush(fromKey, toKey, 0);
            assertEquals("bar", value);
        });

        // wait to be sure we wait more than 0 seconds
        Thread.sleep(1000);
        jedis.rpush(fromKey, "bar");

        future.get();
    }
}