package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Slice;

import java.util.LinkedList;
import java.util.List;

class RO_rpush extends RO_push {
    RO_rpush(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    @Override
    void pusher(LinkedList<Slice> list, Slice slice) {
        list.addLast(slice);
    }

}
