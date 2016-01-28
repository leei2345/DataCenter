package com.jinba.spider.core;

import com.jinba.pojo.ImageType;
import com.jinba.utils.ConfigUtils;


public class ImageParser {
	
	private static final String NEWSIMGPATHHEAD;
	
	static {
		NEWSIMGPATHHEAD = ConfigUtils.getValue("news.img.pathhead");
	}
	
	public static String parseImagesByUrl(String imageUrl, String fromhost, String fromkey, String imageName, int targetId) {
		String path = fromhost + "/" + fromkey + "/";
		String newImageUrl = saveImage(imageUrl, path, imageName, targetId);
		return newImageUrl;
	}

	private static String saveImage(String imageUrl, String path, String imgName, int targetId) {
		String imgPath = NEWSIMGPATHHEAD + path;
		ImageClawer imgClawer = new ImageClawer(ImageType.NewsImage, imageUrl, targetId, imgPath, imgName);
		ImageClawer.ExecutorClaw(imgClawer);
		return imgPath + imgName + ".jpg";
	}
	
}
