package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Slice;

/**
 * Represents a Redis Operation which can be executed against {@link RedisBase}
 */
public interface RedisOperation {
    Slice execute();
}
