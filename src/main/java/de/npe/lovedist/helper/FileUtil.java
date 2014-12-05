package de.npe.lovedist.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUtil {
	public static void concatFiles(File first, File second, File target) {
		try (FileOutputStream fos = new FileOutputStream(target)) {
			Files.copy(first.toPath(), fos);
			Files.copy(second.toPath(), fos);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void copyRecursively(File src, File target) throws IOException {
		if (src.isDirectory()) {
			target.mkdirs();
			File[] content = src.listFiles();
			for (File child : content) {
				copyRecursively(child, new File(target, child.getName()));
			}
		} else {
			Files.copy(src.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	public static void deleteRecursively(File file) {
		if (file.isDirectory()) {
			File[] content = file.listFiles();
			for (File child : content) {
				deleteRecursively(child);
			}
		}
		file.delete();
	}
}
