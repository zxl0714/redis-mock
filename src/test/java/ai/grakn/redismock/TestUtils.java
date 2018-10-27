package ai.grakn.redismock;

import ai.grakn.redismock.exception.WrongNumberOfArgumentsException;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class TestUtils {

    @Test
    public void testCloseQuietly() throws IOException {
        Utils.closeQuietly(null);
        Utils.closeQuietly(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        });
    }

    @Test
    public void testCheckArgumentsNumberEquals() throws WrongNumberOfArgumentsException {
        List<Slice> args = Lists.newArrayList(new Slice(""), new Slice(""));
        Utils.checkArgumentsNumberEquals(args, 2);
        try {
            Utils.checkArgumentsNumberEquals(args, 1);
            assertTrue(false);
        } catch (WrongNumberOfArgumentsException e) {
            // ok
        }
        try {
            Utils.checkArgumentsNumberEquals(args, 3);
            assertTrue(false);
        } catch (WrongNumberOfArgumentsException e) {
            // ok
        }
    }

    @Test
    public void testCheckArgumentsNumberGreater() throws WrongNumberOfArgumentsException {
        List<Slice> args = Lists.newArrayList(new Slice(""), new Slice(""));
        Utils.checkArgumentsNumberGreater(args, 1);
        try {
            Utils.checkArgumentsNumberGreater(args, 2);
            assertTrue(false);
        } catch (WrongNumberOfArgumentsException e) {
            // ok
        }
    }

    @Test
    public void testCheckArgumentsNumberFactor() throws WrongNumberOfArgumentsException {
        List<Slice> args = Lists.newArrayList(new Slice(""), new Slice(""), new Slice(""));
        Utils.checkArgumentsNumberFactor(args, 1);
        Utils.checkArgumentsNumberFactor(args, 3);
        try {
            Utils.checkArgumentsNumberFactor(args, 2);
            assertTrue(false);
        } catch (WrongNumberOfArgumentsException e) {
            // ok
        }
    }

    @Test
    public void testSerializeAndDeserialize() throws Exception {
        Slice a = new Slice("abcdef");
        Slice b = Utils.deserializeObject(Utils.serializeObject(a));
        assertArrayEquals(a.data(), b.data());
    }
}
