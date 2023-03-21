package com.github.fppt.jedismock.operations.lists;

import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.operations.AbstractRedisOperation;
import com.github.fppt.jedismock.operations.RedisCommand;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.List;

@RedisCommand("linsert")
public class LInsert extends AbstractRedisOperation {
    private static final String BEFORE = "BEFORE";
    public LInsert(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    @Override
    protected Slice response() {
        Slice key = params().get(0);
        String direction = params().get(1).toString();
        Slice pivot = params().get(2);
        Slice element = params().get(3);

        if (!base().exists(key)) {
            return Response.integer(0);
        }

        List<Slice> storedElements = base().getList(key).getStoredData();
        int i = storedElements.indexOf(pivot);

        if (i == -1) {
            return Response.integer(-1);
        }

        int index = direction.equalsIgnoreCase(BEFORE) ? i : i + 1;
        storedElements.add(index, element);
        return Response.integer(storedElements.size());
    }
}
