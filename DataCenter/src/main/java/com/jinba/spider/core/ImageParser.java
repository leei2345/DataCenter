package com.jinba.spider.core;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jinba.pojo.ImageType;
import com.jinba.utils.ConfigUtils;


public class ImageParser {
	
	private static final String IMAGEPATHCONF = "image.dir.path";
	private static final String NEWSIMAGECONF = "news.img.path";
	private static String imgeFirstPath;
	private static String newsImagePath;
	static {
		imgeFirstPath = ConfigUtils.getValue(IMAGEPATHCONF);
		newsImagePath = ConfigUtils.getValue(NEWSIMAGECONF);
	}
	
	public static String parseImagesByUrl(String imageUrl, String fromhost, String fromkey, String imageName, int targetId) {
		String path = imgeFirstPath + newsImagePath +  fromhost + "/" + fromkey + "/";
		String newImageUrl = saveImage(imageUrl, path, imageName, targetId);
		return newImageUrl;
	}

	private static String saveImage(String imageUrl, String path, String imgName, int targetId) {
		ImageClawer imgClawer = new ImageClawer(ImageType.NewsImage, imageUrl, targetId, path, imgName);
		ImageClawer.ExecutorClaw(imgClawer);
		return path + imgName + ".jpg";
	}
	
	public static String parseImages(String articleHtml, String baseUrl, String path, String imgName, int targetId) {
		String newArticleHtml = articleHtml;
		if (baseUrl != null && articleHtml != null) {
			Document article = Jsoup.parse(articleHtml, baseUrl);
			Elements elements = article.getElementsByTag("img");
			for (Element element : elements) {
				String imageUrl = element.attr("abs:src");
				if (StringUtils.isBlank(imageUrl)) {
					imageUrl = element.attr("abs:data-src");
				}
				if(null != imageUrl && !imageUrl.isEmpty()) {
					String newImageUrl = saveImage(imageUrl, path, imgName, targetId);
					if(null != newImageUrl) {
						newArticleHtml = newArticleHtml.replace(imageUrl, newImageUrl);
					}
				}
			}
		}
		return newArticleHtml;
	}
	
}
