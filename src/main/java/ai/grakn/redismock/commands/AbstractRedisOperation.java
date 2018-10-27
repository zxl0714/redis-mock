package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Slice;

import java.util.List;

import static ai.grakn.redismock.Utils.checkArgumentsNumberEquals;
import static ai.grakn.redismock.Utils.checkArgumentsNumberFactor;
import static ai.grakn.redismock.Utils.checkArgumentsNumberGreater;

abstract class AbstractRedisOperation implements RedisOperation {
    private final RedisBase base;
    private final List<Slice> params;

    AbstractRedisOperation(RedisBase base, List<Slice> params, Integer expectedParams, Integer minParams, Integer factorParams) {
        this.base = base;
        this.params = params;
        precheck(expectedParams, minParams, factorParams);
    }

    void doOptionalWork(){
    }

    abstract Slice response();

    RedisBase base(){
        return base;
    }

    List<Slice> params(){
        return params;
    }

    @Override
    public Slice execute(){
        doOptionalWork();

        synchronized (base){
            return response();
        }
    }

    /**
     * Runs a default precheck to make sure the parameters are as expected
     */
    private void precheck(Integer expectedParams, Integer minParams, Integer factorParams){
        if(expectedParams != null) checkArgumentsNumberEquals(params, expectedParams);
        if(minParams != null) checkArgumentsNumberGreater(params, minParams);
        if(factorParams != null) checkArgumentsNumberFactor(params, factorParams);
    }
}
