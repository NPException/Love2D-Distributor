package de.npe.lovedist.creator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import de.npe.lovedist.DistConfig;
import de.npe.lovedist.helper.FileUtil;
import de.npe.lovedist.helper.ZipUtil;

public class WindowsReleaseCreator {
	public static void create(DistConfig config, File loveFile) {
		System.out.println("--> WIN32 ZIP <--");
		makeWinFiles(config.love_win32bit, "win32", loveFile, config.targetFolder, config.name);
		System.out.println("--> WIN64 ZIP <--");
		makeWinFiles(config.love_win64bit, "win64", loveFile, config.targetFolder, config.name);
	}

	private static void makeWinFiles(String winFolder, String target, File loveFile, String releaseTargetFolder, String gameName) {
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
		File[] dlls = lovefolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (!name.endsWith(".dll"))
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

		File gameEXE = new File(targetfolder, gameName + ".exe");
		System.out.println("Creating \"" + gameEXE.getName() + "\"");
		FileUtil.concatFiles(loveEXE, loveFile, gameEXE);

		System.out.println("Copying .dll files");
		for (File dll : dlls) {
			File targetDll = new File(targetfolder, dll.getName());
			try {
				Files.copy(dll.toPath(), targetDll.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ZipUtil zipper = new ZipUtil(targetfolder.getAbsolutePath(), true);
		zipper.generateFileList();
		zipper.zipIt(new File(releaseTargetFolder, gameName + " " + target + ".zip"));
		
		System.out.println("Deleting temporary folder");
		FileUtil.deleteRecursively(targetfolder);
		System.out.println();
	}
}
