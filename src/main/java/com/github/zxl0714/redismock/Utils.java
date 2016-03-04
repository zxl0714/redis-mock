package com.github.zxl0714.redismock;

import com.github.zxl0714.redismock.expecptions.WrongNumberOfArgumentsException;

import java.io.*;
import java.util.List;

/**
 * Created by Xiaolu on 2015/4/21.
 */
public class Utils {

    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            // ignore
        }
    }

    public static void checkArgumentsNumberEquals(List<Slice> args, int expect) throws WrongNumberOfArgumentsException {
        if (args.size() != expect) {
            throw new WrongNumberOfArgumentsException();
        }
    }

    public static void checkArgumentsNumberGreater(List<Slice> args, int expect) throws WrongNumberOfArgumentsException {
        if (args.size() <= expect) {
            throw new WrongNumberOfArgumentsException();
        }
    }

    public static void checkArgumentsNumberFactor(List<Slice> args, int factor) throws WrongNumberOfArgumentsException {
        if (args.size() % factor != 0) {
            throw new WrongNumberOfArgumentsException();
        }
    }

    public static Slice serializeObject(Object o) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(o);
        Slice encode = new Slice(bo.toByteArray());
        oo.close();
        bo.close();
        return encode;
    }

    public static <T> T deserializeObject(Slice data) throws Exception {
        ByteArrayInputStream bi = new ByteArrayInputStream(data.data());
        ObjectInputStream oi = new ObjectInputStream(bi);
        T ret = (T) oi.readObject();
        oi.close();
        bi.close();
        return ret;
    }
}
