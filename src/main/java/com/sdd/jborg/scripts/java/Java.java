package com.sdd.jborg.scripts.java;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.sdd.jborg.scripts.Standard.*;
import static com.sdd.jborg.scripts.java.Java.Attributes.*;

public class Java
	implements BorgScript
{
	public static class Attributes
	{
		public static Class<? extends JdkVersionOption> jdkVersion;
		public static String jvmDir = "/usr/lib/jvm";

		public static final class JdkDownloads
		{
			private static Map<Class<? extends JdkVersionOption>,
				Map<Class<? extends JdkArchitectureOption>, Download>> jdkDownloads =
				new HashMap<>();

			public static void put(
				final Class<? extends JdkVersionOption> ver,
				final Class<? extends JdkArchitectureOption> arch,
				final Download dl)
			{
				jdkDownloads.put(ver, new HashMap<Class<? extends JdkArchitectureOption>, Download>()
				{{
					put(arch, dl);
				}});
			}

			public static Download get(
				final Class<? extends JdkVersionOption> ver,
				final Class<? extends JdkArchitectureOption> arch)
			{
				return jdkDownloads.get(ver).get(arch);
			}
		}

		public static abstract class JdkVersionOption
		{
		}

		public static abstract class JdkArchitectureOption
		{
		}

		public static abstract class x86_64 extends JdkArchitectureOption
		{
		}
	}

	@Override
	public void included()
	{
		// validate version installed matches intended version
		then(execute("java -version 2>&1").setTest((code, out, err) -> {
			// NOTICE: we assume x86_64 architecture here, until differentiation is required.
			final Download dl = JdkDownloads.get(jdkVersion, x86_64.class);
			if (code == 0 &&
				out.contains(dl.getExtractsTo().substring(3)))
			{
				return;
			}

			final String file = Paths.get(dl.getUrl()).getFileName().toString();

			Async.subFlow((flow) -> {
				flow.then(download(dl.getUrl(), "/tmp/" + file)
					.setChecksum(dl.getChecksum())
					.setOwner("root")
					.setGroup("root")
					.setMode("0644")
					.setSudo(true));

				flow.then(execute("cd /tmp; tar zxvf " + file));
				flow.then(directory(jvmDir)
					.setRecursive(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0755")
					.setSudo(true));
				flow.then(execute("rm -rf " + jvmDir + "/" + jdkVersion).setSudo(true));
				flow.then(execute("mv /tmp/" + dl.getExtractsTo() + "/ " + jvmDir + "/" + jdkVersion).setSudo(true));
				flow.then(directory(jvmDir + "/" + jdkVersion)
					.setRecursive(true)
					.setOwner("root")
					.setGroup("root")
					.setMode("0755")
					.setSudo(true));

				flow.then(execute("update-alternatives --remove-all java").setSudo(true).setIgnoreErrors(true));
				flow.then(execute("update-alternatives --install /usr/bin/java java '" + jvmDir + "/" + jdkVersion + "/bin/java' 1").setSudo(true));
				flow.then(execute("update-alternatives --remove-all javac").setSudo(true).setIgnoreErrors(true));
				flow.then(execute("update-alternatives --install /usr/bin/javac javac '" + jvmDir + "/" + jdkVersion + "/bin/javac' 1").setSudo(true));

				// TODO: could list more jdk binaries
			}).callImmediate();
		}));
	}
}
