package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_expire extends RO_pexpire {
    RO_expire(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    @Override
    long getValue(List<Slice> params){
        return super.getValue(params) * 1000;
    }
}
