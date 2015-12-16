package com.sdd.jborg.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.sdd.jborg.scripts.Standard.*;

public class FileSystem
{
	public static String readFileToString(final String path)
	{
		return new String(readFileToBytes(path), StandardCharsets.UTF_8);
	}

	private static void giveUp(final String path, final Exception e)
	{
		die(new RuntimeException("Unable to load " + path + ".", e));
	}

	public static File findFile(final String path)
	{
		try
		{
			final ClassLoader classLoader = FileSystem.class.getClassLoader();
			final File file = new File(classLoader.getResource(path).getFile());
			return file;
		}
		catch (final Exception e)
		{
			giveUp(path, e);
			return null;
		}
	}

	public static FileReader getFileReader(final String path)
	{
		try
		{
			return new FileReader(findFile(path));
		}
		catch (final FileNotFoundException e)
		{
			giveUp(path, e);
			return null;
		}
	}

	public static byte[] readFileToBytes(final String path)
	{
		try
		{
			return Files.readAllBytes(findFile(path).toPath());
		}
		catch (IOException e)
		{
			giveUp(path, e);
			return null;
		}
	}

	/**
	 * Write string to local file system.
	 */
	public static void writeStringToFile(final Path path, final String content)
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(path.toString());
			writer.write(content);
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null)
				writer.close();
		}
	}

	/**
	 * Write bytes to local file system.
	 */
	public static void writeBytesToFile(final File file, final byte[] content)
	{
		FileOutputStream writer = null;
		try
		{
			writer = new FileOutputStream(file);
			writer.write(content);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null)
				try
				{
					writer.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
		}
	}

	/**
	 * Delete file from local file system.
	 */
	public static void unlink(final Path path)
	{
		try {
			Files.delete(path);
		}
		catch (final IOException e) {
			giveUp(path.toString(), e);
		}
	}
}
