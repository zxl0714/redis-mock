package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Slice;
import ai.grakn.redismock.SliceParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class RO_rpoplpush extends AbstractRedisOperation {
    RO_rpoplpush(RedisBase base, List<Slice> params) {
        //NOTE: The minimum number of arguments is 1 because this mock is used for brpoplpush as well which takes in 3 arguments
        super(base, params, null, 1, null);
    }

    @Override
    public Slice execute() {
        Slice list1Key = params().get(0);
        Slice list2Key = params().get(1);

        //Pop last one
        Slice result = new RO_rpop(base(), Collections.singletonList(list1Key)).execute();
        Slice valueToPush = SliceParser.consumeParameter(result.data());

        //Push it into the other list
        new RO_lpush(base(), Arrays.asList(list2Key, valueToPush)).execute();

        return result;
    }
}
