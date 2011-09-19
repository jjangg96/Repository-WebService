package com.freegine.repo.library;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;


public class ZipUtil {
	public static InputStream zip(RepoScheme[] schemes) throws IOException
	{
		File tmpFile = File.createTempFile("" + System.currentTimeMillis(), ".zip");
		
		if(zip(schemes, tmpFile.getPath()))
		{
			System.out.println(tmpFile.getPath() + " created");
			FileInputStream fis = new FileInputStream(tmpFile);
			
			return fis;
		}
		else
		{
			System.out.println("Zip failed");
			return null;
		}
	}
	
	public static boolean zip(RepoScheme[] schemes, String outputFile) throws IOException
	{
		if(schemes.length <= 0)
		{
			System.out.println("Scheme size is 0");
			//목록이 없음 
			return false;
		}

		if(outputFile.toLowerCase().indexOf(".zip") <= 0)
		{
			//output 파일이 .zip이 아니면
			System.out.println("suffix is not .zip " + outputFile);
			return false;
		}

		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zos = null;


		fos = new FileOutputStream(outputFile);
		bos = new BufferedOutputStream(fos);
		zos = new ZipOutputStream(bos);

		zos.setLevel(8); // COMPRESSION_LEVEL

		//inputstream compression
		for(RepoScheme scheme : schemes)
		{
			zipEntry(scheme.inputStream, scheme.UUID + "/" + scheme.filename, zos);	
		}
		
		zos.finish();

		if (zos != null)
			zos.close();
		if(bos != null)
			bos.close();
		if(fos != null)
			fos.close();



		return true;
	}

	public static boolean zip(String[] sourceFiles, String outputFile) throws IOException
	{
		if(sourceFiles.length <= 0)
			//source 목록이 없으면 
			return false;

		for(String source : sourceFiles)
		{
			//source 목록 중 파일이 없으면 
			if(!(new File(source)).exists())
				return false;
		}

		if(outputFile.toLowerCase().indexOf(".zip") <= 0)
			//output 파일이 .zip이 아니면 
			return false;


		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zos = null;

		try 
		{
			fos = new FileOutputStream(outputFile);
			bos = new BufferedOutputStream(fos);
			zos = new ZipOutputStream(bos);

			zos.setLevel(8); // COMPRESSION_LEVEL

			//files compression
			for(String sourceFilename : sourceFiles)
			{
				File sourceFile = new File(sourceFilename); 
				String sourcePath = sourceFile.getPath();
				zipEntry(sourceFile, sourcePath, zos);
			}
			zos.finish();

		} catch (FileNotFoundException e) {

		}
		finally
		{
			if (zos != null)
				zos.close();
			if(bos != null)
				bos.close();
			if(fos != null)
				fos.close();
		}

		return true;
	}

	private static boolean zipEntry(InputStream input, String filename, ZipOutputStream zos) throws IOException
	{
		if(input == null || filename == null || filename.trim().equals(""))
			return false;


		BufferedInputStream bis = null;

		try
		{
			bis = new BufferedInputStream(input);

			ZipEntry entry = new ZipEntry(filename);
			zos.putNextEntry(entry);

			byte[] buf = new byte[2048];
			int cnt = 0;
			while((cnt = bis.read(buf, 0, buf.length)) != -1)
				zos.write(buf, 0, cnt);

			zos.closeEntry();

		}
		catch(ZipException ze)
		{
			if(ze.getMessage().indexOf("duplicate entry") >= 0)
			{
				System.out.println("Duplicated entry : " + filename);
			}
		}
		finally
		{
			if(bis != null)
				bis.close();
		}


		return true;
	}

	private static boolean zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos) throws IOException
	{
		if(!sourceFile.exists())
			return false;

		if(sourceFile.isDirectory())
		{
			//directory

			if(sourceFile.getName().equalsIgnoreCase(".metadata"))
				return false;

			File[] fileList = sourceFile.listFiles();
			for(File file : fileList)
			{
				zipEntry(file, sourcePath, zos);
			}
		}
		else if(sourceFile.isFile())
		{
			//file

			BufferedInputStream bis = null;

			try
			{
				String sourceFilePath = sourceFile.getPath();
				String zipEntryName = sourceFilePath.substring(sourcePath.length() + 1, sourceFilePath.length());

				bis = new BufferedInputStream(new FileInputStream(sourceFile));

				ZipEntry entry = new ZipEntry(zipEntryName);
				entry.setTime(sourceFile.lastModified());
				zos.putNextEntry(entry);

				byte[] buf = new byte[2048];
				int cnt = 0;
				while((cnt = bis.read(buf, 0, buf.length)) != -1)
					zos.write(buf, 0, cnt);

				zos.closeEntry();

			}
			finally
			{
				if(bis != null)
					bis.close();
			}
		}

		return true;
	}
}
