package com.sdd.jborg.scripts;

import com.sdd.jborg.Main;
import com.sdd.jborg.util.Callback0;
import com.sdd.jborg.util.Container;
import com.sdd.jborg.util.Crypto;
import com.sdd.jborg.util.FileSystem;
import com.sdd.jborg.util.Func1;
import com.sdd.jborg.util.JsonObject;
import com.sdd.jborg.util.Logger;
import com.sdd.jborg.util.ModifiedStreamingTemplateEngine;
import com.sdd.jborg.util.Ssh;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sdd.jborg.params.*;
import com.sdd.jborg.util.ValidationHelper;
import org.reflections.Reflections;

import static com.sdd.jborg.util.Crypto.Algorithm.*;

/**
 * Standard objects and methods every script should have in scope by default.
 */
public class Standard
{
	// Script interface and include() helpers
	//------------------------------------------------------------------

	/**
	 * All implementations will automatically have matches() invoked
	 * once at startup in random order to self-determine whether
	 * they should be implicitly included().
	 *
	 * Otherwise, implementations must be explicitly included()
	 * from other scripts to have any effect or to be ordered.
	 */
	public interface Script
	{
		default boolean matches()
		{
			return false;
		}

		void included();
	}

	private static Set<Class<? extends Script>> scripts =
		new Reflections(System.getProperty("namespace")).getSubTypesOf(Script.class);

	public static boolean includeAllMatching()
	{
		boolean foundOne = false;
		for (final Class<? extends Script> script : scripts)
		{
			final Script instance;
			try
			{
				instance = script.newInstance();
				if (instance.matches())
				{
					foundOne = true;
					instance.included();
				}
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				die(e);
			}
		}
		return foundOne;
	}

	public static void includeIfPresent(final String className)
	{
		for (final Class<? extends Script> script : scripts)
		{
			if (className.equals(script.getSimpleName())) {
				final Script instance;
				try
				{
					instance = script.newInstance();
					instance.included();
					return;
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					die(e);
				}
			}
		}
	}

	public static void include(final Class<? extends Script> cls)
	{
		try
		{
			cls.newInstance().included();
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			die(e);
		}
	}

	// Global attributes and common interfaces
	//------------------------------------------------------------------

	public static final class Server
	{
		public final JsonObject attributes = new JsonObject();
		public final String timestamp = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss XX").format(new Date()));

		private static final Pattern FQDN_PATTERN = Pattern.compile(
			"^(test-)?([a-z]{2,3}-[a-z]{2,3})-([a-z]{1,5})-([a-z-]+)(\\d{2,4})(?:-([a-z]+))?(?:\\.(\\w+\\.[a-z]{2,3}))$",
			Pattern.CASE_INSENSITIVE);

		public class Fqdn
		{
			public String datacenter;
			public String env;
			public String type;
			public String instance;
			public String subproject;
			public String tld;

			public Fqdn(final String fqdn)
			{
				final Matcher m = FQDN_PATTERN.matcher(fqdn.toLowerCase());
				if (m.matches())
				{
					this.datacenter = m.group(2);
					this.env = m.group(3);
					this.type = m.group(4);
					this.instance = m.group(5);
					this.subproject = m.group(6);
					this.tld = m.group(7);
				}
			}

			@Override
			public String toString() {
				return getHostname() + "." + tld;
			}

			public String getHostname()
			{
				return datacenter + "-" +
					env + "-" +
					type +
					instance +
					(subproject != null ? "-" + subproject : "");
			}
		}
		public Fqdn fqdn;
		public Server setFqdn(final String fqdn)
		{
			this.fqdn = new Fqdn(fqdn);
			return this;
		}

		private Datacenter datacenter;

		public Server setDatacenter(final Datacenter datacenter)
		{
			this.datacenter = datacenter;
			return this;
		}

		public Datacenter getDatacenter()
		{
			return datacenter;
		}

		public class Ssh
		{
			public String host;
			public String user;
			public int port;
			public String key;
		}
		public Ssh ssh = new Ssh();

		private String privateIp;
		private String publicIp;

		public String getPrivateIp()
		{
			return privateIp;
		}

