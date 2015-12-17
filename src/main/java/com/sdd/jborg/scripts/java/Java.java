package com.sdd.jborg.scripts.java;

import com.sdd.jborg.util.JsonObject;

import java.nio.file.Paths;

import static com.sdd.jborg.scripts.Standard.*;

public class Java
	implements Script
{

	static void setAttributes()
	{
		server.attributes.put("java", new JsonObject()
			.put("jdk_version", "6u33")
			.put("jvm_dir", "/usr/lib/jvm")
			.put("jdk_downloads", new JsonObject()
				.put("6u33", new JsonObject()
					.put("x86_64", new JsonObject()
						.put("url", "PROVIDE-URL-YOURSELF")
						.put("checksum", "PROVIDE-SHA256SUM-YOURSELF")
						.put("extracts_to", "jdk1.6.0_33")))
				.put("7u60", new JsonObject()
					.put("x86_64", new JsonObject()
						.put("url", "PROVIDE-URL-YOURSELF")
						.put("checksum", "PROVIDE-SHA256SUM-YOURSELF")
						.put("extracts_to", "jdk1.7.0_60")))));
	}

	@Override
	public void included()
	{
		setAttributes();

		// validate version installed matches intended version
		then(execute("java -version 2>&1").setTest((code, out, err) -> {
			if (code == 0 &&
				out.contains(server.attributes.getObject("java").getObject("jdk_downloads").getObject(
					server.attributes.getObject("java").getString("jdk_version"))
					.getObject("x86_64").getString("extracts_to").substring(3)))
			{
				return;
			}

			final JsonObject dl = server.attributes.getObject("java").getObject("jdk_downloads").getObject(
				server.attributes.getObject("java").getString("jdk_version"))
				.getObject("x86_64");
			final String file = Paths.get(dl.getString("url")).getFileName().toString();

			Async.subFlow((flow) -> {
				flow.then(download(dl.getString("url"), "/tmp/" + file)
					.setChecksum(dl.getString("checksum"))
					.setOwner("root")
					.setGroup("root")
					.setMode("0644")
					.setSudo(true));

				flow.then(execute("cd /tmp; tar zxvf " + file));
				flow.then(directory(server.attributes.getObject("java").getString("jvm_dir"))
					.setRecursive(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0755")
					.setSudo(true));
				flow.then(execute("rm -rf " + server.java.jvm_dir + "/" + server.java.jdk_version).setSudo(true));
				flow.then(execute("mv /tmp/" + dl.extracts_to + "/ " + server.java.jvm_dir + "/" + server.java.jdk_version).setSudo(true));
				flow.then(chown(server.java.jvm_dir + "/" + server.java.jdk_version)
					.setRecursive(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0755")
					.setSudo(true));

				flow.then(execute("update-alternatives --remove-all java").setSudo(true).setIgnoreErrors(true));
				flow.then(execute("update-alternatives --install /usr/bin/java java '" + server.java.jvm_dir + "/" + server.java.jdk_version + "/bin/java' 1").setSudo(true));
				flow.then(execute("update-alternatives --remove-all javac").setSudo(true).setIgnoreErrors(true));
				flow.then(execute("update-alternatives --install /usr/bin/javac javac '" + server.java.jvm_dir + "/" + server.java.jdk_version + "/bin/javac' 1").setSudo(true));

				// TODO: could list more jdk binaries
			}).callImmediate();
		}));
	}
}
