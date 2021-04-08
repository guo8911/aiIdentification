package com.ai.aiIdentification.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
	private IImageService iImageService;
//	public static final String inPutTiffPath = "D:\\guo\\data\\GF2_PMS2_E113.2_N34.9_20201107_WGS84-MSS2.tiff";//要求TIFF必须为4326投影
//	public static final String inPutTiffPath = "D:\\guo\\data\\test\\GF2_PMS2_E113.tif";
	public static final String inPutTiffPath = "D:\\guo\\data\\F42A03111310z0.tif";
    public static GdalImage gdalImage = new GdalImage(inPutTiffPath);
	@GetMapping("/recognition")
	public String imageRecognition() {
		gdalImage = new GdalImage("D:\\guo\\data\\clip1.tif");
		gdalImage.tif2png();
//		gdalImage.clipTiff(114.117138, 114.126009, 30.471375, 30.478209);
//		iImageService.imageRecognition();
//		iImageService.organizeFolder();
		return "T";
	}
}
