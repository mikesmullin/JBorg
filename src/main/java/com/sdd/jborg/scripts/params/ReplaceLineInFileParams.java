package com.sdd.jborg.scripts.params;

public final class ReplaceLineInFileParams extends Params
{
	private Sudoable sudoable = new Sudoable();

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public ReplaceLineInFileParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public ReplaceLineInFileParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}
}