/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.util;

import java.io.File;

/**
 * 
 * Helper class for file system related operations.
 * 
 * @author Ruediger Gad
 *
 */
public class FileSystemHelper {

	public static int mkfifo(String path) {
		try {
			Process p = Runtime.getRuntime().exec("mkfifo " + path);
			return p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return 2;
		}
	}
	
	public static boolean rmFile(String path) {
		File f = new File(path);
		if (f.exists()) {
			return f.delete();
		}
		return true;
	}
	
	public static boolean fileExists(String path) {
		return new File(path).exists();
	}
}
