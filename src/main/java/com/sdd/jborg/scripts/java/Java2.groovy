package com.sdd.jborg.scripts.java

import java.nio.file.Paths;

import static com.sdd.jborg.scripts.Standard.*;

public class Java2
	implements BorgScript
{
	public static def Attributes = [
		jdk_version  : "6u33",
		jvm_dir      : "/usr/lib/jvm",
		jdk_downloads: [
			"6u33": [
				"x86_64": [
					"url"        : "PROVIDE-URL-YOURSELF",
					"checksum"   : "PROVIDE-SHA256SUM-YOURSELF",
					"extracts_to": "jdk1.6.0_33"
				]
			],
			"7u60": [
				"x86_64": [
					"url"        : "PROVIDE-URL-YOURSELF",
					"checksum"   : "PROVIDE-SHA256SUM-YOURSELF",
					"extracts_to": "jdk1.7.0_60",
				]
			]
		]
	];

	@Override
	public void included()
	{
		// validate version installed matches intended version
		then(execute("java -version 2>&1").setTest({ code, out, err ->
			final def dl = Attributes.jdk_downloads[Attributes.jdk_version].x86_64;
			if (code == 0 &&
				out.contains(dl.extracts_to.substring(3)))
			{
				return;
			}

			final String file = Paths.get(dl.url).getFileName().toString();

			Async.subFlow({ flow ->
				flow.then(download(dl.url, "/tmp/" + file)
					.setChecksum(dl.checksum)
					.setOwner("root")
					.setGroup("root")
					.setMode("0644")
					.setSudo(true));

				flow.then(execute("cd /tmp; tar zxvf " + file));
				flow.then(directory(Attributes.jvm_dir)
					.setRecursive(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0755")
					.setSudo(true));
				flow.then(execute("rm -rf ${Attributes.jvm_dir}/${Attributes.jdk_version}").setSudo(true));
				flow.then(execute("mv /tmp/"+ dl.extracts_to +"/ "+ Attributes.jvm_dir + "/" + Attributes.jdk_version).setSudo(true));
				flow.then(directory(Attributes.jvm_dir + "/" + Attributes.jdk_version)
					.setRecursive(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0755")
					.setSudo(true));

				flow.then(execute("update-alternatives --remove-all java").setSudo(true).setIgnoreErrors(true));
				flow.then(execute("update-alternatives --install /usr/bin/java java '${Attributes.jvm_dir}/${Attributes.jdk_version}/bin/java' 1").setSudo(true));
				flow.then(execute("update-alternatives --remove-all javac").setSudo(true).setIgnoreErrors(true));
				flow.then(execute("update-alternatives --install /usr/bin/javac javac '${Attributes.jvm_dir}/${Attributes.jdk_version}/bin/javac' 1").setSudo(true));

				// TODO: could list more jdk binaries
			}).callImmediate();
		}));
	}
}