		public Server setPrivateIp(String privateIp)
		{
			this.privateIp = privateIp;
			return this;
		}

		public String getPublicIp()
		{
			return publicIp;
		}

		public Server setPublicIp(String publicIp)
		{
			this.publicIp = publicIp;
			return this;
		}
	}

	public static final Server server = new Server();
	public static Ssh ssh;
	public static String tz;
	public static boolean permitReboot = false;

	public interface CloudDriver
	{
		void createVirtualMachine();

		String getKeyName();
	}

	public interface Datacenter
	{
		CloudDriver getCloudDriver();

		String getTld();
	}

	// Procedural asynchronous flow control;
	// Use this instead of standard Java flow control operators (for, while, if, try, etc.)
	// to achieve a modular, parallelized, 2-pass approach to devops script runtime compilation.
	//------------------------------------------------------------------

	public static class Async
	{
		private Queue<Callback0> queue = new ArrayDeque<>();

		public Async() { }

		public static Params subFlow(final Standard.ScriptCallback1<Async> cb) {
			final Async async = new Async();
			return Standard.chainForCb(new Params(), p ->
				Standard.handleException(() -> {
					cb.call(async);
					async.go();
				}));
		}

		public void then(final Params params)
		{
			queue.add(params.callback);
		}

		public void end(final String reason) {
			// skip remainder of chain
			queue.clear();

			// optionally print reason as error
			if (reason != null)
			{
				Logger.err(reason);
			}
		}

		public void go()
		{
			while (queue.size() > 0)
			{
				queue.poll().call();
			}
		}
	}

	private static Async flow = new Async();

	public static void then(final Params params)
	{
		flow.then(params);
	}

	public static void go()
	{
		flow.go();
	}

	public static void now(final Params params) {
		params.callImmediate();
	}

	// Helpers
	//------------------------------------------------------------------

	public static String mapConcat(final String[] s, Func1<String, String> cb)
	{
		final StringBuilder sb = new StringBuilder();
		for (final String v : s)
		{
			sb.append(cb.call(v));
		}
		return sb.toString();
	}

	private static final Pattern BASH_PATTERN = Pattern.compile("([^0-9a-z-])", Pattern.CASE_INSENSITIVE);

	public static String bashEscape(final String cmd)
	{
		final Matcher matcher = BASH_PATTERN.matcher(cmd);
		return matcher.replaceAll("\\\\$1");
	}

	public static void die(final String reason)
	{
		Main.die(reason);
	}
	public static void die(final Throwable detail)
	{
		Main.die(detail);
	}
	public static void die(final String reason, final Throwable detail)
	{
		Main.die(reason, detail);
	}

	public static void notifySkip(final Exception reason)
	{
		Logger.err("Skipping. Reason: " + reason.getMessage());
		reason.printStackTrace();
	}

