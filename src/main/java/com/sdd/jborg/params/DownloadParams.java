package com.sdd.jborg.params;

public final class DownloadParams extends Params
{
	private String checksum;

	public String getChecksum()
	{
		return checksum;
	}

	public DownloadParams setChecksum(final String checksum)
	{
		this.checksum = checksum;
		return this;
	}

	private Ownable ownable = new Ownable();

	public String getOwner()
	{
		return ownable.getOwner();
	}

	public DownloadParams setOwner(final String owner)
	{
		ownable.setOwner(owner);
		return this;
	}

	public String getGroup()
	{
		return ownable.getGroup();
	}

	public DownloadParams setGroup(final String group)
	{
		ownable.setGroup(group);
		return this;
	}

	private Modeable modeable = new Modeable();

	public String getMode()
	{
		return modeable.getMode();
	}

	public DownloadParams setMode(final String mode)
	{
		modeable.setMode(mode);
		return this;
	}

	private Sudoable sudoable = new Sudoable();

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public DownloadParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public DownloadParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public DownloadParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}
}