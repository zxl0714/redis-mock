package ai.grakn.redismock;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.List;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class Response {

    public static final Slice OK = new Slice("+OK\r\n");
    public static final Slice NULL = new Slice("$-1\r\n");

    private Response() {}

    public static Slice bulkString(Slice s) {
        if (s == null) {
            return NULL;
        }
        ByteArrayDataOutput bo = ByteStreams.newDataOutput();
        bo.write(String.format("$%d\r\n", s.length()).getBytes());
        bo.write(s.data());
        bo.write("\r\n".getBytes());
        return new Slice(bo.toByteArray());
    }

    public static Slice error(String s) {
        return new Slice(String.format("-%s\r\n", s));
    }

    public static Slice integer(long v) {
        return new Slice(String.format(":%d\r\n", v));
    }

    public static Slice array(List<Slice> values) {
        ByteArrayDataOutput bo = ByteStreams.newDataOutput();
        bo.write(String.format("*%d\r\n", values.size()).getBytes());
        for (Slice value : values) {
            bo.write(value.data());
        }
        return new Slice(bo.toByteArray());
    }
}
