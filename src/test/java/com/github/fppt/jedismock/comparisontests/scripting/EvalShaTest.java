package com.github.fppt.jedismock.comparisontests.scripting;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import com.github.fppt.jedismock.operations.scripting.Script;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonBase.class)
class EvalShaTest {

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void evalShaWorks(Jedis jedis) {
        String script =
                "redis.call('SADD', 'foo', 'bar')\n" +
                        "return 'Hello, scripting!'";
        Object evalResult = jedis.eval(script, 0);
        String sha = Script.getScriptSHA(script);
        assertEquals(evalResult, jedis.evalsha(sha, 0));
    }


    @TestTemplate
    public void evalShaWithScriptLoadingWorks(Jedis jedis) {
        String script = "return 'Hello, ' .. ARGV[1] .. '!'";
        String sha = jedis.scriptLoad(script);
        assertEquals("Hello, world!", jedis.evalsha(sha, 0, "world"));
    }

    @TestTemplate
    public void evalShaNotFoundExceptionIsCorrect(Jedis jedis) {
        RuntimeException e = assertThrows(RuntimeException.class, () -> jedis.evalsha("abc", 0));
        assertEquals("NOSCRIPT No matching script. Please use EVAL.", e.getMessage());
    }
}
