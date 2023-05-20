package com.github.fppt.jedismock.operations.sets;

import com.github.fppt.jedismock.datastructures.RMSet;
import com.github.fppt.jedismock.operations.AbstractRedisOperation;
import com.github.fppt.jedismock.operations.RedisCommand;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.storage.RedisBase;
import com.github.fppt.jedismock.datastructures.Slice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.github.fppt.jedismock.Utils.convertToInteger;

@RedisCommand("spop")
class SPop extends AbstractRedisOperation {
    SPop(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    private List<Slice> popper(Set<Slice> collection, int number) {
        List<Slice> result = new ArrayList<>();
        Iterator<Slice> it = collection.iterator();
        while (number > 0 && it.hasNext()) {
            Slice v = it.next();
            result.add(Response.bulkString(v));
            it.remove();
            number--;
        }
        return result;
    }

    protected Slice response() {
        Slice key = params().get(0);
        int number = params().size() > 1 ?
                convertToInteger(params().get(1).toString()) : 1;

        final RMSet setDBObj = getSetFromBaseOrCreateEmpty(key);
        Set<Slice> data = setDBObj.getStoredData();
        if (data == null || data.isEmpty()) return Response.NULL;
        List<Slice> v = popper(data, number);
        if (v.size() == 1) {
            return v.get(0);
        } else {
            return Response.array(v);
        }
    }
}
