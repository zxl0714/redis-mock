package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_lpushx extends RO_lpush {
    RO_lpushx(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    Slice response(){
        Slice key = params().get(0);
        Slice data = base().rawGet(key);

        if(data != null){
            return super.response();
        }

        return Response.integer(0);
    }
}
