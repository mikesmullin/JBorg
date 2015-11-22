package com.sdd.jborg.scripts.params;

import com.sdd.jborg.scripts.Standard;

public interface ScriptRemoteTestCallback
{
	void call(final int code, final String out, final String err)
		throws Standard.SkipException, Standard.AbortException;
}