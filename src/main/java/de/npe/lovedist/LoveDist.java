package de.npe.lovedist;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.npe.lovedist.creator.MacOSXReleaseCreator;
import de.npe.lovedist.creator.WindowsReleaseCreator;
import de.npe.lovedist.helper.ZipUtil;

public class LoveDist {

	private static final String DEFAULT_CONFIG_FILE = "lovedistConfig.json";

	private DistConfig config;

	/**
	 * Returns true if the config file could be read successfully, false
	 * otherwise
	 * 
	 * @param configFileName
	 * @return
	 */
	public boolean loadConfig(String configFileName) {
		if (configFileName == null)
			configFileName = DEFAULT_CONFIG_FILE;

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		config = new DistConfig();
		try (FileReader fr = new FileReader(configFileName)) {
			config = gson.fromJson(fr, DistConfig.class);
			return true;
		} catch (Exception ex) {
			System.out.println("Could not load config from file \""
					+ configFileName + "\", creating default config file \""
					+ DEFAULT_CONFIG_FILE + "\"");
			try (FileWriter writer = new FileWriter(DEFAULT_CONFIG_FILE)) {
				gson.toJson(config, writer);
			} catch (Exception e) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	public File createLoveFile() {
		System.out.println("--> LOVE FILE <--");
		ZipUtil zipper = new ZipUtil(config.srcFolder, true);
		zipper.generateFileList();

		File targetFolder = new File(config.targetFolder);
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}
		
		File loveFile = new File(config.targetFolder, config.name + ".love");
		zipper.zipIt(loveFile);
		System.out.println();
		return loveFile;
	}
	
	public void createWindowsRelease(File loveFile) {
		WindowsReleaseCreator.create(config, loveFile);
	}
	
	public void createOSXRelease(File loveFile) {
		MacOSXReleaseCreator.create(config, loveFile);
	}
}
