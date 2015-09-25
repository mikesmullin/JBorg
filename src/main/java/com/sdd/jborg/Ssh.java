package com.sdd.jborg;

import com.sdd.jborg.util.Callback1;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Ssh
{
	private SSHClient ssh;

	public Ssh connect(final String host, final int port, final String user, final String key)
	{
		try
		{
			ssh = new SSHClient();
			// just accept any remote host fingerprint;
			// they will almost always be new to us here
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			//ssh.loadKnownHosts();
			Logger.info("Connecting to remote host "+ user + "@" + host + ":" + port + " with key " + key + "...");
			ssh.connect(host, port);
			try
			{
				ssh.authPublickey(user, System.getProperty("user.home") + File.separator + ".ssh" + File.separator + key);
			}
			catch (UserAuthException | TransportException e)
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
		return this;
	}

	public interface CmdCallback
	{
		void call(final int code, final String out, final String err);
	}

	private static class PipeStream
	{
		private final InputStreamReader isr;
		private final BufferedReader br;
		private final StringBuffer everything;
		private final Callback1<String> dst;
		private boolean closed = false;

		public PipeStream(final InputStream src,
			final Callback1<String> dst)
		{
			isr = new InputStreamReader(src);
			br = new BufferedReader(isr);
			everything = new StringBuffer();
			this.dst = dst;
		}

		public boolean isEOF() {
			if (closed)
			{
				return true;
			}
			String line;
			try
			{
				if ((line = br.readLine()) != null) { // blocks until a line is returned?
					if (line.length() > 0)
					{
						dst.call(line);
						everything.append(line);
					}
					return false;
				}
				else
				{
					try
					{
						isr.close();
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
					closed = true;
					return true;
				}
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			closed = true;
			return true; // its the end because an error occurred
		}

		public String getEverything() {
			return everything.toString();
		}
	}

	public void cmd(final String command, final CmdCallback cb)
	{
		Session session = null;
		try
		{
			session = ssh.startSession();
			final Session.Command cmd = session.exec(command);
			Logger.stdin(command);
			final PipeStream out = new PipeStream(cmd.getInputStream(), Logger::stdout);
			final PipeStream err = new PipeStream(cmd.getErrorStream(), Logger::stderr);
			while(!(out.isEOF() && err.isEOF()))
			{
				// waiting...
				try
				{
					Thread.sleep(0); // yield to any other threads
				}
				catch (final InterruptedException ignored)
				{
				}
			}
			cmd.join(); // wait indefinitely for remote process to exit
			final int code = cmd.getExitStatus();
			Logger.info("remote process exit code: " + code);
			cb.call(code, out.getEverything(), err.getEverything());
		}
		catch (final IOException e)
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
			catch (final TransportException | ConnectionException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void put(final Path src, final String dst, final Callback1<IOException> errCallback)
	{
		SFTPClient sftp = null;
		try
		{
			sftp = ssh.newSFTPClient();
			sftp.put(src.toString(), dst);
		}
		catch (IOException e)
		{
			errCallback.call(e);
		}
		finally
		{
			try
			{
				if (sftp != null)
				{
					sftp.close();
				}
			}
			catch (IOException e)
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
