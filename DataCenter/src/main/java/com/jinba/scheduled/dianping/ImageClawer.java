package com.jinba.scheduled.dianping;

import com.jinba.spider.core.HttpMethod;
import com.jinba.utils.ConfigUtils;

public class ImageClawer implements Runnable {

	private static String imgeFilePath;
	protected HttpMethod http = null;
	private String imageUrl;
	private int targetId;
	private String identidy;
	private String dirName;
	
	public ImageClawer (String url, int targetId, String identidy, String dirName) {
		this.http = new HttpMethod(targetId);
		this.imageUrl = url;
		this.targetId = targetId;
		this.identidy = identidy;
		this.dirName = dirName;
	}
	
	static {
		ConfigUtils.getValue("image.file.path");
	}
	
	
	public void run() {
		byte[][] imageClawRes = http.GetImageByteArr(imageUrl);
		String fileType = new String(imageClawRes[0]);
		
		
	}

}
