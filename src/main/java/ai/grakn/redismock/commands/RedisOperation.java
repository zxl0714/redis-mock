package ai.grakn.redismock.commands;

import ai.grakn.redismock.Slice;

/**
 * Represents a Redis Operation which can be executed against {@link ai.grakn.redismock.RedisBase}
 */
public interface RedisOperation {
    Slice execute();
}
