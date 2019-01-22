package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.Collection;
import java.util.List;

import static com.github.fppt.jedismock.Utils.serializeObject;

abstract class RO_pop<V extends Collection<Slice>> extends AbstractRedisOperation {
    RO_pop(RedisBase base, List<Slice> params ) {
        super(base, params);
    }

    abstract Slice popper(V list);

    Slice response() {
        Slice key = params().get(0);
        V list = getDataFromBase(key, null);
        if(list == null || list.isEmpty()) return Response.NULL;
        Slice v = popper(list);
        base().putValue(key, serializeObject(list));
        return Response.bulkString(v);
    }
}
