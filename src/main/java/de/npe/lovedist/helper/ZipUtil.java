package de.npe.lovedist.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	private List<String> fileList;
	private String srcFolder;
	private boolean contentOnly;

	public ZipUtil(String srcFolder, boolean contentOnly) {
		fileList = new ArrayList<String>();
		this.srcFolder = srcFolder;
		this.contentOnly = contentOnly;
	}

	public void zipIt(File zipFile) {
		byte[] buffer = new byte[1024];
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			String source = contentOnly ? "" : (srcFolder.substring(srcFolder.lastIndexOf("/") + 1, srcFolder.length()) + "/");
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);
			FileInputStream in = null;

			for (String file : this.fileList) {
				ZipEntry ze = new ZipEntry(source + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(srcFolder + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} finally {
					in.close();
				}
			}

			zos.closeEntry();
			System.out.println("Folder successfully compressed");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void generateFileList() {
		generateFileList(new File(srcFolder));
	}

	private void generateFileList(File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.toString()));

		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateZipEntry(String file) {
		return file.substring(srcFolder.length() + 1, file.length());
	}
}