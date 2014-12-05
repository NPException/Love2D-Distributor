package de.npe.lovedist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Main {
	public static void main(String[] args) {
		try {
			PrintStream err = new PrintStream(new FileOutputStream("lovedist_error.log"), false);
			System.setErr(err);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String configfile = null;
		if (args.length > 0) {
			configfile = args[0];
		}
		
		LoveDist ld = new LoveDist();
		if (!ld.loadConfig(configfile)) return;
		
		System.out.println("=== CREATING DISTRIBUTION FILES ===");
		System.out.println();
		
		File lovefile = ld.createLoveFile();
		ld.createWindowsRelease(lovefile);
		ld.createOSXRelease(lovefile);
		
		System.out.println("=== SUCCESS ===");
		System.out.println();
	}
}
