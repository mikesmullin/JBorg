package com.sdd.jborg.params;

import static com.sdd.jborg.scripts.Standard.*;
import com.sdd.jborg.util.FileSystem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class TemplateParams
	extends Params
{
	private Path localTemplateFile;

	public Path getLocalTemplateFile()
	{
		return localTemplateFile;
	}

	public TemplateParams setLocalTemplateFile(final Class cls, final String... localTemplateFile)
	{
		this.localTemplateFile = FileSystem.getResourcePath(cls, localTemplateFile);
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

	private Map<String, Object> variables = new HashMap<>();

	public Map<String, Object> getVariables()
	{
		return variables;
	}

	public TemplateParams setVariables(final Map<String, Object> variables)
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
