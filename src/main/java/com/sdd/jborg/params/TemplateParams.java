package com.sdd.jborg.params;

import java.util.Map;

public final class TemplateParams
	extends Params
{
	private String localTemplateFile;

	public String getLocalTemplateFile()
	{
		return localTemplateFile;
	}

	public TemplateParams setLocalTemplateFile(final String localTemplateFile)
	{
		this.localTemplateFile = localTemplateFile;
		return this;
	}

	private String templateBody;

	public String getTemplateBody()
	{
		return templateBody;
	}

	public TemplateParams setTemplateBody(final String templateBody)
	{
		this.templateBody = templateBody;
		return this;
	}

	private Map variables;

	public Map getVariables()
	{
		return variables;
	}

	public TemplateParams setVariables(final Map variables)
	{
		this.variables = variables;
		return this;
	}

	private Sudoable sudoable = new Sudoable();

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public TemplateParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public TemplateParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public TemplateParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}

	private Ownable ownable = new Ownable();

	public String getOwner()
	{
		return ownable.getOwner();
	}

	public TemplateParams setOwner(final String owner)
	{
		ownable.setOwner(owner);
		return this;
	}

	public String getGroup()
	{
		return ownable.getGroup();
	}

	public TemplateParams setGroup(final String group)
	{
		ownable.setGroup(group);
		return this;
	}

	private Modeable modeable = new Modeable();

	public String getMode()
	{
		return modeable.getMode();
	}

	public TemplateParams setMode(final String mode)
	{
		modeable.setMode(mode);
		return this;
	}
}
