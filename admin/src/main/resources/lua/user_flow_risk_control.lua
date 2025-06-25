-- 设置用户访问频率限制的参数
local username = KEYS[1]
--In Redis Lua scripts, KEYS is an array of keys that the script will operate on.
--This line initializes a local variable username with the value passed as the first key to the Lua script (represented by KEYS[1]).
--句子直接翻译的中文意思难以通顺，改成 with the value as the first key passed to the Lua script，翻译就是将本地变量初始化为传入lua脚本的第一个键

local timeWindow = tonumber(ARGV[1]) -- 时间窗口，单位：秒
--ARGV is a special global array that holds the additional arguments you pass to your Lua script
--This line initializes a local variable timeWindow with the value passed as the first argument to the Lua script (represented by ARGV[1]).
--The tonumber() function ensures that the argument, which is a string by default, is converted into a number.

-- 构造 Redis 中存储用户访问次数的键名
local accessKey = "short-link:user-flow-risk-control:" .. username

-- 原子递增访问次数，并获取递增后的值
local currentAccessCount = redis.call("INCR", accessKey)
--INCR is a Redis command, it means the script is telling the Redis server 将用户访问次数存入该键，and increment the integer value of a key by one
--If the accessKey does not exist, it's created and set to 0 before being incremented to 1.
--If the accessKey exists and contains a non-numeric value, an error will occur.

-- 设置键的过期时间
redis.call("EXPIRE", accessKey, timeWindow)
--同上

-- 返回当前访问次数
return currentAccessCount