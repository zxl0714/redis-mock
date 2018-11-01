package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Slice;

import java.util.List;

import static com.github.fppt.jedismock.Utils.convertToLong;

class RO_psetex extends RO_setex {
    RO_psetex(RedisBase base, List<Slice> params) {
        super(base, params, 3);
    }

    @Override
    long valueToSet(List<Slice> params){
        return convertToLong(new String(params.get(1).data()));
    }
}
