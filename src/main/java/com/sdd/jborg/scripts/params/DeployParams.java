package com.sdd.jborg.scripts.params;

import com.sdd.jborg.util.Func1;

public final class DeployParams extends Params
{
	private String deployTo;

	public DeployParams setDeployTo(final String deployTo)
	{
		this.deployTo = deployTo;
		return this;
	}

	public String getDeployTo()
	{
		return deployTo;
	}

	public final static class GitParams
	{
		private String repo;
		private String branch;
		private String deployKey;

		public String getRepo()
		{
			return repo;
		}

		public GitParams setRepo(final String repo)
		{
			this.repo = repo;
			return this;
		}

		public String getBranch()
		{
			return branch;
		}

		public GitParams setBranch(final String branch)
		{
			this.branch = branch;
			return this;
		}

		public String getDeployKey()
		{
			return deployKey;
		}

		public GitParams setDeployKey(final String deployKey)
		{
			this.deployKey = deployKey;
			return this;
		}
	}

	private GitParams gitParams;

	public GitParams getGit()
	{
		return gitParams;
	}

	public DeployParams setGit(final Func1<GitParams, GitParams> gitParamsCallback)
	{
		this.gitParams = gitParamsCallback.call(new GitParams());
		return this;
	}

	private int keepReleases = 3; // default

	public int getKeepReleases()
	{
		return keepReleases;
	}

	public DeployParams setKeepReleases(final int amount)
	{
		this.keepReleases = amount;
		return this;
	}

	private Sudoable sudoable = new Sudoable();

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public DeployParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public DeployParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public DeployParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}

	private Ownable ownable = new Ownable();

	public String getOwner()
	{
		return ownable.getOwner();
	}

	public DeployParams setOwner(final String owner)
	{
		ownable.setOwner(owner);
		return this;
	}

	public String getGroup()
	{
		return ownable.getGroup();
	}

	public DeployParams setGroup(final String group)
	{
		ownable.setGroup(group);
		return this;
	}
}