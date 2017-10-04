package ai.grakn.redismock;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Xiaolu on 2015/4/20.
 */
public class RedisCommand {

    private List<Slice> params = Lists.newArrayList();

    RedisCommand() {}

    void addParameter(Slice token) {
        this.params.add(token);
    }

    public List<Slice> getParameters() {
        return params;
    }

    @Override
    public String toString(){
        return params.stream().map(Slice::toString).collect(Collectors.joining(" "));
    }
}
