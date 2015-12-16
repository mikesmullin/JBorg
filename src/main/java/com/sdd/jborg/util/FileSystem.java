package com.sdd.jborg.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.sdd.jborg.scripts.Standard.*;

public class FileSystem
{
	public static String readFileToString(final String file)
	{
		return new String(readFileToBytes(getResourcePath(file)), StandardCharsets.UTF_8);
	}

	private static void giveUp(final String file, final Exception e)
	{
		die(new RuntimeException("Unable to find file \"" + file + "\".", e));
	}

	public static Path getResourcePath(final String file)
	{
		try
		{
			final ClassLoader classLoader = FileSystem.class.getClassLoader();
			final URL resource = classLoader.getResource(file);
			if (resource != null)
			{
				return new File(resource.getFile()).toPath();
			}
			giveUp(file, null);
		}
		catch (final Exception e)
		{
			giveUp(file, e);
		}
		return new File("").toPath(); // will never be reached
	}

	public static FileReader getFileReader(final Path file)
	{
		try
		{
			return new FileReader(file.toFile());
		}
		catch (final FileNotFoundException e)
		{
			giveUp(file.toString(), e);
			return null;
		}
	}

	public static byte[] readFileToBytes(final Path file)
	{
		try
		{
			return Files.readAllBytes(file);
		}
		catch (final IOException e)
		{
			giveUp(file.toString(), e);
			return new byte[0]; // will never be reached
		}
	}

	/**
	 * Write string to local file system.
	 */
	public static void writeStringToFile(final Path file, final String content)
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(file.toFile());
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
	public static void writeBytesToFile(final Path file, final byte[] content)
	{
		FileOutputStream writer = null;
		try
		{
			writer = new FileOutputStream(file.toFile());
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
