package com.sdd.jborg.params;

import static com.sdd.jborg.scripts.Standard.*;
import com.sdd.jborg.util.FileSystem;

import java.nio.file.Path;
import java.util.Map;

public final class UploadParams extends Params
{
	private String remoteTmpFile;

	public String getRemoteTmpFile()
	{
		return remoteTmpFile;
	}

	public UploadParams setRemoteTmpFile(final String remoteTmpFile)
	{
		this.remoteTmpFile = remoteTmpFile;
		return this;
	}

	private Path localSourceFile;

	public Path getLocalSourceFile()
	{
		return localSourceFile;
	}

	public UploadParams setLocalSourceFile(final Path localSourceFile)
	{
		this.localSourceFile = localSourceFile;
		return this;
	}

	public UploadParams setLocalSourceFile(final Class cls, final String... localSourceFile)
	{
		return setLocalSourceFile(FileSystem.getResourcePath(cls, localSourceFile));
	}

	private boolean encrypted;

	public boolean isEncrypted()
	{
		return encrypted;
	}

	public UploadParams isEncrypted(final boolean encrypted)
	{
		this.encrypted = encrypted;
		return this;
	}

	private String content;

	public String getContent()
	{
		return content;
	}

	public UploadParams setContent(final String content)
	{
		this.content = content;
		return this;
	}

	private Map variables;

	public Map getVariables()
	{
		return variables;
	}

	public UploadParams setVariables(final Map variables)
	{
		this.variables = variables;
		return this;
	}

	private Sudoable sudoable = new Sudoable();

	public String getSudoCmd()
	{
		return sudoable.getSudoCmd();
	}

	public UploadParams setSudoCmd(final String cmd)
	{
		sudoable.setSudoCmd(cmd);
		return this;
	}

	public UploadParams setSudoAsUser(final String sudoer)
	{
		sudoable.setSudoAsUser(sudoer);
		return this;
	}

	public UploadParams setSudo(final boolean sudo)
	{
		sudoable.setSudo(sudo);
		return this;
	}

	private Ownable ownable = new Ownable();

	public String getOwner()
	{
		return ownable.getOwner();
	}

	public UploadParams setOwner(final String owner)
	{
		ownable.setOwner(owner);
		return this;
	}

	public String getGroup()
	{
		return ownable.getGroup();
	}

	public UploadParams setGroup(final String group)
	{
		ownable.setGroup(group);
		return this;
	}

	private Modeable modeable = new Modeable();

	public String getMode()
	{
		return modeable.getMode();
	}

	public UploadParams setMode(final String mode)
	{
		modeable.setMode(mode);
		return this;
	}
}