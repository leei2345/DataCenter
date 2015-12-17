package com.jinba.scheduled.dianping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;

import com.jinba.spider.core.HttpMethod;
import com.jinba.utils.ConfigUtils;
import com.jinba.utils.LoggerUtil;

public class ImageClawer implements Runnable {

	private static String imgeFilePath;
	private static ExecutorService threadPool = null;
	private static final String THREADPOOLCONF = "dpimgclaw.thread.pool";
	private static final String IMAGEPATHCONF = "image.file.path";
	protected HttpMethod http = null;
	private String imageUrl;
	private int targetId;
	private String identidy;

	static {
		imgeFilePath = ConfigUtils.getValue(IMAGEPATHCONF);
		String threadPoolStr = ConfigUtils.getValue(THREADPOOLCONF);
		threadPool = Executors.newFixedThreadPool(Integer.parseInt(threadPoolStr));
	}
	
	public ImageClawer (String url, int targetId, String identidy) {
		this.http = new HttpMethod(targetId);
		this.imageUrl = url;
		this.targetId = targetId;
		this.identidy = identidy;
	}
	
	public static void ExecutorClaw (ImageClawer imageClawer) {
		threadPool.execute(imageClawer);
	}
	
	public void run() {
		if (StringUtils.isBlank(imageUrl)) {
			LoggerUtil.ImageInfoLog("[ImageClaw][" + targetId + "][" + identidy + "][" + imageUrl + "][Fail]");
			return;
		}
		byte[][] imageClawRes = http.GetImageByteArr(imageUrl);
		String fileType = new String(imageClawRes[1]);
		if (StringUtils.equals(fileType, "txt") || StringUtils.isBlank(fileType)) {
			LoggerUtil.ImageInfoLog("[ImageClaw][" + targetId + "][" + identidy + "][" + imageUrl + "][Fail]");
			return;
		}
		String filePath = imgeFilePath + targetId + "/" + identidy + "." + fileType;
		File dir = new File(imgeFilePath + targetId + "/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		OutputStream imageStream = null;
		try {
			imageStream = new FileOutputStream(filePath);
			byte[] imageArr = imageClawRes[0];
			imageStream.write(imageArr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LoggerUtil.ImageInfoLog("[ImageClaw][" + targetId + "][" + identidy + "][" + imageUrl + "][Error]");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			LoggerUtil.ImageInfoLog("[ImageClaw][" + targetId + "][" + identidy + "][" + imageUrl + "][Error]");
			return;
		} finally {
			if (imageStream != null) {
				try {
					imageStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		LoggerUtil.ImageInfoLog("[ImageClaw][" + targetId + "][" + identidy + "][" + imageUrl + "][Succ]");
	}

}
