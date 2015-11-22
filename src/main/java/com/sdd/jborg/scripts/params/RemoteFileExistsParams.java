package com.sdd.jborg.scripts.params;

import com.sdd.jborg.util.Callback0;

public final class RemoteFileExistsParams extends Params
{
	private String path = "";
	private String compareLocalFile;
	private String compareChecksum;
	private Sudoable sudoable = new Sudoable();
	private Callback0 trueCallback;
	private Callback0 falseCallback;

	public String getPath()
	{
		return path;
	}

	public RemoteFileExistsParams setPath(final String path)
	{
		this.path = path;
		return this;
	}

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public RemoteFileExistsParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public RemoteFileExistsParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public RemoteFileExistsParams invokeTrueCallback()
	{
		if (trueCallback != null)
			trueCallback.call();
		return this;
	}

	public RemoteFileExistsParams setTrueCallback(final Callback0 trueCb)
	{
		this.trueCallback = trueCb;
		return this;
	}

	public RemoteFileExistsParams invokeFalseCallback()
	{
		if (falseCallback != null)
			falseCallback.call();
		return this;
	}

	public RemoteFileExistsParams setFalseCallback(final Callback0 falseCb)
	{
		this.falseCallback = falseCb;
		return this;
	}

	public RemoteFileExistsParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}

	public String getCompareLocalFile()
	{
		return this.compareLocalFile;
	}

	public RemoteFileExistsParams setCompareLocalFile(final String CompareLocalFile)
	{
		this.compareLocalFile = CompareLocalFile;
		return this;
	}

	public String getCompareChecksum()
	{
		return this.compareChecksum;
	}

	public RemoteFileExistsParams setCompareChecksum(final String CompareChecksum)
	{
		this.compareChecksum = CompareChecksum;
		return this;
	}

	private Modeable modeable = new Modeable();

	public String getMode()
	{
		return modeable.getMode();
	}

	public RemoteFileExistsParams setMode(final String mode)
	{
		modeable.setMode(mode);
		return this;
	}
}