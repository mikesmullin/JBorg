package com.sdd.jborg.util;

public final class Container<T>
{
	private T t;

	public void set(final T t)
	{
		this.t = t;
	}

	public T get()
	{
		return t;
	}
}
