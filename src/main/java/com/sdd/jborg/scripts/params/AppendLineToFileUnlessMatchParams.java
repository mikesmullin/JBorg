package com.sdd.jborg.scripts.params;

public final class AppendLineToFileUnlessMatchParams extends Params
{
	private Sudoable sudoable = new Sudoable();

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public AppendLineToFileUnlessMatchParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public AppendLineToFileUnlessMatchParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public AppendLineToFileUnlessMatchParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}

	private String match = "";
	public AppendLineToFileUnlessMatchParams setMatch(final String match) {
		this.match = match;
		return this;
	}
	public String getMatch()
	{
		return match;
	}

	private String append = "";
	public AppendLineToFileUnlessMatchParams setAppend(final String append) {
		this.append = append;
		return this;
	}
	public String getAppend()
	{
		return append;
	}
}