package com.sdd.jborg.scripts.params;

public final class LinkParams extends Params
{
	private String target;

	public LinkParams setTarget(final String target)
	{
		this.target = target;
		return this;
	}

	public String getTarget()
	{
		return target;
	}

	private Sudoable sudoable = new Sudoable();

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public LinkParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public LinkParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public LinkParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}
}