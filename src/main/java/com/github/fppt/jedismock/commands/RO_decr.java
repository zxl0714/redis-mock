package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Slice;

import java.util.List;

class RO_decr extends RO_decrby {
    RO_decr(RedisBase base, List<Slice> params) {
        super(base, params, 1);
    }

    @Override
    long incrementOrDecrementValue(List<Slice> params){
        return -1L;
    }
}
