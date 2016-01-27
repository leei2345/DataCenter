package com.jinba.spider.core;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jinba.utils.ConfigUtils;
import com.jinba.utils.LoggerUtil;


public class ImageParser {
	
	private static final String NEWSIMGPATHHEAD;
	
	static {
		NEWSIMGPATHHEAD = ConfigUtils.getValue("news.img.pathhead");
	}
	
	public static String parseImagesByUrl(String imageUrl, String fromhost, String fromkey, int targetId) {
		String newPath;
		String newImageUrl = saveImage(imageUrl);
		if(null != newImageUrl) {
			newArticleHtml = newArticleHtml.replace(imageUrl, newImageUrl);
		}
		return ;
	}

	private static String saveImage(String imageUrl, String path, String imgName, int targetId) {
		String imgPath = NEWSIMGPATHHEAD + path;
		ImageClawer imgClawer = new ImageClawer(imageUrl, targetId, path, imgName);
		
		return null;
	}
	
}
