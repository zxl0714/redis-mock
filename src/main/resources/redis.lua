-- Global `redis` object available for Lua scripts

return {
  LOG_DEBUG = 0,
  LOG_VERBOSE = 1,
  LOG_NOTICE = 2,
  LOG_WARNING = 3,

  call = function(...)
    return _mock:call({...})
  end,

  pcall = function(...)
    return _mock:pcall({...})
  end,

  sha1hex = function(x)
    return _mock:sha1hex(x)
  end,

  status_reply = function(x)
    return { ok = x }
  end,

  error_reply = function(x)
      return { err = x }
  end,

  log = function(level, message)
      return _mock:log(level, message)
  end,
}
