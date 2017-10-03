package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.RedisClient;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import ai.grakn.redismock.SliceParser;
import ai.grakn.redismock.util.WorkerManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static ai.grakn.redismock.Utils.convertToLong;

class RO_brpoplpush extends RO_rpoplpush {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RO_unsubscribe.class);
    private final RedisClient client;

    RO_brpoplpush(RedisBase base, RedisClient client, List<Slice> params) {
        //NOTE: The minimum number of arguments is 1 because this mock is used for brpoplpush as well which takes in 3 arguments
        super(base, params, 3);
        this.client = client;
    }

    @Override
    public Slice execute() {
        Slice source = params().get(0);
        long timeout = convertToLong(params().get(2).toString());

        WorkerManager.runJob(() -> {
            //TODO: Active block dumb.
            long currentSleep = 0L;
            long count = 0L;
            while(count == 0L && currentSleep < timeout * 1000){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                currentSleep = currentSleep + 10;
                count = getCount(source);
            }

            Slice response;
            if(count != 0){
                response = super.execute();
            } else {
                response = Response.NULL;
            }

            try {
                client.sendResponse(Response.clientResponse("brpoplpush", response));
            } catch (IOException e) {
                LOG.error("Unable to respond to brpoplpush", e);
            }
        });

        return Response.SKIP;
    }

    private long getCount(Slice source){
        Slice index = new Slice("0");
        List<Slice> commands = Arrays.asList(source, index, index);
        Slice result = new RO_lrange(base(), commands).execute();
        return SliceParser.consumeCount(result.data());
    }
}
