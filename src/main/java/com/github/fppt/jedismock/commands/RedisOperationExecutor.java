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
import java.util.Map;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class RedisOperationExecutor {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RedisOperationExecutor.class);
    private final RedisClient owner;
    private final Map<Integer, RedisBase> redisBases;
    private boolean transactionModeOn;
    private List<RedisOperation> transaction;
    private int selectedRedisBase = 0;

    public RedisOperationExecutor(Map<Integer, RedisBase> redisBases, RedisClient owner) {
        this.redisBases = redisBases;
        this.owner = owner;
        transactionModeOn = false;
        transaction = new ArrayList<>();
    }

    private RedisBase getCurrentBase(){
        return redisBases.computeIfAbsent(selectedRedisBase, key -> new RedisBase());
    }

    private RedisOperation buildSimpleOperation(String name, List<Slice> params){
        RedisOperations foundOperation = RedisOperations.valueOf(name);

        switch(foundOperation){
            case SET:
                return new RO_set(getCurrentBase(), params);
            case SETEX:
                return new RO_setex(getCurrentBase(), params);
            case PSETEX:
                return new RO_psetex(getCurrentBase(), params);
            case SETNX:
                return new RO_setnx(getCurrentBase(), params);
            case SETBIT:
                return new RO_setbit(getCurrentBase(), params);
            case APPEND:
                return new RO_append(getCurrentBase(), params);
            case GET:
                return new RO_get(getCurrentBase(), params);
            case GETBIT:
                return new RO_getbit(getCurrentBase(), params);
            case TTL:
                return new RO_ttl(getCurrentBase(), params);
            case PTTL:
                return new RO_pttl(getCurrentBase(), params);
            case EXPIRE:
                return new RO_expire(getCurrentBase(), params);
            case PEXPIRE:
                return new RO_pexpire(getCurrentBase(), params);
            case INCR:
                return new RO_incr(getCurrentBase(), params);
            case INCRBY:
                return new RO_incrby(getCurrentBase(), params);
            case DECR:
                return new RO_decr(getCurrentBase(), params);
            case DECRBY:
                return new RO_decrby(getCurrentBase(), params);
            case PFCOUNT:
                return new RO_pfcount(getCurrentBase(), params);
            case PFADD:
                return new RO_pfadd(getCurrentBase(), params);
            case PFMERGE:
                return new RO_pfmerge(getCurrentBase(), params);
            case MGET:
                return new RO_mget(getCurrentBase(), params);
            case MSET:
                return new RO_mset(getCurrentBase(), params);
            case GETSET:
                return new RO_getset(getCurrentBase(), params);
            case STRLEN:
                return new RO_strlen(getCurrentBase(), params);
            case DEL:
                return new RO_del(getCurrentBase(), params);
            case EXISTS:
                return new RO_exists(getCurrentBase(), params);
            case EXPIREAT:
                return new RO_expireat(getCurrentBase(), params);
            case PEXPIREAT:
                return new RO_pexpireat(getCurrentBase(), params);
            case LPUSH:
                return new RO_lpush(getCurrentBase(), params);
            case RPUSH:
                return new RO_rpush(getCurrentBase(), params);
            case LPUSHX:
                return new RO_lpushx(getCurrentBase(), params);
            case LRANGE:
                return new RO_lrange(getCurrentBase(), params);
            case LLEN:
                return new RO_llen(getCurrentBase(), params);
            case LPOP:
                return new RO_lpop(getCurrentBase(), params);
            case RPOP:
                return new RO_rpop(getCurrentBase(), params);
            case LINDEX:
                return new RO_lindex(getCurrentBase(), params);
            case RPOPLPUSH:
                return new RO_rpoplpush(getCurrentBase(), params);
            case BRPOPLPUSH:
                return new RO_brpoplpush(getCurrentBase(), params);
            case SUBSCRIBE:
                return new RO_subscribe(getCurrentBase(), owner, params);
            case UNSUBSCRIBE:
                return new RO_unsubscribe(getCurrentBase(), owner, params);
            case PUBLISH:
                return new RO_publish(getCurrentBase(), params);
            case FLUSHALL:
                return new RO_flushall(getCurrentBase(), params);
            case LREM:
                return new RO_lrem(getCurrentBase(), params);
            case QUIT:
                return new RO_quit(getCurrentBase(), owner, params);
            case EXEC:
                transactionModeOn = false;
                return new RO_exec(getCurrentBase(), transaction, params);
            case PING:
                return new RO_ping(getCurrentBase(), params);
            case KEYS:
                return new RO_keys(getCurrentBase(), params);
            case SADD:
                return new RO_sadd(getCurrentBase(), params);
            case SMEMBERS:
                return new RO_smembers(getCurrentBase(), params);
            case SPOP:
                return new RO_spop(getCurrentBase(), params);
            case HGET:
                return new RO_hget(getCurrentBase(), params);
            case HSET:
                return new RO_hset(getCurrentBase(), params);
            case HDEL:
                return new RO_hdel(getCurrentBase(), params);
            case HGETALL:
                return new RO_hegetall(getCurrentBase(), params);
            case SINTER:
                return new RO_sinter(getCurrentBase(), params);
            case HMGET:
                return new RO_hmget(getCurrentBase(), params);
            case HMSET:
                return new RO_hmset(getCurrentBase(), params);
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

            if(name.equals(RedisOperations.SELECT.name())){
                changeActiveRedisBase(commandParams);
                return Response.clientResponse(name, Response.OK);
            }

            //Checking if we mutating the transaction or the redisBases
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

    private void changeActiveRedisBase(List<Slice> commandParams) {
        String data = new String(commandParams.get(0).data());
        selectedRedisBase = Integer.parseInt(data);
    }

    private void newTransaction(){
        if(transactionModeOn) throw new RuntimeException("Redis mock does not support more than one transaction");
        transactionModeOn = true;
    }
}
