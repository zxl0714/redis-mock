package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

import static ai.grakn.redismock.Utils.convertToNonNegativeInteger;

class RO_getbit extends AbstractRedisOperation {
    RO_getbit(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    @Override
    public Slice execute() {
        Slice value = base().rawGet(params().get(0));
        int pos = convertToNonNegativeInteger(params().get(1).toString());

        if (value == null) {
            return Response.integer(0L);
        }
        if (pos >= value.length() * 8) {
            return Response.integer(0L);
        }
        if ((value.data()[pos / 8] & (1 << (pos % 8))) != 0) {
            return Response.integer(1);
        }
        return Response.integer(0);
    }
}
