package ai.grakn.redismock.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WorkerManager {
    private final static ExecutorService pool = Executors.newFixedThreadPool(20);

    public static Future runJob(Runnable function){
        return pool.submit(function::run);
    }
}
