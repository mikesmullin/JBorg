package com.sdd.jborg.scripts.params;

import com.sdd.jborg.util.Callback0;

public class Params
{
	public Callback0 callback;

	public void callImmediate()
	{
		callback.call();
	}
}
