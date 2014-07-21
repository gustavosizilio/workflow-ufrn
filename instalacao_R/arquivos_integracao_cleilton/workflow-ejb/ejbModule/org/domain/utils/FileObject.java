package org.domain.utils;

public class FileObject {

	private String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public FileObject(){
		
	}
	
	public FileObject(String s){
		fileName = s;
	}
	
	
}