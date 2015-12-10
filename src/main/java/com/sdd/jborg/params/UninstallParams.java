package com.sdd.jborg.params;

public final class UninstallParams extends Params
{
	private boolean purge;

	public boolean isPurge()
	{
		return purge;
	}

	public UninstallParams setPurge(final boolean purge)
	{
		this.purge = purge;
		return this;
	}
}