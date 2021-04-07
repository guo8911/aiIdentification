package com.ai.aiIdentification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.aiIdentification.service.IImageService;

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
	@GetMapping("/recognition")
	public String imageRecognition() {
		iImageService.imageRecognition();
		iImageService.organizeFolder();
		return "";
	}
}
