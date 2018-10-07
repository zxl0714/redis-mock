package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

import static ai.grakn.redismock.Utils.convertToLong;

class RO_setex extends RO_set {
    RO_setex(RedisBase base, List<Slice> params) {
        super(base, params, 3);
    }

    RO_setex(RedisBase base, List<Slice> params, Integer expectedParams) {
        super(base, params, expectedParams);
    }

    @Override
    long valueToSet(List<Slice> params){
        return convertToLong(new String(params.get(1).data())) * 1000;
    }

    Slice response() {
        base().rawPut(params().get(0), params().get(2), valueToSet(params()));
        return Response.OK;
    }
}
