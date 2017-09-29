package ai.grakn.redismock.comparisontests;


import ai.grakn.redismock.util.MockSubscriber;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Theories.class)
public class AdvanceOperationsTest extends ComparisonBase {

    @Theory
    public void whenTransactionWithMultiplePushesIsExecuted_EnsureResultsAreSaved(Jedis jedis) {
        String key = "my-list";
        assertEquals(new Long(0), jedis.llen(key));

        Transaction transaction = jedis.multi();
        transaction.lpush(key, "1");
        transaction.lpush(key, "2");
        transaction.lpush(key, "3");
        transaction.exec();

        assertEquals(new Long(3), jedis.llen(key));
    }

    @Theory
    public void whenUsingTransactionAndTryingToAccessJedis_Throw(Jedis jedis) {
        //Do Something random with Jedis
        assertNull(jedis.get("oobity-oobity-boo"));

        //Start transaction
        Transaction transaction = jedis.multi();

        expectedException.expect(JedisDataException.class);
        expectedException.expectMessage("Cannot use Jedis when in Multi. Please use Transation or reset jedis state.");

        jedis.get("oobity-oobity-boo");
    }

    @Theory
    public void whenSubscribingToAChannel_EnsurePublishedMessagesAreReceived(Jedis jedis) throws InterruptedException {
        String channel = "normalbob";
        String message = "SUPERBOB";

        //Create subscriber
        ExecutorService subsciberThread = Executors.newSingleThreadExecutor();
        MockSubscriber mockSubscriber = new MockSubscriber();

        Client client = jedis.getClient();
        Jedis subscriber = new Jedis(client.getHost(), client.getPort());

        subsciberThread.submit(() -> subscriber.subscribe(mockSubscriber, channel));

        //Give some time to subscribe
        Thread.sleep(50);

        //publish message
        jedis.publish(channel, message);

        //Give some time for the message to go through
        Thread.sleep(50);

        assertEquals(channel, mockSubscriber.latestChannel());
        assertEquals(message, mockSubscriber.latestMessage());

        mockSubscriber.unsubscribe();
        subsciberThread.shutdownNow();
    }

    //TODO: complete this test
    @Theory
    public void whenSubscribingToAChannelAndThenUnsubscribing_EnsureAllChannelsAreUbSubScribed(Jedis jedis) throws InterruptedException {
        String channel = "normaltim";
        String message = "SUPERTIM";

        //Create subscriber
        ExecutorService subsciberThread = Executors.newSingleThreadExecutor();
        Client client = jedis.getClient();
        Jedis subscriber = new Jedis(client.getHost(), client.getPort());
        subsciberThread.submit(() -> subscriber.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                unsubscribe();
            }
        }, channel));

        //Give some time to subscribe
        Thread.sleep(50);

        //publish message
        jedis.publish(channel, message);
    }

}
