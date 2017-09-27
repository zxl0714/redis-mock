package ai.grakn.redismock;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ai.grakn.redismock.expecptions.WrongNumberOfArgumentsException;
import ai.grakn.redismock.expecptions.WrongValueTypeException;
import ai.grakn.redismock.expecptions.InternalException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static ai.grakn.redismock.Utils.*;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class CommandExecutor {

    private final RedisBase base;

    public static List<String> getSupportedCommands() {
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();
        Method[] methods = CommandExecutor.class.getMethods();
        for (Method method : methods) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].getName().equals(List.class.getName())
                    && method.getReturnType().getName().equals(Slice.class.getName())) {
                builder.add(method.getName());
            }
        }
        return builder.build();
    }

    public CommandExecutor(RedisBase base) {
        this.base = base;
    }

    public Slice set(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 2);

        base.rawPut(params.get(0), params.get(1), -1L);
        return Response.OK;
    }

    public Slice setex(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 3);

        try {
            long ttl = Long.parseLong(new String(params.get(1).data())) * 1000;
            base.rawPut(params.get(0), params.get(2), ttl);
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
        return Response.OK;
    }

    public Slice psetex(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 3);

        try {
            long ttl = Long.parseLong(new String(params.get(1).data()));
            base.rawPut(params.get(0), params.get(2), ttl);
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
        return Response.OK;
    }

    public Slice setnx(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 2);

        if (base.rawGet(params.get(0)) == null) {
            base.rawPut(params.get(0), params.get(1), -1L);
            return Response.integer(1);
        }
        return Response.integer(0);
    }

    public Slice setbit(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 3);

        Slice value = base.rawGet(params.get(0));
        byte bit;
        try {
            bit = Byte.parseByte(params.get(2).toString());
            if (bit != 0 && bit != 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR bit is not an integer or out of range");
        }
        int pos;
        try {
            pos = Integer.parseInt(params.get(1).toString());
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR bit offset is not an integer or out of range");
        }
        if (pos < 0) {
            throw new WrongValueTypeException("ERR bit offset is not an integer or out of range");
        }
        if (value == null) {
            byte[] data = new byte[pos / 8 + 1];
            Arrays.fill(data, (byte) 0);
            data[pos / 8] = (byte) (bit << (pos % 8));
            base.rawPut(params.get(0), new Slice(data), -1L);
            return Response.integer(0L);
        }
        long original;
        if (pos / 8 >= value.length()) {
            byte[] data = new byte[pos / 8 + 1];
            Arrays.fill(data, (byte) 0);
            for (int i = 0; i < value.length(); i++) {
                data[i] = value.data()[i];
            }
            data[pos / 8] = (byte) (bit << (pos % 8));
            original = 0;
            base.rawPut(params.get(0), new Slice(data), -1L);
        } else {
            byte[] data = value.data();
            if ((data[pos / 8] & (1 << (pos % 8))) != 0) {
                original = 1;
            } else {
                original = 0;
            }
            data[pos / 8] |= (byte) (1 << (pos % 8));
            data[pos / 8] &= (byte) (bit << (pos % 8));
            base.rawPut(params.get(0), new Slice(data), -1L);
        }
        return Response.integer(original);
    }

    public Slice append(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 2);

        Slice key = params.get(0);
        Slice value = params.get(1);
        Slice s = base.rawGet(key);
        if (s == null) {
            base.rawPut(key, value, -1L);
            return Response.integer(value.length());
        }
        byte[] b = new byte[s.length() + value.length()];
        for (int i = 0; i < s.length(); i++) {
            b[i] = s.data()[i];
        }
        for (int i = s.length(); i < s.length() + value.length(); i++) {
            b[i] = value.data()[i - s.length()];
        }
        base.rawPut(key, new Slice(b), -1L);
        return Response.integer(b.length);
    }

    public Slice get(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 1);

        Slice value = base.rawGet(params.get(0));
        return Response.bulkString(value);
    }

    public Slice getbit(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        Slice value = base.rawGet(params.get(0));
        int pos;
        try {
            pos = Integer.parseInt(params.get(1).toString());
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR bit offset is not an integer or out of range");
        }
        if (pos < 0) {
            throw new WrongValueTypeException("ERR bit offset is not an integer or out of range");
        }
        if (value == null) {
            return Response.integer(0L);
        }
        if (pos >= value.length() * 8) {
            return Response.integer(0L);
        }
        if ((value.data()[pos / 8] & (1 << (pos % 8))) != 0) {
            return Response.integer(1);
        }
        return Response.integer(0);
    }

    public Slice ttl(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 1);

        Long pttl = base.getTTL(params.get(0));
        if (pttl == null) {
            return Response.integer(-2L);
        }
        if (pttl == -1) {
            return Response.integer(-1L);
        }
        return Response.integer((pttl + 999) / 1000);
    }

    public Slice pttl(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 1);

        Long pttl = base.getTTL(params.get(0));
        if (pttl == null) {
            return Response.integer(-2L);
        }
        if (pttl == -1) {
            return Response.integer(-1L);
        }
        return Response.integer(pttl);
    }

    public Slice expire(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        try {
            long pttl = Long.parseLong(new String(params.get(1).data())) * 1000;
            return Response.integer(base.setTTL(params.get(0), pttl));
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice pexpire(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        try {
            long pttl = Long.parseLong(new String(params.get(1).data()));
            return Response.integer(base.setTTL(params.get(0), pttl));
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice incr(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 1);

        Slice key = params.get(0);
        Slice v = base.rawGet(key);
        if (v == null) {
            base.rawPut(key, new Slice("1"), -1L);
            return Response.integer(1L);
        }
        try {
            long r = Long.parseLong(new String(v.data())) + 1;
            base.rawPut(key, new Slice(String.valueOf(r)), -1L);
            return Response.integer(r);
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice incrby(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        try {
            Slice key = params.get(0);
            long d = Long.parseLong(String.valueOf(params.get(1)));
            Slice v = base.rawGet(key);
            if (v == null) {
                base.rawPut(key, new Slice(String.valueOf(d)), -1L);
                return Response.integer(d);
            }
            long r = Long.parseLong(new String(v.data())) + d;
            base.rawPut(key, new Slice(String.valueOf(r)), -1L);
            return Response.integer(r);
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice decr(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 1);

        Slice key = params.get(0);
        Slice v = base.rawGet(key);
        if (v == null) {
            base.rawPut(key, new Slice("-1"), -1L);
            return Response.integer(-1L);
        }
        try {
            long r = Long.parseLong(new String(v.data())) - 1;
            base.rawPut(key, new Slice(String.valueOf(r)), -1L);
            return Response.integer(r);
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice decrby(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        try {
            Slice key = params.get(0);
            long d = Long.parseLong(String.valueOf(params.get(1)));
            Slice v = base.rawGet(key);
            if (v == null) {
                base.rawPut(key, new Slice(String.valueOf(-d)), -1L);
                return Response.integer(-d);
            }
            long r = Long.parseLong(new String(v.data())) - d;
            base.rawPut(key, new Slice(String.valueOf(r)), -1L);
            return Response.integer(r);
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice pfcount(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberGreater(params, 0);

        Set<Slice> set = Sets.newHashSet();
        for (Slice key : params) {
            Slice data = base.rawGet(key);
            if (data == null) {
                continue;
            }
            try {
                Set<Slice> s = deserializeObject(data);
                set.addAll(s);
            } catch (Exception e) {
                throw new WrongValueTypeException("WRONGTYPE Key is not a valid HyperLogLog string value.");
            }
        }
        return Response.integer((long) set.size());
    }

    public Slice pfadd(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException, InternalException {
        checkArgumentsNumberGreater(params, 1);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        boolean first;
        Set<Slice> set;
        int prev;
        if (data == null) {
            set = Sets.newHashSet();
            first = true;
            prev = 0;
        } else {
            try {
                set = deserializeObject(data);
            } catch (Exception e) {
                throw new WrongValueTypeException("WRONGTYPE Key is not a valid HyperLogLog string value.");
            }
            first = false;
            prev = set.size();
        }
        for (Slice v : params.subList(1, params.size())) {
            set.add(v);
        }
        try {
            Slice out = serializeObject(set);
            if (first) {
                base.rawPut(key, out, -1L);
            } else {
                base.rawPut(key, out, null);
            }
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        if (prev != set.size()) {
            return Response.integer(1L);
        }
        return Response.integer(0L);
    }

    public Slice pfmerge(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException, InternalException {
        checkArgumentsNumberGreater(params, 0);

        Slice dst = params.get(0);
        Slice data = base.rawGet(dst);
        boolean first;
        Set<Slice> set;
        if (data == null) {
            set = Sets.newHashSet();
            first = true;
        } else {
            try {
                set = deserializeObject(data);
            } catch (Exception e) {
                throw new WrongValueTypeException("WRONGTYPE Key is not a valid HyperLogLog string value.");
            }
            first = false;
        }
        for (Slice v : params.subList(1, params.size())) {
            Slice src = base.rawGet(v);
            if (src != null) {
                try {
                    Set<Slice> s = deserializeObject(src);
                    set.addAll(s);
                } catch (Exception e) {
                    throw new WrongValueTypeException("WRONGTYPE Key is not a valid HyperLogLog string value.");
                }
            }
        }
        try {
            Slice out = serializeObject(set);
            if (first) {
                base.rawPut(dst, out, -1L);
            } else {
                base.rawPut(dst, out, null);
            }
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        return Response.OK;
    }

    public Slice mget(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberGreater(params, 0);

        ImmutableList.Builder<Slice> builder = new ImmutableList.Builder<Slice>();
        for (Slice key : params) {
            builder.add(Response.bulkString(base.rawGet(key)));

        }
        return Response.array(builder.build());
    }

    public Slice mset(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberGreater(params, 0);
        checkArgumentsNumberFactor(params, 2);

        for (int i = 0; i < params.size(); i += 2) {
            base.rawPut(params.get(i), params.get(i + 1), -1L);
        }
        return Response.OK;
    }

    public Slice getset(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 2);

        Slice value = base.rawGet(params.get(0));
        base.rawPut(params.get(0), params.get(1), -1L);
        return Response.bulkString(value);
    }

    public Slice strlen(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 1);

        Slice value = base.rawGet(params.get(0));
        if (value == null) {
            return Response.integer(0);
        }
        return Response.integer(value.length());
    }

    public Slice del(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberGreater(params, 0);

        int count = 0;
        for (Slice key : params) {
            Slice value = base.rawGet(key);
            base.del(key);
            if (value != null) {
                count++;
            }
        }
        return Response.integer(count);
    }

    public Slice exists(List<Slice> params) throws WrongNumberOfArgumentsException {
        checkArgumentsNumberEquals(params, 1);

        if (base.rawGet(params.get(0)) != null) {
            return Response.integer(1);
        }
        return Response.integer(0);
    }

    public Slice expireat(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        try {
            long deadline = Long.parseLong(new String(params.get(1).data())) * 1000;
            return Response.integer(base.setDeadline(params.get(0), deadline));
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice pexpireat(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        try {
            long deadline = Long.parseLong(new String(params.get(1).data()));
            return Response.integer(base.setDeadline(params.get(0), deadline));
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
    }

    public Slice lpush(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException, InternalException {
        checkArgumentsNumberGreater(params, 1);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        LinkedList<Slice> list;
        try {
            if (data != null) {
                list = deserializeObject(data);
            } else {
                list = Lists.newLinkedList();
            }
        } catch (Exception e) {
            throw new WrongValueTypeException("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        for (int i = 1; i < params.size(); i++) {
            list.addFirst(params.get(i));
        }
        try {
            base.rawPut(key, serializeObject(list), -1L);
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        return Response.integer(list.size());
    }

    public Slice lpushx(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException, InternalException {
        checkArgumentsNumberGreater(params, 1);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        LinkedList<Slice> list;
        try {
            if (data != null) {
                list = deserializeObject(data);
            } else {
                return Response.integer(0);
            }
        } catch (Exception e) {
            throw new WrongValueTypeException("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        for (int i = 1; i < params.size(); i++) {
            list.addFirst(params.get(i));
        }
        try {
            base.rawPut(key, serializeObject(list), -1L);
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        return Response.integer(list.size());
    }

    public Slice lrange(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 3);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        LinkedList<Slice> list;
        try {
            if (data != null) {
                list = deserializeObject(data);
            } else {
                list = Lists.newLinkedList();
            }
        } catch (Exception e) {
            throw new WrongValueTypeException("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        int start;
        int end;
        try {
            start = Integer.parseInt(params.get(1).toString());
            end = Integer.parseInt(params.get(2).toString());
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
        if (start < 0) {
            start = list.size() + start;
            if (start < 0) {
                start = 0;
            }
        }
        if (end < 0) {
            end = list.size() + end;
            if (end < 0) {
                end = 0;
            }
        }
        ImmutableList.Builder<Slice> builder = new ImmutableList.Builder<Slice>();
        for (int i = start; i <= end && i < list.size(); i++) {
            builder.add(Response.bulkString(list.get(i)));
        }
        return Response.array(builder.build());
    }

    public Slice llen(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 1);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        LinkedList<Slice> list;
        try {
            if (data != null) {
                list = deserializeObject(data);
            } else {
                list = Lists.newLinkedList();
            }
        } catch (Exception e) {
            throw new WrongValueTypeException("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        return Response.integer(list.size());
    }

    public Slice lpop(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException, InternalException {
        checkArgumentsNumberEquals(params, 1);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        LinkedList<Slice> list;
        try {
            if (data != null) {
                list = deserializeObject(data);
            } else {
                return Response.NULL;
            }
        } catch (Exception e) {
            throw new WrongValueTypeException("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        if (list.isEmpty()) {
            return Response.NULL;
        }
        Slice v = list.removeFirst();
        try {
            base.rawPut(key, serializeObject(list), -1L);
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        return Response.bulkString(v);
    }

    public Slice lindex(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException {
        checkArgumentsNumberEquals(params, 2);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        LinkedList<Slice> list;
        try {
            if (data != null) {
                list = deserializeObject(data);
            } else {
                return Response.NULL;
            }
        } catch (Exception e) {
            throw new WrongValueTypeException("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        int index;
        try {
            index = Integer.parseInt(params.get(1).toString());
        } catch (NumberFormatException e) {
            throw new WrongValueTypeException("ERR value is not an integer or out of range");
        }
        if (index < 0) {
            index = list.size() + index;
            if (index < 0) {
                return Response.NULL;
            }
        }
        if (index >= list.size()) {
            return Response.NULL;
        }
        return Response.bulkString(list.get(index));
    }

    public Slice rpush(List<Slice> params) throws WrongNumberOfArgumentsException, WrongValueTypeException, InternalException {
        checkArgumentsNumberGreater(params, 1);

        Slice key = params.get(0);
        Slice data = base.rawGet(key);
        LinkedList<Slice> list;
        try {
            if (data != null) {
                list = deserializeObject(data);
            } else {
                list = Lists.newLinkedList();
            }
        } catch (Exception e) {
            throw new WrongValueTypeException("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        for (int i = 1; i < params.size(); i++) {
            list.addLast(params.get(i));
        }
        try {
            base.rawPut(key, serializeObject(list), -1L);
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        return Response.integer(list.size());
    }

    public synchronized Slice execCommand(RedisCommand command) {
        Preconditions.checkArgument(command.getParameters().size() > 0);

        List<Slice> params = command.getParameters();
        String name = new String(params.get(0).data()).toLowerCase();
        try {
            Method method = this.getClass().getMethod(name, List.class);
            return (Slice) method.invoke(this, params.subList(1, params.size()));
        } catch (IllegalAccessException e) {
            throw new NoSuchElementException();
        } catch (InvocationTargetException e) {
            Throwable err = e.getTargetException();
            if (err instanceof WrongValueTypeException) {
                return Response.error(err.getMessage());
            } else if (err instanceof WrongNumberOfArgumentsException) {
                return Response.error(String.format("ERR wrong number of arguments for '%s' command", name));
            }
            return Response.error(e.getMessage());
        } catch (NoSuchMethodException e) {
            return Response.error(String.format("ERR unknown or disabled command '%s'", name));
        }
    }
}
