package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import ai.grakn.redismock.SliceParser;

import java.util.Arrays;
import java.util.List;

import static ai.grakn.redismock.Utils.convertToLong;

class RO_brpoplpush extends RO_rpoplpush {
    RO_brpoplpush(RedisBase base, List<Slice> params) {
        //NOTE: The minimum number of arguments is 1 because this mock is used for brpoplpush as well which takes in 3 arguments
        super(base, params, 3);
    }

    @Override
    public Slice execute() {
        Slice source = params().get(0);
        long timeout = convertToLong(params().get(2).toString());

        //TODO: Remove active block dumb.
        long currentSleep = 0L;
        long count = 0L;
        while(count == 0L && currentSleep < timeout * 1000){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            currentSleep = currentSleep + 100;
            count = getCount(source);
        }

        if(count != 0){
            return super.execute();
        } else {
            return Response.NULL;
        }
    }

    private long getCount(Slice source){
        Slice index = new Slice("0");
        List<Slice> commands = Arrays.asList(source, index, index);
        Slice result = new RO_lrange(base(), commands).execute();
        return SliceParser.consumeCount(result.data());
    }
}
