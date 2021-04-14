package com.ai.aiIdentification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.aiIdentification.service.IImageService;
import com.ai.aiIdentification.util.GdalImage;

/**
 * @author Jerome Guo
 * @version 0.01
 * @date 2021/04/07
 * */
@RequestMapping("/image")
@RestController
public class ImageController {
	@Value("${imageService.basePath}")
	private String basePath;//工作空间
	@Autowired
	private IImageService iImageService;
//	public static final String inPutTiffPath = "D:\\guo\\data\\GF2_PMS2_E113.2_N34.9_20201107_WGS84-MSS2.tiff";//要求TIFF必须为4326投影
//	public static final String inPutTiffPath = "D:\\guo\\data\\test\\GF2_PMS2_E113.tif";
	public static final String inPutTiffPath = "D:\\guo\\data\\F42A03111310z0.tif";
    public static GdalImage gdalImage = new GdalImage(inPutTiffPath);
	@GetMapping("/recognition")
	public String imageRecognition() {
		boolean tab =false;
		String workPath=iImageService.workFolder();
		String clipImage=gdalImage.clipTiff(114.117138, 114.126009, 30.471375, 30.478209,workPath);
		GdalImage tempImage = new GdalImage(clipImage);
		String tempPng=tempImage.tif2png();
		tab=iImageService.organizeFolder(new String[]{tempPng}, basePath);
		if(!tab) {
			return "请清理目录";
		}
		iImageService.imageRecognition();
		tab=iImageService.organizeFolder(new String[]{basePath+"/label_img.png",basePath+"/class.txt",basePath+"/input.png"}, workPath);
		if(!tab) {
			return "请清理目录";
		}
		String geo=tempImage.createJson(workPath+"/class.txt");
		/*GdalImage tempImage = new GdalImage("D:\\guo\\Test\\1617950975974\\clip1.tif");
		String geo=tempImage.createJson("D:\\guo\\Test\\1617950975974\\class.txt");*/
		return geo;
	}
}
