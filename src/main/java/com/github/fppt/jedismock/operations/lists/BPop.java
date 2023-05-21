package com.github.fppt.jedismock.operations.lists;

import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.exception.WrongValueTypeException;
import com.github.fppt.jedismock.operations.AbstractRedisOperation;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.server.SliceParser;
import com.github.fppt.jedismock.storage.OperationExecutorState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.fppt.jedismock.Utils.convertToDouble;

abstract class BPop extends AbstractRedisOperation {

    private final Object lock;
    private final boolean isInTransaction;

    BPop(OperationExecutorState state, List<Slice> params) {
        super(state.base(), params);
        this.lock = state.lock();
        this.isInTransaction = state.isTransactionModeOn();
    }

    abstract AbstractRedisOperation popper(List<Slice> params);

    protected Slice response() {
        int size = params().size();
        if (size < 2) {
            throw new IndexOutOfBoundsException("require at least 2 params");
        }
        List<Slice> keys = params().subList(0, size - 1);
        long timeoutNanos = (long) (convertToDouble(params().get(size - 1).toString()) * 1_000_000_000L);

        if (timeoutNanos < 0) {
            throw new IllegalArgumentException("ERR timeout is negative");
        }

        Slice source = getKey(keys, true);

        long waitEnd = System.nanoTime() + timeoutNanos;
        long waitTimeNanos;
        try {
            while (source == null &&
                    !isInTransaction &&
                    (waitTimeNanos = timeoutNanos == 0 ? 0 : waitEnd - System.nanoTime()) >= 0) {
                lock.wait(waitTimeNanos / 1_000_000, (int) waitTimeNanos % 1_000_000);
                source = getKey(keys, false);
            }
        } catch (InterruptedException e) {
            //wait interrupted prematurely
            Thread.currentThread().interrupt();
            return Response.NULL;
        }
        if (source != null) {
            Slice result = popper(Collections.singletonList(source)).execute();
            return Response.array(Arrays.asList(Response.bulkString(source), result));
        } else {
            System.out.println("Source is still null");
            return Response.NULL;
        }
    }

    private Slice getKey(List<Slice> list, boolean checkForType) {
        for (Slice key : list) {
            if (!base().exists(key)) {
                continue;
            }
            Slice result;
            try {
                result = new LLen(base(), Collections.singletonList(key)).execute();
            } catch (WrongValueTypeException e) {
                if (checkForType) {
                    throw e;
                }
                continue;
            }
            int length = SliceParser.consumeInteger(result.data());
            if (length > 0) {
                return key;
            }
        }
        return null;
    }
}
