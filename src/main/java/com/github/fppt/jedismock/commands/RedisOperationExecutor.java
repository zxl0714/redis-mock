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
        switch(name){
            case "set":
                return new RO_set(getCurrentBase(), params);
            case "setex":
                return new RO_setex(getCurrentBase(), params);
            case "psetex":
                return new RO_psetex(getCurrentBase(), params);
            case "setnx":
                return new RO_setnx(getCurrentBase(), params);
            case "setbit":
                return new RO_setbit(getCurrentBase(), params);
            case "append":
                return new RO_append(getCurrentBase(), params);
            case "get":
                return new RO_get(getCurrentBase(), params);
            case "getbit":
                return new RO_getbit(getCurrentBase(), params);
            case "ttl":
                return new RO_ttl(getCurrentBase(), params);
            case "pttl":
                return new RO_pttl(getCurrentBase(), params);
            case "expire":
                return new RO_expire(getCurrentBase(), params);
            case "pexpire":
                return new RO_pexpire(getCurrentBase(), params);
            case "incr":
                return new RO_incr(getCurrentBase(), params);
            case "incrby":
                return new RO_incrby(getCurrentBase(), params);
            case "decr":
                return new RO_decr(getCurrentBase(), params);
            case "decrby":
                return new RO_decrby(getCurrentBase(), params);
            case "pfcount":
                return new RO_pfcount(getCurrentBase(), params);
            case "pfadd":
                return new RO_pfadd(getCurrentBase(), params);
            case "pfmerge":
                return new RO_pfmerge(getCurrentBase(), params);
            case "mget":
                return new RO_mget(getCurrentBase(), params);
            case "mset":
                return new RO_mset(getCurrentBase(), params);
            case "getset":
                return new RO_getset(getCurrentBase(), params);
            case "strlen":
                return new RO_strlen(getCurrentBase(), params);
            case "del":
                return new RO_del(getCurrentBase(), params);
            case "exists":
                return new RO_exists(getCurrentBase(), params);
            case "expireat":
                return new RO_expireat(getCurrentBase(), params);
            case "pexpireat":
                return new RO_pexpireat(getCurrentBase(), params);
            case "lpush":
                return new RO_lpush(getCurrentBase(), params);
            case "rpush":
                return new RO_rpush(getCurrentBase(), params);
            case "lpushx":
                return new RO_lpushx(getCurrentBase(), params);
            case "lrange":
                return new RO_lrange(getCurrentBase(), params);
            case "llen":
                return new RO_llen(getCurrentBase(), params);
            case "lpop":
                return new RO_lpop(getCurrentBase(), params);
            case "rpop":
                return new RO_rpop(getCurrentBase(), params);
            case "lindex":
                return new RO_lindex(getCurrentBase(), params);
            case "rpoplpush":
                return new RO_rpoplpush(getCurrentBase(), params);
            case "brpoplpush":
                return new RO_brpoplpush(getCurrentBase(), params);
            case "subscribe":
                return new RO_subscribe(getCurrentBase(), owner, params);
            case "unsubscribe":
                return new RO_unsubscribe(getCurrentBase(), owner, params);
            case "publish":
                return new RO_publish(getCurrentBase(), params);
            case "flushall":
                return new RO_flushall(getCurrentBase(), params);
            case "lrem":
                return new RO_lrem(getCurrentBase(), params);
            case "quit":
                return new RO_quit(getCurrentBase(), owner, params);
            case "exec":
                transactionModeOn = false;
                return new RO_exec(getCurrentBase(), transaction, params);
            case "ping":
                return new RO_ping(getCurrentBase(), params);
            case "keys":
                return new RO_keys(getCurrentBase(), params);
            case "sadd":
                return new RO_sadd(getCurrentBase(), params);
            case "smembers":
                return new RO_smembers(getCurrentBase(), params);
            case "spop":
                return new RO_spop(getCurrentBase(), params);
            case "hget":
                return new RO_hget(getCurrentBase(), params);
            case "hset":
                return new RO_hset(getCurrentBase(), params);
            case "hdel":
                return new RO_hdel(getCurrentBase(), params);
            case "hgetall":
                return new RO_hegetall(getCurrentBase(), params);
            case "sinter":
                return new RO_sinter(getCurrentBase(), params);
            case "hmget":
                return new RO_hmget(getCurrentBase(), params);
            case "hmset":
                return new RO_hmset(getCurrentBase(), params);
            default:
                throw new UnsupportedOperationException(String.format("Unsupported operation '%s'", name));
        }
    }

    public synchronized Slice execCommand(RedisCommand command) {
        Preconditions.checkArgument(command.parameters().size() > 0);
        List<Slice> params = command.parameters();
        List<Slice> commandParams = params.subList(1, params.size());
        String name = new String(params.get(0).data()).toLowerCase();

        try {
            //Transaction handling
            if(name.equals("multi")){
                newTransaction();
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

    private void newTransaction(){
        if(transactionModeOn) throw new RuntimeException("Redis mock does not support more than one transaction");
        transactionModeOn = true;
    }
}
