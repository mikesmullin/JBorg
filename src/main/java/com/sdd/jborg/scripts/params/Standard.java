package com.sdd.jborg.scripts.params;

import com.sdd.jborg.Ssh;
import com.sdd.jborg.util.Callback0;
import com.sdd.jborg.scripts.Standard.RemoteServerValidationException;

public class Standard
{
	public static class Params
	{
		public Callback0 callback;

		public void callImmediate()
		{
			callback.call();
		}
	}

	private static class Sudoable
	{
		private String sudo = "";

		public String getSudoCmd()
		{
			return sudo;
		}

		public void setSudoCmd(final String cmd)
		{
			this.sudo = cmd;
		}

		public void setSudoAsUser(final String sudoer)
		{
			this.sudo = "sudo -i -u " + sudoer + " ";
		}

		public void setSudo(final boolean sudo)
		{
			this.sudo = sudo ? "sudo -i " : "";
		}
	}

	public interface ScriptRemoteTestCallback1
	{
		void call(final int code, final String out, final String err)
			throws RemoteServerValidationException;
	}

	public static final class ExecuteParams extends Params
	{
		private Ssh.CmdCallback testCb;
		private Sudoable sudoable = new Sudoable();

		public ExecuteParams setTest(final ScriptRemoteTestCallback1 testCb)
		{
			this.testCb = (code, out, err) -> {
				try {
					testCb.call(code, out, err);
				}
				catch (final RemoteServerValidationException e)
				{
					com.sdd.jborg.scripts.Standard.notifySkip(e);
				}
			};
			return this;
		}

		public Ssh.CmdCallback getTest()
		{
			return testCb;
		}

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public ExecuteParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public ExecuteParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public ExecuteParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}
	}

	private static class Ownable
	{
		private String owner = "";
		private String group = "";

		public String getOwner()
		{
			return owner;
		}

		public void setOwner(final String owner)
		{
			this.owner = owner;
		}

		public String getGroup()
		{
			return group;
		}

		public void setGroup(final String group)
		{
			this.group = group;
		}
	}

	private static class Recursable
	{
		private boolean recursive = false;

		public boolean getRecursive()
		{
			return recursive;
		}

		public void setRecursive(final boolean recursive)
		{
			this.recursive = recursive;
		}
	}

	private static class Modeable
	{
		private String mode = "";

		public String getMode()
		{
			return mode;
		}

		public void setMode(String mode)
		{
			this.mode = mode;
		}
	}

	public static final class ChownParams extends Params
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public ChownParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public ChownParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public ChownParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public ChownParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public ChownParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public ChownParams setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
			return this;
		}
	}

	public static final class ChmodParams extends Params
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public ChmodParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public ChmodParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public ChmodParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public ChmodParams setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public ChmodParams setMode(String mode)
		{
			modeable.setMode(mode);
			return this;
		}
	}

	public static final class DirectoryParams extends Params
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public DirectoryParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public DirectoryParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public DirectoryParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public DirectoryParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public DirectoryParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public DirectoryParams setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public DirectoryParams setMode(String mode)
		{
			modeable.setMode(mode);
			return this;
		}
	}

	public static final class UserParams extends Params
	{
		private String comment;
		private String password;
		private String[] sshKeys;
		private String groupName;
		private String[] groups;
		private String shell;

		public String getComment()
		{
			return comment;
		}

		public UserParams setComment(final String comment)
		{
			this.comment = comment;
			return this;
		}

		public String getPassword()
		{
			return password;
		}

		public UserParams setPassword(final String password)
		{
			this.password = password;
			return this;
		}

		public String[] getSshKeys()
		{
			return sshKeys;
		}

		public UserParams setSshKeys(final String[] sshKeys)
		{
			this.sshKeys = sshKeys;
			return this;
		}

		public String getGroupName()
		{
			return groupName;
		}

		public UserParams setGroupName(final String groupName)
		{
			this.groupName = groupName;
			return this;
		}

		public String[] getGroups()
		{
			return groups;
		}

		public UserParams setGroups(final String[] groups)
		{
			this.groups = groups;
			return this;
		}

		public String getShell()
		{
			return shell;
		}

		public UserParams setShell(final String shell)
		{
			this.shell = shell;
			return this;
		}

		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public UserParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public UserParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public UserParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}
	}
}