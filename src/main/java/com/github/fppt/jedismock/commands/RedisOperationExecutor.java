package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.RedisClient;
import com.github.fppt.jedismock.RedisCommand;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;
import com.github.fppt.jedismock.exception.WrongValueTypeException;
import com.google.common.base.Preconditions;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class RedisOperationExecutor {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RedisOperationExecutor.class);
    private final RedisClient owner;
    private final RedisBase base;
    private boolean transactionModeOn;
    private List<RedisOperation> transaction;

    public RedisOperationExecutor(RedisBase base, RedisClient owner) {
        this.base = base;
        this.owner = owner;
        transactionModeOn = false;
        transaction = new ArrayList<>();
    }

    private RedisOperation buildSimpleOperation(String name, List<Slice> params){
        RedisOperations foundOperation = RedisOperations.valueOf(name);

        switch(foundOperation){
            case SET:
                return new RO_set(base, params);
            case SETEX:
                return new RO_setex(base, params);
            case PSETEX:
                return new RO_psetex(base, params);
            case SETNX:
                return new RO_setnx(base, params);
            case SETBIT:
                return new RO_setbit(base, params);
            case APPEND:
                return new RO_append(base, params);
            case GET:
                return new RO_get(base, params);
            case GETBIT:
                return new RO_getbit(base, params);
            case TTL:
                return new RO_ttl(base, params);
            case PTTL:
                return new RO_pttl(base, params);
            case EXPIRE:
                return new RO_expire(base, params);
            case PEXPIRE:
                return new RO_pexpire(base, params);
            case INCR:
                return new RO_incr(base, params);
            case INCRBY:
                return new RO_incrby(base, params);
            case DECR:
                return new RO_decr(base, params);
            case DECRBY:
                return new RO_decrby(base, params);
            case PFCOUNT:
                return new RO_pfcount(base, params);
            case PFADD:
                return new RO_pfadd(base, params);
            case PFMERGE:
                return new RO_pfmerge(base, params);
            case MGET:
                return new RO_mget(base, params);
            case MSET:
                return new RO_mset(base, params);
            case GETSET:
                return new RO_getset(base, params);
            case STRLEN:
                return new RO_strlen(base, params);
            case DEL:
                return new RO_del(base, params);
            case EXISTS:
                return new RO_exists(base, params);
            case EXPIREAT:
                return new RO_expireat(base, params);
            case PEXPIREAT:
                return new RO_pexpireat(base, params);
            case LPUSH:
                return new RO_lpush(base, params);
            case RPUSH:
                return new RO_rpush(base, params);
            case LPUSHX:
                return new RO_lpushx(base, params);
            case LRANGE:
                return new RO_lrange(base, params);
            case LLEN:
                return new RO_llen(base, params);
            case LPOP:
                return new RO_lpop(base, params);
            case RPOP:
                return new RO_rpop(base, params);
            case LINDEX:
                return new RO_lindex(base, params);
            case RPOPLPUSH:
                return new RO_rpoplpush(base, params);
            case BRPOPLPUSH:
                return new RO_brpoplpush(base, params);
            case SUBSCRIBE:
                return new RO_subscribe(base, owner, params);
            case UNSUBSCRIBE:
                return new RO_unsubscribe(base, owner, params);
            case PUBLISH:
                return new RO_publish(base, params);
            case FLUSHALL:
                return new RO_flushall(base, params);
            case LREM:
                return new RO_lrem(base, params);
            case QUIT:
                return new RO_quit(base, owner, params);
            case EXEC:
                transactionModeOn = false;
                return new RO_exec(base, transaction, params);
            case PING:
                return new RO_ping(base, params);
            case KEYS:
                return new RO_keys(base, params);
            case SADD:
                return new RO_sadd(base, params);
            case SMEMBERS:
                return new RO_smembers(base, params);
            case SPOP:
                return new RO_spop(base, params);
            case HGET:
                return new RO_hget(base, params);
            case HSET:
                return new RO_hset(base, params);
            case HDEL:
                return new RO_hdel(base, params);
            case HGETALL:
                return new RO_hegetall(base, params);
            case SINTER:
                return new RO_sinter(base, params);
            case HMGET:
                return new RO_hmget(base, params);
            case HMSET:
                return new RO_hmset(base, params);
            default:
                throw new UnsupportedOperationException(String.format("Unsupported operation '%s'", name));
        }
    }

    public synchronized Slice execCommand(RedisCommand command) {
        Preconditions.checkArgument(command.parameters().size() > 0);
        List<Slice> params = command.parameters();
        List<Slice> commandParams = params.subList(1, params.size());
        String name = new String(params.get(0).data()).toUpperCase();

        try {
            //Meta Command handling
            if(name.equals(RedisOperations.MULTI.name())){
                newTransaction();
                return Response.clientResponse(name, Response.OK);
            }

            //Checking if we mutating the transaction or the base
            RedisOperation redisOperation = buildSimpleOperation(name, commandParams);
            if(transactionModeOn){
                transaction.add(redisOperation);
            } else {
                return Response.clientResponse(name, redisOperation.execute());
            }

            return Response.clientResponse(name, Response.OK);
        } catch(UnsupportedOperationException | WrongValueTypeException | IllegalArgumentException e){
            LOG.error("Malformed request", e);
            return Response.error(e.getMessage());
        }
    }

    private void newTransaction(){
        if(transactionModeOn) throw new RuntimeException("Redis mock does not support more than one transaction");
        transactionModeOn = true;
    }
}
