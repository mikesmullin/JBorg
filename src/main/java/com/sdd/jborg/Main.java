package com.sdd.jborg;

import com.sdd.jborg.util.FileSystem;
import com.sdd.jborg.util.Logger;
import com.sdd.jborg.util.Ssh;

import static com.sdd.jborg.scripts.Standard.*;

public class Main
{
	public static void main(String[] args)
	{
		// miscellaneous cli utilities
		switch (args[0].toLowerCase())
		{
			// encrypt com/wildworks/devops/ajc/files/ssl_certs.tar.gz
			case "encrypt":
				encryptLocalFile(FileSystem.getResourcePath(args[1]));
				done("Done.");
				break;

			// decrypt com/wildworks/devops/ajc/files/ssl_certs.tar.gz
			case "decrypt":
				decryptLocalFile(FileSystem.getResourcePath(args[1]));
				done("Done.");
				break;
		}


		// main purpose
		server.setFqdn(args[1]);

		// pass 1: compilation
		includeIfPresent("First");
		if (!includeAllMatching())
		{
			die(new RuntimeException("Unable to locate matching script."));
		}
		includeIfPresent("Last");

		// pass 2: execution
		switch (args[0].toLowerCase())
		{
			case "assemble":
				server.getDatacenter().getCloudDriver().createVirtualMachine();

				// flow through to assimilate

			case "assimilate":
				Logger.setHost(server.ssh.host);
				ssh = new Ssh().connect(
					server.ssh.host,
					server.ssh.port,
					server.ssh.user,
					server.ssh.key
				);

				go(); // loop 2
				ssh.close();
				Logger.info("Assimilation complete.");
				break;
		}
	}

	public static void done(final String message)
	{
		Logger.info(message);
		System.exit(0);
	}

	public static void die(final String reason)
	{
		die(reason, null);
	}

	public static void die(final Throwable reason)
	{
		die(null, reason);
	}

	public static void die(final String reason, final Throwable detail)
	{
		if (detail != null)
		{
			detail.printStackTrace();
		}
		try
		{
			// necessary or messages will print out-of-order.
			// not totally sure why but must be multiple threads somewhere
			Thread.sleep(250);
		}
		catch (final InterruptedException ignored)
		{
		}
		Logger.err("Aborting." + (reason == null ? "" : " Reason: " + reason));
		System.exit(1);
	}
}
