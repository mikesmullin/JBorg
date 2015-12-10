package com.sdd.jborg.params;

class Comparable
{
	private boolean compareLocalFile = false;

	private String compareChecksum;

	public boolean getCompareLocalFile()
	{
		return compareLocalFile;
	}

	public void setCompareLocalFile(final boolean compareLocalFile)
	{
		this.compareLocalFile = compareLocalFile;
	}

	public String getCompareChecksum()
	{
		return compareChecksum;
	}

	public void setCompareChecksum(final String compareChecksum)
	{
		this.compareChecksum = compareChecksum;
	}
}