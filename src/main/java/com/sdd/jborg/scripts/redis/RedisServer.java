package com.sdd.jborg.scripts.redis;

import static com.sdd.jborg.scripts.Standard.*;

public class RedisServer
	implements Script
{
	@Override
	public void included()
	{
		// install redis server from source
		final String redisVersion = "redis-3.0.2";
		final String redisTarball = redisVersion + ".tar.gz";

		then(download("http://download.redis.io/releases/" + redisTarball,
			"/tmp/" + redisTarball)
			.setChecksum("93e422c0d584623601f89b956045be158889ebe594478a2c24e1bf218495633f")
			.setOwner("root")
			.setGroup("root")
			.setMode("0644")
			.setSudo(true)
		);

		then(execute("cd /tmp; tar zxvf " + redisTarball));
		then(execute("make install").setSudo(true)
			.setPrefix("cd /tmp/" + redisVersion + "; "));

		then(directory("/etc/redis")
			.setRecursive(true)
			.setOwner("root")
			.setGroup("root")
			.setMode("0755")
			.setSudo(true)
		);

		then(directory("/var/redis/6379")
			.setRecursive(true)
			.setOwner("root")
			.setGroup("root")
			.setMode("0755")
			.setSudo(true)
		);

		// install configuration
		final String redisConf = "/etc/redis/6379.conf";
		then(remoteFileExists(redisConf)
			.setSudo(true)
			.setFalseCallback(() -> {
				Async.subFlow((flow) -> {
					flow.then(execute("cp /tmp/" + redisVersion + "/redis.conf " + redisConf).setSudo(true));
					flow.then(replaceLineInFile(redisConf, "^# bind 127.0.0.1", "bind 0.0.0.0").setSudo(true));
					flow.then(replaceLineInFile(redisConf, "^daemonize", "daemonize yes").setSudo(true));
					flow.then(replaceLineInFile(redisConf, "^pidfile", "pidfile /var/run/redis_6379.pid").setSudo(true));
					flow.then(replaceLineInFile(redisConf, "^logfile", "logfile /var/log/redis_5379.log").setSudo(true));
					flow.then(replaceLineInFile(redisConf, "^dir", "dir /var/redis/6379").setSudo(true));
				}).callImmediate();
			})
		);

		// install init script
		final String redisInitdFile = "/etc/init.d/redis_6379";
		then(remoteFileExists(redisInitdFile)
				.setSudo(true)
				.setFalseCallback(() -> {
					execute("cp /tmp/" + redisVersion + "/utils/redis_init_script " + redisInitdFile).setSudo(true)
						.callImmediate();
				})
		);

		// auto-start on reboot
		then(execute("update-rc.d redis_6379 defaults").setSudo(true));

		// start service now
		then(execute("service redis_6379 start").setSudo(true));
	}
}

