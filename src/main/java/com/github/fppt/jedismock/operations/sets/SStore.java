package com.github.fppt.jedismock.operations.sets;

import com.github.fppt.jedismock.datastructures.RMSet;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.operations.AbstractRedisOperation;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public abstract class SStore extends AbstractRedisOperation {
    private final BiFunction<RedisBase, List<Slice>, Set<Slice>> operation;

    public SStore(RedisBase base,
                  List<Slice> params,
                  BiFunction<RedisBase, List<Slice>, Set<Slice>> operation) {
        super(base, params);
        this.operation = operation;
    }

    @Override
    protected final Slice response() {
        Slice key = params().get(0);
        Set<Slice> result = operation.apply(base(), params().subList(1, params().size()));
        if (result.isEmpty()) {
            base().deleteValue(key);
        } else {
            base().putValue(key, new RMSet(result));
        }
        return Response.integer(result.size());
    }
}
