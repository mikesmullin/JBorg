package com.sdd.jborg.params;

import com.sdd.jborg.scripts.Standard;

public final class ExecuteParams extends Params
{
	private ScriptRemoteTestCallback testCb;
	private Sudoable sudoable = new Sudoable();
	private int retryTimes;
	private Integer expectCode;
	private boolean ignoreErrors = false;
	private String prefix = "";

	public ExecuteParams setTest(final ScriptRemoteTestCallback testCb)
		throws Standard.SkipException, Standard.AbortException
	{
		this.testCb = testCb;
		return this;
	}

	public ScriptRemoteTestCallback getTest()
	{
		return testCb;
	}

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public ExecuteParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public ExecuteParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public ExecuteParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}

	public int getRetryTimes()
	{
		return this.retryTimes;
	}

	public ExecuteParams setRetry(final int times)
	{
		this.retryTimes = times;
		return this;
	}

	public Integer getExpectCode()
	{
		return expectCode;
	}

	public ExecuteParams setExpectCode(final Integer code)
	{
		this.expectCode = code;
		return this;
	}

	public boolean isIgnoringErrors()
	{
		return ignoreErrors;
	}

	public ExecuteParams setIgnoreErrors(final boolean ignoreErrors)
	{
		this.ignoreErrors = ignoreErrors;
		return this;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public ExecuteParams setPrefix(final String prefix)
	{
		this.prefix = prefix;
		return this;
	}
}