	public static void delay(final int ms, final String reason)
	{
		try
		{
			Logger.info("Waiting " + ms + "ms " + reason + "...");
			Thread.sleep(ms);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean empty(final String value)
	{
		return value == null || value.equals("");
	}

	public static boolean empty(final Object value)
	{
		return value == null;
	}

	public static boolean empty(final String[] value)
	{
		return value == null || value.length < 1;
	}

	public static void encryptLocalFile(final Path file)
	{
		// NOTICE: not streaming; file must fit in memory
		FileSystem.writeBytesToFile(file,
			Crypto.encrypt(
				FileSystem.readFileToBytes(file)));
	}

	public static String encrypt(final String s)
	{
		return Crypto.encrypt(s);
	}

	public static void decryptLocalFile(final Path target)
	{
		decryptLocalFile(target, target);
	}

	public static void decryptLocalFile(final Path source, final Path destination)
	{
		// NOTICE: not streaming; file must fit in memory
		FileSystem.writeBytesToFile(destination,
			Crypto.decrypt(
				FileSystem.readFileToBytes(source)));
	}

	public static String decrypt(final String s)
	{
		return Crypto.decrypt(s);
	}

	/**
	 * Will notify user and abort process.
	 */
	public static final class AbortException extends RuntimeException
	{
		public AbortException(String message)
		{
			super(message);
		}
	}

	/**
	 * Will step out, notify user, and continue.
	 */
	public static final class SkipException extends RuntimeException
	{
		public SkipException(String message)
		{
			super(message);
		}
	}

	public interface ScriptCallback0
	{
		void call()
			throws AbortException,
			SkipException;
	}

	public interface ScriptCallback1<T>
	{
		void call(final T t)
			throws AbortException,
			SkipException;
	}

	static void handleException(final ScriptCallback0 cb) {
		try
		{
			cb.call();
		}
		// skip current chain link
		catch (final SkipException e)
		{
			Standard.notifySkip(e);
		}
		// skip remainder of chain
		catch (final AbortException e)
		{
			die(e);
		}
	}

	// TODO: would be helpful to capture and remember stack traces where then() was invoked
	//       and display those with die() output to console.
	public static <T extends Params> T chainForCb(final T p, final ScriptCallback1<T> cb)
	{
		p.callback = () ->
			handleException(() ->
				cb.call(p));
		return p;
	}

	// Resource methods;
	// Use these instead of straight bash commands in order to achieve
	// more convenient and OS-agnostic devops scripting.
	//------------------------------------------------------------------

	public static Params log(final String msg)
	{
		return chainForCb(new Params(), p -> Logger.info(msg));
	}

	public static ExecuteParams execute(final String cmd)
	{
		return chainForCb(new ExecuteParams(), p -> {
			// TODO: could do this with simple while loop probably; would be less complex
			final AtomicInteger triesRemaining = new AtomicInteger(p.getRetryTimes());
			final Container<Callback0> _try = new Container<>();
			_try.set(() -> ssh.cmd(p.getPrefix() + p.getSudoCmd() + cmd, (code, out, err) -> {
				// TODO: implement regex and case-insensitive string search expectations
				String error = null;
				if (p.getExpectCode() == null && !p.isIgnoringErrors()) {
					p.setExpectCode(0);
				}
				// NOTICE: the order of these tests matters; they are depending on each other to avoid repeating operations
				if (code != 0 && p.isIgnoringErrors())
				{
					Logger.info("NOTICE: Non-zero exit code can be ignored. Will continue.");
				}
				else if (code != 0 && code == p.getExpectCode())
				{
					Logger.info("NOTICE: Non-zero exit code "+ p.getExpectCode() +" was expected. Will continue.");
				}
				else if (code != 0 && !empty(p.getTest()))
				{
					Logger.info("NOTICE: We are just testing. Will continue.");
				}
				else if (!p.isIgnoringErrors() && code != p.getExpectCode())
				{
					error = "Expected exit code " + p.getExpectCode() + ", but got " + code + ".";
				}

				if (error != null)
				{
					if (triesRemaining.decrementAndGet() > 0)
					{
						Logger.err(error + " Will try again...");
						_try.get().call(); // try again
					}
					else
					{
						die(new SkipException(error + " Tried " + (p.getRetryTimes() + 1) + " time(s). Giving up."));
					}
				}
				else
				{
					// NOTICE: here we wait until tries are over... DON'T invoke test on every try
					if (!empty(p.getTest()))
					{
						handleException(() ->
							p.getTest().call(code, out, err));
					}
				}
			}));

			_try.get().call();
		});
	}

	public static ServiceParams service(final String serviceName)
	{
		return chainForCb(new ServiceParams(), p -> {
			execute("service " + serviceName + " " + p.getAction())
				.setSudo(true)
				.callImmediate();
		});
	}

	public static ChownParams chown(final String path)
	{
		return chainForCb(new ChownParams(), p -> {
			if (empty(p.getOwner()) || empty(p.getGroup()))
				throw new AbortException("chown owner and group are required.");

			execute("chown " +
				(p.getRecursive() ? "-R " : "") +
				p.getOwner() +
				"." + p.getGroup() +
				" " + path)
				.setSudoCmd(p.getSudoCmd())
				.callImmediate();
		});
	}

	public static ChmodParams chmod(final String path)
	{
		return chainForCb(new ChmodParams(), p -> {
			if (empty(p.getMode()))
				throw new AbortException("mode is required.");

			execute("chmod " +
				p.getMode() +
				" " + path)
				.setSudoCmd(p.getSudoCmd())
				.callImmediate();
		});
	}

	private static String DIRECTORY_SEPARATOR = "/";

	public static DirectoryParams directory(final String... paths)
	{
		return chainForCb(new DirectoryParams(), p -> {
			if (empty(p.getMode()))
				p.setMode("0755");

			for (final String path : paths) {
				execute("test -d " + path)
					.setTest((code, out, err) -> {
						if (code == 0)
						{
							Logger.info("Skipping existing directory.");
						}
						else
						{
							execute("mkdir " +
								(p.getRecursive() ? " -p" : "") +
								" " + path)
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
						}
						//Check if user exists
						if (!empty(p.getOwner()) || !empty(p.getGroup()))
							chown(path)
								.setOwner(p.getOwner())
								.setGroup(p.getGroup())
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();

						if (!empty(p.getMode()))
							chmod(path)
								.setMode(p.getMode())
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
					})
					.callImmediate();
			}
		});
	}

	public static UserParams user(final String name)
	{
		return chainForCb(new UserParams(), p -> {
			execute("id " + name)
				.setTest((code, out, err) -> {
					if (code == 0)
						throw new SkipException("user " + name + " exists.");

					execute("useradd " + name + " \\\n" +
						"  --create-home \\\n" +
						"  --user-group \\\n" +
						(!empty(p.getComment()) ? "  --comment " + bashEscape(p.getComment()) + " \\\n" : "") +
						(!empty(p.getPassword()) ? "  --password " + bashEscape(p.getPassword()) + " \\\n" : "") +
						("  --shell " + (empty(p.getShell()) ? "/bin/bash" : "")))
						.setSudoCmd(p.getSudoCmd())
						.callImmediate();

					if (!empty(p.getGroupName()))
						execute("usermod -g " + p.getGroupName() + " " + name)
							.setSudoCmd(p.getSudoCmd())
							.callImmediate();

					if (!empty(p.getGroups()))
						for (final String group : p.getGroups())
							execute("usermod -a -G " + group + " " + name)
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();

					if (!empty(p.getSshKeys()))
						for (final String key : p.getSshKeys())
						{
							directory("$(echo ~" + name + ")/.ssh/")
								.setRecursive(true)
								.setMode("0700")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							execute("touch $(echo ~" + name + ")/.ssh/authorized_keys")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							chmod("$(echo ~" + name + ")/.ssh/authorized_keys")
								.setMode("0600")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							execute("echo " + bashEscape(key) + " | sudo tee -a $(echo ~" + name + ")/.ssh/authorized_keys >/dev/null")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							chown("$(echo ~" + name + ")/.ssh/")
								.setRecursive(true)
								.setOwner(name)
								.setGroup(name)
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
						}
				})
				.callImmediate();
		});
	}

	public static Params install(final String packages)
	{
		return chainForCb(new Params(), p -> {
			execute("dpkg -s " + packages + " 2>&1 | grep 'is not installed and'")
				.setTest((code, out, err) -> {
					if (code != 0)
					{
						Logger.info("Skipping package(s) already installed.");
						return;
					}

					execute("DEBIAN_FRONTEND=noninteractive apt-get install -y " + packages)
						.setSudo(true)
						.setRetry(3)
						.setExpectCode(0)
						.callImmediate();
				}).callImmediate();
		});
	}

	public static UninstallParams uninstall(final String packages)
	{
		return chainForCb(new UninstallParams(), p -> {
			execute("dpkg -s " + packages + " 2>&1 | grep 'install ok installed'")
				.setSudo(true)
				.setTest((code, out, err) -> {
					if (code != 0)
					{
						throw new SkipException(packages + " is not installed, ignoring");
					}
					else
					{
						execute("DEBIAN_FRONTEND=noninteractive apt-get " + ((p.isPurge() == true) ? "purge " : "uninstall ") + packages)
							.setSudo(true)
							.setRetry(3)
							.setExpectCode(0)
							.callImmediate();
					}
				}).callImmediate();
		});
	}

	public static Params packageUpdate()
	{
		return chainForCb(new Params(), p -> {
			execute("dpkg --configure -a")
				.setSudo(true)
				.setExpectCode(0)
				.callImmediate();

			execute("apt-get update")
				.setSudo(true)
				.setRetry(3)
				.setExpectCode(0)
				.callImmediate();

			execute("DEBIAN_FRONTEND=noninteractive apt-get dist-upgrade -y")
				.setSudo(true)
				.setRetry(3)
				.setExpectCode(0)
				.callImmediate();
		});
	}

	public static ReplaceLineInFileParams replaceLineInFile(String file, String find, String replace)
	{
		return chainForCb(new ReplaceLineInFileParams(), p ->
		{
			execute("grep " + bashEscape(find) + " " + file)
				.setTest((code, out, err) -> {
					if (code == 0)
					{
						Logger.info("Matching lines found, replacing...");
						final String tempFile = getHash(file);
						execute("sed " + "s/" + bashEscape(find) + ".*/" +
							(bashEscape(replace).replace("/", "\\/")) + "/ " +
							bashEscape(file) + " | " + p.getSudoCmd() +
							"tee /tmp/remote-" + tempFile + " > /dev/null 2>&1")
							.setTest(((code1, out1, err1) -> {
								if (code1 == 0)
								{
									execute("mv " + bashEscape("/tmp/remote-" + tempFile) + " " + bashEscape(file)).setTest((code2, out2, err2) -> {
										if (code2 != 0)
										{
											Logger.err("FATAL ERROR: Unable to replace line");
										}
									})
										.setSudoCmd(p.getSudoCmd())
										.callImmediate();
								}
							}))
							.setSudoCmd(p.getSudoCmd())
							.callImmediate();
					}
					else
					{
						die(new AbortException("Find string not found, nothing is being replaced."));
					}
				})
				.setSudoCmd(p.getSudoCmd())
				.callImmediate();
		});
	}

	public static LinkParams link(final String src, final String target)
	{
		return chainForCb(new LinkParams(), p -> {
			now(execute("test -L " + target)
				.setTest((code, out, err) -> {
					if (code != 1)
					{
						now(execute("rm " +target)
							.setSudoCmd(p.getSudoCmd()));
					}
				}));

			now(execute("ln -s " + src + " " + target)
				.setSudoCmd(p.getSudoCmd())
				.setExpectCode(0));
		});
	}

	private static final Pattern CHECKSUM_PATTERN_2 = Pattern.compile("([a-f0-9]{40})\\s.+");

	public static DeployParams deploy(final String appName)
	{
		return chainForCb(new DeployParams(), p -> {
			// TODO: support shared dir, cached-copy, and symlinking logs and other stuff
			// TODO: support keep_releases

			// force sudo as deploy owner
			p.setSudoAsUser(p.getOwner());

			// TODO: use unique key name to avoid accidentally overwriting different key
			final String privateKeyPath = "$(echo ~" + p.getOwner() + ")/.ssh/id_rsa";

			now(directory("$(echo ~" + p.getOwner() + ")/")
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setSudo(true)
				.setRecursive(true)
				.setMode("0700"));

			now(directory("$(echo ~" + p.getOwner() + ")/.ssh/")
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setSudo(true)
				.setRecursive(true)
				.setMode("0700"));

			// write ssh key to ~/.ssh/
			now(template(privateKeyPath)
				.setTemplateBody(p.getGit().getDeployKey())
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setMode("0600")
				.setSudo(true));

			// create the release dir
			// TODO: find a better alternative to next line
			now(execute("echo -e \"Host github.com\\n\\tStrictHostKeyChecking no\\n\" | sudo -u " + p.getSudoCmd() + " tee -a $(echo ~" + p.getOwner() + ")/.ssh/config"));
			// StringBuffers so we can modify it async from inside lambdas below
			final StringBuffer releaseDir = new StringBuffer();
			final StringBuffer remoteRef = new StringBuffer();
			now(execute("git ls-remote " + p.getGit().getRepo() + " " + p.getGit().getBranch())
				.setSudoCmd(p.getSudoCmd())
				.setTest((code, out, err) -> {
					final Matcher matcher = CHECKSUM_PATTERN_2.matcher(out);
					if (!matcher.matches())
					{
						die("github repo didn't have the branch we're expecting " + p.getGit().getBranch());
					}
					remoteRef.append(matcher.group(1));
					releaseDir.append(p.getDeployTo() +"/releases/"+ remoteRef.toString());
				}));
			now(directory(p.getDeployTo())
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setSudo(true)
				.setRecursive(true));
			now(directory(releaseDir.toString())
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setSudo(true)
				.setRecursive(true));
			now(execute("git clone -b " + p.getGit().getBranch() + " " + p.getGit().getRepo() + " " + releaseDir)
				.setSudoCmd(p.getSudoCmd())
				.setIgnoreErrors(true));
			now(link(releaseDir.toString(), p.getDeployTo() + "/current")
				.setSudoCmd(p.getSudoCmd()));
		});
	}

	private static String getHash(final String seed)
	{
		return Crypto.computeHash(SHA_1, seed) +
			Long.toHexString(Double.doubleToRawLongBits(Math.random())).toUpperCase().substring(8);
	}

	/**
	 * Compiles text output from strings or local files on disk when given a map of variables to fill in.
	 * <p>
	 * Template engine syntax and details:
	 * http://docs.groovy-lang.org/latest/html/documentation/template-engines.html#_streamingtemplateengine
	 */
	public static TemplateParams template(final String remoteTargetFile)
	{
		return chainForCb(new TemplateParams(), p -> {
			final Reader template;
			// template from string
			if (p.getTemplateBody() != null)
			{
				template = new StringReader(p.getTemplateBody());
			}
			// template from local disk
			else if (p.getLocalTemplateFile() != null)
			{
				template = FileSystem.getFileReader(p.getLocalTemplateFile());
			}
			else {
				throw new AbortException("One of .setLocalTemplateFile() or .setTemplateBody() is a required parameter!");
			}

			// include the global `server` variable in-scope for all templates automatically
			p.getVariables().put("server", server);

			// compile template variables
			final String output;
			try
			{
				output = new ModifiedStreamingTemplateEngine()
					.createTemplate(template)
					.make(p.getVariables())
					.toString();
			}
			catch (final Exception e)
			{
				die("Unable to compile template for remote file " + remoteTargetFile, e);
				return; // abort
			}

			// log for debugging purposes
			final String ver = getHash(output);
			Logger.info("rendering file " + remoteTargetFile + " version " + ver);
			Logger.out("---- BEGIN FILE ----\n" + output + "\n--- END FILE ---");

			// write string to temporary file on local disk
			final Path tmpFile = Paths.get(System.getProperty("java.io.tmpdir"), "local-" + ver);
			FileSystem.writeStringToFile(tmpFile, output);

			// upload file to remote disk
			upload(remoteTargetFile)
				.setRemoteTmpFile("/tmp/remote-" + ver)
				.setLocalSourceFile(tmpFile)
				.setSudoCmd(p.getSudoCmd())
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setMode(p.getMode())
				.callImmediate();

			// delete temporary file from local test
			FileSystem.unlink(tmpFile);
		});
	}

	/**
	 * Upload local file to remote host via SFTP.
	 */
	public static UploadParams upload(final String remoteTargetFile)
	{
		return chainForCb(new UploadParams(), p -> {
			if (p.getLocalSourceFile() == null)
				throw new AbortException(".setLocalSourceFile() is a required parameter!");

			final String ver = getHash(p.getLocalSourceFile().toString());
			final Path localTmp;

			if (p.isEncrypted())
			{
				Logger.info("Decrypting file "+ p.getLocalSourceFile().toString() +" to temporary location on local disk...");
				try
				{
					localTmp = File.createTempFile("local-"+ver, "").toPath();
					decryptLocalFile(p.getLocalSourceFile(), localTmp);
				}
				catch (final IOException e)
				{
					die(e);
					return;
				}
			}
			else
			{
				localTmp = p.getLocalSourceFile();
			}

			if (p.getRemoteTmpFile() == null)
			{
				p.setRemoteTmpFile("/tmp/remote-" + ver);
			}

			remoteFileExists(remoteTargetFile)
				.setSudoCmd(p.getSudoCmd())
				.setCompareLocalFile(localTmp)
				.setTrueCallback(() -> {
					Logger.info("Upload would be pointless since checksums match; skipping to save time.");

					// set ownership and permissions
					chown(remoteTargetFile)
						.setSudoCmd(p.getSudoCmd())
						.setOwner(p.getOwner())
						.setGroup(p.getGroup())
						.callImmediate();
					chmod(remoteTargetFile)
						.setSudoCmd(p.getSudoCmd())
						.setMode(p.getMode())
						.callImmediate();
				})
				.setFalseCallback(() -> {
					Logger.info("SFTP uploading " + localTmp.toFile().length() + " " +
						"bytes from \"" + p.getLocalSourceFile().toString() + "\" to \"" + remoteTargetFile + "\" " +
						(!remoteTargetFile.equals(p.getRemoteTmpFile()) ? " through temporary file \"" + p.getRemoteTmpFile() + "\"" : "") +
						"...");

					ssh.put(localTmp, p.getRemoteTmpFile(), e -> {
						die(new SkipException("error during SFTP file transfer: " + e.getMessage()));
					});

					Logger.info("SFTP upload complete.");

					if (p.isEncrypted())
					{
						// delete temporarily decrypted version of the file from local disk
						FileSystem.unlink(localTmp);
					}

					// set ownership and permissions
					chown(p.getRemoteTmpFile())
						.setSudoCmd(p.getSudoCmd())
						.setOwner(p.getOwner())
						.setGroup(p.getGroup())
						.callImmediate();
					chmod(p.getRemoteTmpFile())
						.setSudoCmd(p.getSudoCmd())
						.setMode(p.getMode())
						.callImmediate();

					// move into final location
					execute("mv " + p.getRemoteTmpFile() + " " + remoteTargetFile)
						.setSudoCmd(p.getSudoCmd())
						.setExpectCode(0)
						.callImmediate();
				})
				.callImmediate();
		});
	}

	/**
	 * download a file from the internet to the remote host with wget
	 */
	public static DownloadParams download(final String uri, final String destination)
	{
		return chainForCb(new DownloadParams(), p -> {
			//TODO: Once remote file exists function is verified, check if the file already exists before downloading
			//for now, just download
			execute("wget -nv " + uri + " " + bashPrefix("-O", destination))
				.setSudoCmd(p.getSudoCmd())
				.callImmediate();

			if (!empty(p.getOwner()) && !empty(p.getGroup()))
			{
				chown(destination)
					.setOwner(p.getOwner())
					.setGroup(p.getGroup())
					.setSudoCmd(p.getSudoCmd())
					.callImmediate();
			}

			if (!empty(p.getMode()))
			{
				chmod(destination)
					.setMode(p.getMode())
					.setSudoCmd(p.getSudoCmd())
					.callImmediate();
			}
		});
	}

	public static Params sysctl(final String variable, final String value)
	{
		return chainForCb(new Params(), params -> {
			//remove any existing values for this key in sysctl.conf
			execute("sed -i '/^" + variable + "/d' /etc/sysctl.conf")
				.setSudo(true)
				.callImmediate();
			//Apply changes to sysctl.conf so that it persists beyond a reboot
			execute("echo '" + variable + " = " + value + "' | sudo tee -a /etc/sysctl.conf > /dev/null")
				.callImmediate();
			//Reload sysctl
			execute("sysctl -q -p /etc/sysctl.conf")
				.setSudo(true)
				.callImmediate();
		});
	}

	public static Params reboot()
	{
		return chainForCb(new Params(), params -> {
			Logger.info("JBorg rebooting system now");
			execute("reboot")
				.setSudo(true)
				.callImmediate();
		});
	}

	public static Params setTimeZone()
	{
		return chainForCb(new Params(), params -> {
			Logger.info("Setting time zone");
			install("tzdata")
				.callImmediate();
			execute("echo " + tz + " | sudo tee /etc/timezone >/dev/null")
				.callImmediate();
			execute("dpkg-reconfigure -f noninteractive tzdata")
				.setSudo(true)
				.callImmediate();
		});
	}

	public static String bashPrefix(String flag, String value)
	{
		if (value != null)
			return " " + flag + " " + value + " ";
		else
			return "";
	}

	public static String bashPrefix(String flag)
	{
		return " " + flag + " ";
	}

	private static final Pattern CHECKSUM_PATTERN = Pattern.compile("^([a-f0-9]{64})  .+");

	/**
	 * Check whether a file exists on the remote server's disk.
	 *
	 * @param path path to file on remote server
	 * @return Composable parameters:
	 * CompareLocalFile - path to file on local machine to hash and compare to hash of remote file
	 * CompareChecksum - sha256 hash string to compare hash of remote file to
	 */
	public static RemoteFileExistsParams remoteFileExists(final String path)
	{
		return chainForCb(new RemoteFileExistsParams(), p -> {
			if (empty(p.getCompareLocalFile()) && empty(p.getCompareChecksum()))
			{
				execute("stat " + path)
					.setSudoCmd(p.getSudoCmd())
					.setTest((code, out, err) -> {
						if (code == 0)
						{
							Logger.info("Remote file " + path + " exists.");
							p.invokeTrueCallback();
						}
						else
						{
							Logger.info("Remote file " + path + " does not exist.");
							p.invokeFalseCallback();
						}
					})
					.callImmediate();
			}
			else
			{
				if (p.getCompareLocalFile() != null)
				{
					execute("sha256sum " + path)
						.setSudoCmd(p.getSudoCmd())
						.setTest((code, out, err) -> {
							final Matcher matcher = CHECKSUM_PATTERN.matcher(out);
							if (matcher.matches())
							{
								final String localChecksum = Crypto.computeHash(SHA_256,
									FileSystem.readFileToBytes(p.getCompareLocalFile())).toLowerCase();
								if (matcher.group(1).equals(localChecksum))
								{
									Logger.info("Remote file checksum matches checksum of local file " +
										p.getCompareLocalFile() + ".");
									p.invokeTrueCallback();
								}
								else
								{
									Logger.info("Remote file checksum does not match checksum of local file " +
										localChecksum + " " + p.getCompareLocalFile() + ".");
									p.invokeFalseCallback();
								}
							}
							else
							{
								Logger.info("Remote file " + path + " does not exist.");
								p.invokeFalseCallback();
							}
						})
						.callImmediate();
				}
				if (p.getCompareChecksum() != null)
				{
					execute("sha256sum " + path)
						.setSudoCmd(p.getSudoCmd())
						.setTest((code, out, err) -> {
							final Matcher matcher = CHECKSUM_PATTERN.matcher(out);
							if (matcher.matches())
							{
								if (matcher.group(0).equals(p.getCompareChecksum()))
								{
									Logger.info("Remote file checksum " + matcher.group(0) + " matches expected checksum " + p.getCompareChecksum() + ".");
									p.invokeTrueCallback();
								}
								else
								{
									Logger.info("Remote file checksum " + matcher.group(0) + " does not match expected checksum " + p.getCompareChecksum() + ".");
									p.invokeFalseCallback();
								}
							}
							else
							{
								Logger.info("Unexpected problems reading remote file checksum.  Assuming remote file checksum does not match expected checksum " + p.getCompareChecksum() + ".");
								p.invokeFalseCallback();
							}
						})
						.callImmediate();
				}
			}
		});
	}

	/**
	 * Appends line only if no matching line is found.
	 */
	public static AppendLineToFileUnlessMatchParams appendLineToFileUnlessMatch(final String file)
	{
		return chainForCb(new AppendLineToFileUnlessMatchParams(), p -> {
			if (empty(p.getMatch()) || empty(p.getAppend()))
				throw new AbortException(".setMatch() and .setAppend() are required.");

			now(execute("grep " + bashEscape(p.getMatch()) + " " + bashEscape(file))
				.setTest((code, out, err) -> {
					if (code == 0)
					{
						now(log("Matching line found, not appending"));
					}
					else
					{
						now(log("Matching line not found, appending..."));
						now(execute("echo " + bashEscape(p.getAppend()) + " | sudo tee -a " + bashEscape(file))
							.setTest((code2, out2, err2) -> {
								if (code2 != 0)
								{
									throw new AbortException("FATAL ERROR: unable to append line.");
								}
							}));
					}
				}));
		});
	}

}
