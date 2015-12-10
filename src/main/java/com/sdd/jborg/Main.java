package com.sdd.jborg;

import com.sdd.jborg.util.Logger;
import com.sdd.jborg.util.Ssh;

import static com.sdd.jborg.scripts.Standard.*;

public class Main
{
	public static void main(String[] args)
	{
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
		Logger.err("Aborting." + (reason == null ? "" : " Reason: " + reason));
		try
		{
			// necessary or messages will print out-of-order.
			// not totally sure why but must be multiple threads somewhere
			Thread.sleep(250);
		}
		catch (final InterruptedException ignored)
		{
		}
		finally
		{
			if (detail != null)
			{
				detail.printStackTrace();
			}
			System.exit(1);
		}
	}
}
