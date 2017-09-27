package ai.grakn.redismock;

import ai.grakn.redismock.expecptions.EOFException;
import ai.grakn.redismock.expecptions.ParseErrorException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class TestCommandParser {

    @Test
    public void testConsumeCharacter() throws ParseErrorException, EOFException {
        RedisCommandParser parser = new RedisCommandParser("a");
        assertEquals(parser.consumeByte(), 'a');
    }

    @Test
    public void testExpectCharacter() throws ParseErrorException, EOFException {
        RedisCommandParser parser = new RedisCommandParser("a");
        parser.expectByte((byte) 'a');
    }

    @Test
    public void testConsumeLong() throws ParseErrorException {
        RedisCommandParser parser = new RedisCommandParser("12345678901234\r");
        assertEquals(parser.consumeLong(), 12345678901234L);
    }

    @Test
    public void testConsumeString() throws ParseErrorException {
        RedisCommandParser parser = new RedisCommandParser("abcd");
        assertEquals(parser.consumeSlice(4).toString(), "abcd");
    }

    @Test
    public void testConsumeCount1() throws ParseErrorException, EOFException {
        RedisCommandParser parser = new RedisCommandParser("*12\r\n");
        assertEquals(parser.consumeCount(), 12L);
    }

    @Test
    public void testConsumeCount2() throws EOFException {
        RedisCommandParser parser = new RedisCommandParser("*2\r");
        try {
            parser.consumeCount();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }


    @Test
    public void testConsumeParameter() throws ParseErrorException {
        RedisCommandParser parser = new RedisCommandParser("$5\r\nabcde\r\n");
        assertEquals(parser.consumeParameter().toString(), "abcde");
    }

    @Test
    public void testParse() throws ParseErrorException, EOFException {
        RedisCommand cmd = RedisCommandParser.parse("*3\r\n$0\r\n\r\n$4\r\nabcd\r\n$2\r\nef\r\n");
        assertEquals(cmd.getParameters().get(0).toString(), "");
        assertEquals(cmd.getParameters().get(1).toString(), "abcd");
        assertEquals(cmd.getParameters().get(2).toString(), "ef");
    }

    @Test
    public void testConsumeCharacterError() throws ParseErrorException {
        RedisCommandParser parser = new RedisCommandParser("");
        try {
            parser.consumeByte();
            assertTrue(false);
        } catch (EOFException e) {
            // OK
        }
    }

    @Test
    public void testExpectCharacterError1() throws EOFException {
        RedisCommandParser parser = new RedisCommandParser("a");
        try {
            parser.expectByte((byte) 'b');
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testExpectCharacterError2() throws ParseErrorException {
        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException();
            }
        };
        RedisCommandParser parser = new RedisCommandParser(in);
        try {
            parser.expectByte((byte) 'b');
            assertTrue(false);
        } catch (EOFException e) {
            // OK
        }
    }

    @Test
    public void testConsumeLongError1() {
        RedisCommandParser parser = new RedisCommandParser("\r");
        try {
            parser.consumeLong();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeLongError2() {
        RedisCommandParser parser = new RedisCommandParser("100a");
        try {
            parser.consumeLong();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeLongError3() {
        RedisCommandParser parser = new RedisCommandParser("");
        try {
            parser.consumeLong();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeStringError() {
        RedisCommandParser parser = new RedisCommandParser("abc");
        try {
            parser.consumeSlice(4);
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeCountError1() throws EOFException {
        RedisCommandParser parser = new RedisCommandParser("$12\r\n");
        try {
            parser.consumeCount();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeCountError2() throws EOFException {
        RedisCommandParser parser = new RedisCommandParser("*12\ra");
        try {
            parser.consumeCount();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeParameterError1() {
        RedisCommandParser parser = new RedisCommandParser("$4\r\nabcde\r\n");
        try {
            parser.consumeParameter();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeParameterError2() {
        RedisCommandParser parser = new RedisCommandParser("$4\r\nabc\r\n");
        try {
            parser.consumeParameter();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeParameterError3() {
        RedisCommandParser parser = new RedisCommandParser("$4\r\nabc");
        try {
            parser.consumeParameter();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testConsumeParameterError4() {
        RedisCommandParser parser = new RedisCommandParser("$4\r");
        try {
            parser.consumeParameter();
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }

    @Test
    public void testParseError() throws ParseErrorException, EOFException {
        try {
            RedisCommandParser.parse("*0\r\n");
            assertTrue(false);
        } catch (ParseErrorException e) {
            // OK
        }
    }
}
