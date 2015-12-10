package com.sdd.jborg.params;

public final class UserParams extends Params
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