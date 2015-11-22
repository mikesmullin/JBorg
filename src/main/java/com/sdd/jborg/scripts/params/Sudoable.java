package com.sdd.jborg.scripts.params;

class Sudoable
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
		this.sudo = "sudo -u " + sudoer + " ";
	}

	public void setSudo(final boolean sudo)
	{
		this.sudo = sudo ? "sudo " : "";
	}
}