package ai.grakn.redismock;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class RedisBase {
    private final Map<Slice, Set<RedisClient>> subscribers = new ConcurrentHashMap<>();
    private final Map<Slice, Slice> base = new ConcurrentHashMap<>();
    private final Map<Slice, Long> deadlines = new ConcurrentHashMap<>();
    private final Set<RedisBase> syncBases = ConcurrentHashMap.newKeySet();

    public RedisBase() {}

    public synchronized void addSyncBase(RedisBase base) {
        syncBases.add(base);
    }

    public synchronized Slice rawGet(Slice key) {
        Preconditions.checkNotNull(key);

        Long deadline = deadlines.get(key);
        if (deadline != null && deadline != -1 && deadline <= System.currentTimeMillis()) {
            base.remove(key);
            deadlines.remove(key);
            return null;
        }
        return base.get(key);
    }

    public synchronized Long getTTL(Slice key) {
        Preconditions.checkNotNull(key);

        Long deadline = deadlines.get(key);
        if (deadline == null) {
            return null;
        }
        if (deadline == -1) {
            return deadline;
        }
        long now = System.currentTimeMillis();
        if (now < deadline) {
            return deadline - now;
        }
        base.remove(key);
        deadlines.remove(key);
        return null;
    }

    public synchronized long setTTL(Slice key, long ttl) {
        Preconditions.checkNotNull(key);

        if (base.containsKey(key)) {
            deadlines.put(key, ttl + System.currentTimeMillis());
            for (RedisBase base : syncBases) {
                base.setTTL(key, ttl);
            }
            return 1L;
        }
        return 0L;
    }

    public synchronized long setDeadline(Slice key, long deadline) {
        Preconditions.checkNotNull(key);

        if (base.containsKey(key)) {
            deadlines.put(key, deadline);
            for (RedisBase base : syncBases) {
                base.setDeadline(key, deadline);
            }
            return 1L;
        }
        return 0L;
    }

    public void clear(){
        base.clear();
        subscribers.clear();
        deadlines.clear();
        syncBases.clear();
    }

    public synchronized void rawPut(Slice key, Slice value, Long ttl) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);

        base.put(key, value);
        if (ttl != null) {
            if (ttl != -1) {
                deadlines.put(key, ttl + System.currentTimeMillis());
            } else {
                deadlines.put(key, -1L);
            }
        }
        for (RedisBase base : syncBases) {
            base.rawPut(key, value, ttl);
        }
    }

    public synchronized void del(Slice key) {
        Preconditions.checkNotNull(key);

        base.remove(key);
        deadlines.remove(key);

        for (RedisBase base : syncBases) {
            base.del(key);
        }
    }

    public synchronized void addSubscriber(Slice channel, RedisClient client){
        Set<RedisClient> newClient = new HashSet<>();
        newClient.add(client);
        subscribers.merge(channel, newClient, (currentSubscribers, newSubscribers) -> {
            currentSubscribers.addAll(newSubscribers);
            return currentSubscribers;
        });
    }

    public synchronized boolean removeSubscriber(Slice channel, RedisClient client){
        if(subscribers.containsKey(channel)){
            subscribers.get(channel).remove(client);
            return true;
        }
        return false;
    }

    public Set<RedisClient> getSubscribers(Slice channel){
        if (subscribers.containsKey(channel)) {
            return subscribers.get(channel);
        }
        return Collections.emptySet();
    }

    public List<Slice> getSubscriptions(RedisClient client){
        List<Slice> subscriptions = new ArrayList<>();

        subscribers.forEach((channel, subscribers) -> {
            if(subscribers.contains(client)){
                subscriptions.add(channel);
            }
        });

        return subscriptions;
    }
}
