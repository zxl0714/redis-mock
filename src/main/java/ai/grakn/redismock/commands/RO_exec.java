package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

class RO_exec extends AbstractRedisOperation {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RO_exec.class);
    private final List<RedisOperation> transaction;

    RO_exec(RedisBase base, List<RedisOperation> transaction, List<Slice> params) {
        super(base, params,0, null, null);
        this.transaction = transaction;
    }

    Slice response() {
        try {
            List<Slice> results = transaction.stream().
                    map(RedisOperation::execute).
                    collect(Collectors.toList());
            transaction.clear();
            return Response.array(results);
        } catch (Throwable t){
            LOG.error("ERROR during committing transaction", t);
            return Response.NULL;
        }
    }
}
