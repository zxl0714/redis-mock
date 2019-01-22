package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Slice;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

class RO_spop extends RO_pop<Set<Slice>> {
    RO_spop(RedisBase base, List<Slice> params ) {
        super(base, params);
    }

    @Override
    Slice popper(Set<Slice> collection) {
        Iterator<Slice> it = collection.iterator();
        Slice v = it.next();
        it.remove();
        return v;
    }
}
