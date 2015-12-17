package com.sdd.jborg.scripts.java;

import com.sdd.jborg.util.JsonObject;

import java.nio.file.Paths;

import static com.sdd.jborg.scripts.Standard.*;

public class Ant
	implements Script
{
	static void setAttributes()
	{
		server.attributes.put("java", new JsonObject()
			.put("ant", new JsonObject()
				.put("install_dir", "/opt/apache-ant-1.9.5")
				.put("download", new JsonObject()
					.put("url", "http://apache.arvixe.com//ant/binaries/apache-ant-1.9.5-bin.tar.gz")
					.put("checksum", "e272e057a3c32b3536ffc050a6abce20aaa08b2618f79868e3e4c9c58628aeef")
					.put("extracts_to", "apache-ant-1.9.5"))));
	}

	@Override
	public void included()
	{
		setAttributes();

		then(execute("which ant").setTest((code, out, err) -> {
			if (code == 0) return;
			final JsonObject dl = server.attributes.getObject("java").getObject("ant").getObject("download");
			final String file = Paths.get(dl.getString("url")).getFileName().toString();

			Async.subFlow((flow) -> {
				flow.then(download(dl.getString("url"), "/tmp/"+ file)
					.setChecksum(dl.getString("checksum"))
					.setSudo(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0644"));

				flow.then(execute("cd /tmp; tar zxvf "+ file));
				flow.then(execute("rm -rf "+ server.attributes.getObject("java").getObject("ant").getString("install_dir")).setSudo(true));
				flow.then(execute("mv /tmp/"+ dl.getString("extracts_to") +"/ "+ server.attributes.getObject("java").getObject("ant").getString("install_dir")).setSudo(true));

				flow.then(appendLineToFileUnlessMatch("/etc/environment")
					.setSudo(true)
					.setMatch("ANT_HOME")
					.setAppend("ANT_HOME="+ server.attributes.getObject("java").getObject("ant").getString("install_dir")));

				flow.then(link(server.attributes.getObject("java").getObject("ant").getString("install_dir") + "/bin/ant", "/usr/local/bin/ant").setSudo(true));

				flow.then(execute("ant -version").setExpectCode(0));
			}).callImmediate();
		}));
	}
}
