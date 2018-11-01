package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Slice;

import java.util.LinkedList;
import java.util.List;

class RO_lpop extends RO_pop {
    RO_lpop(RedisBase base,List<Slice> params ) {
        super(base, params);
    }

    @Override
    Slice popper(LinkedList<Slice> list) {
        return list.removeFirst();
    }
}
