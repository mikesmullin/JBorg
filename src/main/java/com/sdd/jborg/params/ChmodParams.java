package com.sdd.jborg.params;

public final class ChmodParams
	extends Params
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

	public ChmodParams setMode(final String mode)
	{
		modeable.setMode(mode);
		return this;
	}
}