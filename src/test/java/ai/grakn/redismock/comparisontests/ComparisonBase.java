package ai.grakn.redismock.comparisontests;

import ai.grakn.redismock.RedisServer;
import ai.grakn.redismock.util.EmbeddedRedis;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.rules.ExpectedException;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class ComparisonBase {
    private static RedisServer fakeServer;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void startupRediseServers() throws IOException {
        //Start up the real redis server
        EmbeddedRedis.start();

        //Start up the fake redis server
        fakeServer = RedisServer.newRedisServer(EmbeddedRedis.PORT + 1);
        fakeServer.start();
    }

    @AfterClass
    public static void takeDownRedisServers(){
        //Kill the real redis server
        EmbeddedRedis.stop();

        //Kill the fake redis server
        fakeServer.stop();
    }

    @DataPoints
    public static Jedis[] jedis(){
        Jedis[] jedis = new Jedis[2];

        //Create mocked jedis connection
        jedis[0] = new Jedis(fakeServer.getHost(), fakeServer.getBindPort());

        //Create real jedis connection
        jedis[1] = new Jedis("localhost", EmbeddedRedis.PORT);

        return jedis;
    }

}
