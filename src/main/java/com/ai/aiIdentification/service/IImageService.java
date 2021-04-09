package com.ai.aiIdentification.service;

public interface IImageService {
	String workFolder();
	String imageRecognition();
	boolean organizeFolder(String[] filePath,String dsPath);
}
