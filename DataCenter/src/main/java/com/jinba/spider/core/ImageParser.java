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

import com.jinba.utils.LoggerUtil;


public class ImageParser {
	
	public static String parseImages(String articleHtml, String baseUrl) {
		String newArticleHtml = articleHtml;
		if (baseUrl != null && articleHtml != null) {
			Document article = Jsoup.parse(articleHtml, baseUrl);
			Elements elements = article.getElementsByTag("img");
			for (Element element : elements) {
				String imageUrl = element.attr("abs:src");
				if(null != imageUrl && !imageUrl.isEmpty()) {
					String newImageUrl = saveImage(imageUrl);
					if(null != newImageUrl) {
						newArticleHtml = newArticleHtml.replace(imageUrl, newImageUrl);
					}
				}
			}
		} else {
			LoggerUtil.ImageInfoLog(ImageParser.class.getName() + " - Parsing image links error! URL is null or articleHtml is null!");
		}
		return newArticleHtml;
	}

	private static String saveImage(String imageUrl) {
		String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		InputStream imageStream = HttpConnection.getImageAsInputStream(imageUrl);
		if(imageStream != null) {
			int imageSize = 0;
			try {
				byte[] imageInBytes = IOUtils.toByteArray(imageStream);
				imageStream.close();
				
				String yunFilePath = "/news_photo/" + sdf.format(new Date()) + "/" + DigSign.getMD5(imageUrl, "utf-8")  + "_" + fileName;
				boolean succ = upyun.writeFile(yunFilePath, imageInBytes, true);
				if(succ) {
					String newImageUrl = "http://stocktest.b0.upaiyun.com" + yunFilePath;
					logger.info(ImageParser.class.getName() + " - Saved image URL:[" + newImageUrl + "]");
					return newImageUrl;
				} else {
					logger.error(ImageParser.class.getName() + " - Save image error! URL:[" + imageUrl + "]");
				}
			} catch (IOException e) {
				logger.error(ImageParser.class.getName() + " - Get image " + imageUrl + " Exception! Read size: " + imageSize);
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		} else {
			logger.error(ImageParser.class.getName() + " - ImageStream is null! URL:[" + imageUrl + "]");
		}
		return null;
	}
	
}
