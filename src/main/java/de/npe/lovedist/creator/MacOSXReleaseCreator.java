package de.npe.lovedist.creator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import de.npe.lovedist.DistConfig;
import de.npe.lovedist.helper.FileUtil;
import de.npe.lovedist.helper.ZipUtil;

public class MacOSXReleaseCreator {
	
	private enum PListState {
		searching, bundleName, bundleID, removeArray
	}
	
	public static void create(DistConfig config, File loveFile) {
		System.out.println("--> MAC OSX APP <--");
		
		if (config.love_osx_app == null || config.love_osx_app.trim().isEmpty()) {
			System.out.println("No love.apps folder specified");
			return;
		}
		
		File loveAppFolder = new File(config.love_osx_app.trim());

		if (!loveAppFolder.exists()) {
			System.out.println("The folder specified does not exist: " + loveAppFolder);
			return;
		}
		
		// prepare target folder
		File targetfolder = new File(config.targetFolder, "osx");
		targetfolder.mkdirs();

		File gameAppFolder = new File(targetfolder, config.name + ".app");
		if (gameAppFolder.exists()) {
			FileUtil.deleteRecursively(gameAppFolder);
		}
		
		// copy .app folder
		try {
			System.out.println("Copying love.app folder to \"" + gameAppFolder.getName() + "\"");
			FileUtil.copyRecursively(loveAppFolder, gameAppFolder);
			
			System.out.println("Copying \"" + loveFile.getName() + "\" to .app folder");
			File targetLoveFile = new File(gameAppFolder, "Contents/Resources/" + loveFile.getName());
			Files.copy(loveFile.toPath(), targetLoveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// read plist file
		System.out.println("Reading Info.plist");
		File infoPlist = new File(gameAppFolder, "Contents/Info.plist");
		
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(infoPlist))) {
			String line = reader.readLine();
			while (line != null) {
				lines.add(line);
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		// modify content of plist file
		System.out.println("Modifying Info.plist");
		List<String> modLines = new ArrayList<>();
		
		PListState state = PListState.searching;
		for (String line : lines) {
			switch (state) {
				case searching: {
					modLines.add(line);
					if (line.contains("CFBundleIdentifier")) {
						state = PListState.bundleID;
					} else if (line.contains("CFBundleName")) {
						state = PListState.bundleName;
					} else if (line.contains("UTExportedTypeDeclarations")) {
						modLines.remove(modLines.size()-1);
						state = PListState.removeArray;
					}
					break;
				}
				case bundleID: {
					StringBuilder sb = new StringBuilder(line.substring(0, line.indexOf("<string>")));
					sb.append("<string>");
					sb.append(config.osx_bundle_identitfier);
					sb.append("</string>");
					modLines.add(sb.toString());
					state = PListState.searching;
					break;
				}
				case bundleName: {
					StringBuilder sb = new StringBuilder(line.substring(0, line.indexOf("<string>")));
					sb.append("<string>");
					sb.append(config.name);
					sb.append("</string>");
					modLines.add(sb.toString());
					state = PListState.searching;
					break;
				}
				case removeArray: {
					if (line.contains("</array>")) {
						state = PListState.searching;
					}
				}
			}
		}
		
		System.out.println("Saving Info.plist");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoPlist))) {
			for (String line : modLines) {
				writer.append(line);
				writer.append("\n");
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		ZipUtil zipper = new ZipUtil(targetfolder.getAbsolutePath(), true);
		zipper.generateFileList();
		zipper.zipIt(new File(config.targetFolder, config.name + " osx.zip"));
		
		System.out.println("Deleting temporary folder");
		FileUtil.deleteRecursively(targetfolder);
		System.out.println();
	}
}
