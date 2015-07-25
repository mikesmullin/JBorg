package com.sdd.jborg;

import com.sdd.jborg.util.Logger;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.File;
import java.io.IOException;

public class Ssh
{
	private SSHClient ssh;

	public void connect(final String host, final int port, final String user, final String key)
	{
		try
		{
			ssh = new SSHClient();
			// just accept any remote host fingerprint;
			// they will almost always be new to us here
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			//ssh.loadKnownHosts();
			ssh.connect(host, port);
			try
			{
				ssh.authPublickey(user, System.getProperty("user.home") + File.separator + ".ssh" + File.separator + key);
			}
			catch (UserAuthException e)
			{
				e.printStackTrace();
				close();
			}
			catch (TransportException e)
			{
				e.printStackTrace();
				close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			close();
		}
	}

	public void cmd(final String command)
	{
		Session session = null;
		try
		{
			session = ssh.startSession();
			final Session.Command cmd = session.exec(command);
			Logger.stdin(command);
			cmd.join(); // wait indefinitely for remote process to exit
			final String stdOut = IOUtils.readFully(cmd.getInputStream()).toString();
			if (stdOut.length() > 0)
				Logger.stdout(stdOut);
			final String stdErr = IOUtils.readFully(cmd.getErrorStream()).toString();
			if (stdErr.length() > 0)
				Logger.stderr(stdErr);
			final int code = cmd.getExitStatus();
			Logger.info("remote process exit code: " + code);
		}
		catch (ConnectionException e)
		{
			e.printStackTrace();
		}
		catch (TransportException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (session != null)
				{
					session.close();
				}
			}
			catch (TransportException e)
			{
				e.printStackTrace();
			}
			catch (ConnectionException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void close()
	{
		try
		{
			ssh.disconnect();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}