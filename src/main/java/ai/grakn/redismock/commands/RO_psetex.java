package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Slice;

import java.util.List;

import static ai.grakn.redismock.Utils.convertToLong;

class RO_psetex extends RO_setex {
    RO_psetex(RedisBase base, List<Slice> params) {
        super(base, params, 3);
    }

    @Override
    long valueToSet(List<Slice> params){
        return convertToLong(new String(params.get(1).data()));
    }
}
