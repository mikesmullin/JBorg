package com.sdd.jborg.params;

class Ownable
{
	private String owner = "";
	private String group = "";

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(final String owner)
	{
		this.owner = owner;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(final String group)
	{
		this.group = group;
	}
}
