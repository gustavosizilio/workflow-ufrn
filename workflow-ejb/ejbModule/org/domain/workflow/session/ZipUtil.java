package org.domain.workflow.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	private Map<String,String> fileList;
	// private static final String OUTPUT_ZIP_FILE = "C:\\MyFile.zip";
	private String zipFile;

	ZipUtil(String zipFile) {
		this.zipFile = zipFile;
		fileList = new HashMap<String, String>();
	}

	public void zipIt() {

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);

			for (String fileEntry : this.fileList.keySet()) {
				String file = this.fileList.get(fileEntry);
				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(fileEntry);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			// remember close it
			zos.close();

			System.out.println("Done");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void addDir(String dir) {
		File d = new File(dir);
		this.generateFileList(d, dir);
	}
	
	public void addFile(String dir, String file) {
		generateFileList(new File(file), dir);
	}

	private void generateFileList(File node, String sourceDir) {

		// add file only
		if (node.isFile()) {
			fileList.put(node.getAbsoluteFile().toString(), generateZipEntry(sourceDir, node.getAbsoluteFile().toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename), sourceDir);
			}
		}

	}

	private String generateZipEntry(String sourceFolder, String file) {
		return file.substring(sourceFolder.length(), file.length());
	}
}