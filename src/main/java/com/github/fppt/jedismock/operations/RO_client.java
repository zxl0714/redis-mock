package com.github.fppt.jedismock.operations;

import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.server.Slice;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.List;

class RO_client extends AbstractRedisOperation {
  RO_client(RedisBase base, List<Slice> params) {
      super(base, params);
  }

  Slice response() {
      Slice key = params().get(1);
      return Response.bulkString(key);
  }
}
