package com.github.zxl0714.redismock;

import com.github.zxl0714.redismock.expecptions.EOFException;
import com.github.zxl0714.redismock.expecptions.ParseErrorException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class RedisCommandParser {

    private final InputStream messageInput;

    @VisibleForTesting
    RedisCommandParser(String stringInput) {
        this(new ByteArrayInputStream(stringInput.getBytes()));
    }

    @VisibleForTesting
    RedisCommandParser(InputStream messageInput) {
        Preconditions.checkNotNull(messageInput);

        this.messageInput = messageInput;
    }

    @VisibleForTesting
    byte consumeByte() throws EOFException {
        int b;
        try {
            b = messageInput.read();
        } catch (IOException e) {
            throw new EOFException();
        }
        if (b == -1) {
            throw new EOFException();
        }
        return (byte) b;
    }

    @VisibleForTesting
    void expectByte(byte c) throws ParseErrorException, EOFException {
        if (consumeByte() != c) {
            throw new ParseErrorException();
        }
    }

    @VisibleForTesting
    long consumeLong() throws ParseErrorException {
        byte c;
        long ret = 0;
        boolean hasLong = false;
        while (true) {
            try {
                c = consumeByte();
            } catch (EOFException e) {
                throw new ParseErrorException();
            }
            if (c == '\r') {
                break;
            }
            if (!isNumber(c)) {
                throw new ParseErrorException();
            }
            ret = ret * 10 + c - '0';
            hasLong = true;
        }
        if (!hasLong) {
            throw new ParseErrorException();
        }
        return ret;
    }

    @VisibleForTesting
    Slice consumeSlice(long len) throws ParseErrorException {
        ByteArrayDataOutput bo = ByteStreams.newDataOutput();
        for (long i = 0; i < len; i++) {
            try {
                bo.write(consumeByte());
            } catch (EOFException e) {
                throw new ParseErrorException();
            }
        }
        return new Slice(bo.toByteArray());
    }

    @VisibleForTesting
    long consumeCount() throws ParseErrorException, EOFException {
        expectByte((byte) '*');
        try {
            long count = consumeLong();
            expectByte((byte) '\n');
            return count;
        } catch (EOFException e) {
            throw new ParseErrorException();
        }
    }

    @VisibleForTesting
    Slice consumeParameter() throws ParseErrorException {
        try {
            expectByte((byte) '$');
            long len = consumeLong();
            expectByte((byte) '\n');
            Slice para = consumeSlice(len);
            expectByte((byte) '\r');
            expectByte((byte) '\n');
            return para;
        } catch (EOFException e) {
            throw new ParseErrorException();
        }
    }

    private static boolean isNumber(byte c) {
        return '0' <= c && c <= '9';
    }

    @VisibleForTesting
    static RedisCommand parse(String stringInput) throws ParseErrorException, EOFException {
        Preconditions.checkNotNull(stringInput);

        return parse(new ByteArrayInputStream(stringInput.getBytes()));
    }

    public static RedisCommand parse(InputStream messageInput) throws ParseErrorException, EOFException {
        Preconditions.checkNotNull(messageInput);

        RedisCommandParser parser = new RedisCommandParser(messageInput);
        long count = parser.consumeCount();
        if (count == 0) {
            throw new ParseErrorException();
        }
        RedisCommand command = new RedisCommand();
        for (long i = 0; i < count; i++) {
            command.addParameter(parser.consumeParameter());
        }
        return command;
    }
}
