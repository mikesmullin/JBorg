package com.sdd.jborg.scripts.java;

import java.nio.file.Paths;

import static com.sdd.jborg.scripts.Standard.*;
import static com.sdd.jborg.scripts.java.Ant.Attributes.*;

public class Ant
	implements BorgScript
{
	public static class Attributes
	{
		public static String installDir = "/opt/apache-ant-1.9.5";
		public static Download download = new Download()
			.setUrl("http://apache.arvixe.com//ant/binaries/apache-ant-1.9.5-bin.tar.gz")
			.setChecksum("e272e057a3c32b3536ffc050a6abce20aaa08b2618f79868e3e4c9c58628aeef")
			.setExtractsTo("apache-ant-1.9.5");
	}

	@Override
	public void included()
	{
		then(execute("which ant").setTest((code, out, err) -> {
			if (code == 0) return;
			final String file = Paths.get(download.getUrl()).getFileName().toString();

			Async.subFlow((flow) -> {
				flow.then(download(download.getUrl(), "/tmp/"+ file)
					.setChecksum(download.getChecksum())
					.setSudo(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0644"));

				flow.then(execute("cd /tmp; tar zxvf "+ file));
				flow.then(execute("rm -rf "+ installDir).setSudo(true));
				flow.then(execute("mv /tmp/"+ download.getExtractsTo() +"/ "+ installDir).setSudo(true));

				flow.then(appendLineToFileUnlessMatch("/etc/environment")
					.setSudo(true)
					.setMatch("ANT_HOME")
					.setAppend("ANT_HOME="+ installDir));

				flow.then(link(installDir + "/bin/ant", "/usr/local/bin/ant").setSudo(true));

				flow.then(execute("ant -version").setExpectCode(0));
			}).callImmediate();
		}));
	}
}
