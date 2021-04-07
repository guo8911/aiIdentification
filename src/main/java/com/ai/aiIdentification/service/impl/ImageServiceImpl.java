package com.ai.aiIdentification.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ai.aiIdentification.service.IImageService;
@Service
public class ImageServiceImpl implements IImageService {

	@Value("${imageService.basePath}")
	private String basePath;//工作空间
	/**
	 * 执行exe文件
	 * */
	@Override
	public String imageRecognition() {
		// TODO Auto-generated method stub
		BufferedReader bufferedReader = null;
		String command=basePath+"/satellite.exe";
		File exeDir = new File(basePath);
        try {
        	// 执行命令返回执行的子进程对象
            Process proc = Runtime.getRuntime().exec(command,null,exeDir);
//            Process proc = Runtime.getRuntime().exec(command);

            // 获取子进程的错误流，并打印
            bufferedReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            
            String line = null;
			while ((line = bufferedReader.readLine()) != null) {
			    System.out.println(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                }
            }
        }
        System.out.println("处理完毕！");
		return null;
	}
	/**
	 * 整理文件目录
	 * */
	@Override
	public String organizeFolder() {
		// TODO Auto-generated method stub
		Date date = new Date();
		String newPath=basePath+"/"+date.getTime();
		File filePath=new File(newPath);
		if(!filePath.exists()) {
			filePath.mkdirs();
		}
		File classFile=new File(basePath+"/class.txt");
		File labelFile=new File(basePath+"/label_img.png");
		classFile.renameTo(new File(filePath+ File.separator+ classFile.getName()));
		labelFile.renameTo(new File(filePath+ File.separator+ labelFile.getName()));
		return null;
	}

}
