package com.sdd.jborg.scripts.params;

public final class ChownParams
	extends Params
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
