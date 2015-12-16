package com.sdd.jborg.params;

public final class ServiceParams extends Params
{
	private String action = "start";

	public String getAction()
	{
		return action;
	}

	public ServiceParams setAction(final String action)
	{
		this.action = action;
		return this;
	}
}