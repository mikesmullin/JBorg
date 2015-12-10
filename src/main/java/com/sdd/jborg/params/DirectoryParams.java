package com.sdd.jborg.params;

public final class DirectoryParams extends Params
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

	public DirectoryParams setMode(final String mode)
	{
		modeable.setMode(mode);
		return this;
	}
}