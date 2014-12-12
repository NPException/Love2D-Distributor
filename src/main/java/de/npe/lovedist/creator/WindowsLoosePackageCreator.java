package de.npe.lovedist.creator;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import de.npe.lovedist.DistConfig;
import de.npe.lovedist.helper.FileUtil;
import de.npe.lovedist.helper.ZipUtil;

public class WindowsLoosePackageCreator {
	public static void create(DistConfig config) {
		System.out.println("--> WIN32 LOOSE PACKAGE ZIP <--");
		makeWinFiles(config.love_win32bit, "win32 loose", config.srcFolder, config.targetFolder, config.name);
		System.out.println("--> WIN64 LOOSE PACKAGE ZIP <--");
		makeWinFiles(config.love_win64bit, "win64 loose", config.srcFolder, config.targetFolder, config.name);
	}

	private static void makeWinFiles(String winFolder, String target, String sourceFolderPath, String releaseTargetFolder, String gameName) {
		if (winFolder == null || winFolder.trim().isEmpty()) {
			System.out.println("No binaries folder specified");
			return;
		}
		winFolder = winFolder.trim();
		File lovefolder = new File(winFolder);

		if (!lovefolder.exists()) {
			System.out.println("The folder specified does not exist: " + winFolder);
			return;
		}

		File loveEXE = new File(lovefolder, "love.exe");
		if (!loveEXE.exists()) {
			System.out.println("The love.exe does not exist: " + loveEXE.getAbsolutePath());
			return;
		}
		
		File[] files = lovefolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (!(name.endsWith(".dll") || name.equals("love.exe")))
					return false;
				File f = new File(dir, name);
				return f.isFile();
			}
		});
		

		// prepare target folder
		File targetfolder = new File(releaseTargetFolder, target);
		if (targetfolder.exists()) {
			FileUtil.deleteRecursively(targetfolder);
		}
		targetfolder.mkdirs();

		System.out.println("Copying Love2D files");
		for (File file : files) {
			File targetFile = new File(targetfolder, file.getName());
			try {
				Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		System.out.println("Copying source folder");
		File sourceFolder = new File(sourceFolderPath);
		try {
			FileUtil.copyRecursively(sourceFolder, new File(targetfolder, sourceFolder.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Creating batch file");
		File batchFile = new File(targetfolder, "_RUN_ " + gameName + ".bat");
		try (FileWriter writer = new FileWriter(batchFile)) {
			writer.write("love \"" + sourceFolder.getName() + "\"");
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		ZipUtil zipper = new ZipUtil(targetfolder.getAbsolutePath(), true);
		zipper.generateFileList();
		zipper.zipIt(new File(releaseTargetFolder, gameName + " " + target + ".zip"));
		
		System.out.println("Deleting temporary folder");
		FileUtil.deleteRecursively(targetfolder);
		System.out.println();
	}
}
