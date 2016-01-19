package com.jinba.scheduled.dianping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;

import com.jinba.spider.core.HttpMethod;
import com.jinba.utils.ConfigUtils;
import com.jinba.utils.LoggerUtil;

public class ImageClawer implements Runnable {

	private static String imgeFilePath;
	private static ExecutorService threadPool = null;
	private static BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private static final String THREADPOOLCONF = "imgclaw.thread.pool";
	private static final String IMAGEPATHCONF = "image.file.path";
	protected HttpMethod http = null;
	private String imageUrl;
	private int targetId;
	private String targetInfo;
	private String identidy;
	private String areacode;
	private String imageDirName;

	static {
		imgeFilePath = ConfigUtils.getValue(IMAGEPATHCONF);
		String threadPoolStr = ConfigUtils.getValue(THREADPOOLCONF);
		threadPool = new ThreadPoolExecutor(Integer.parseInt(threadPoolStr), Integer.parseInt(threadPoolStr), 60000, TimeUnit.MILLISECONDS, queue);
	}
	
	public ImageClawer (String url, int targetId, String targetInfo, String identidy, String areacode, String imageDirName) {
		this.http = new HttpMethod(targetId);
		this.imageUrl = url;
		this.targetId = targetId;
		this.targetInfo = targetInfo;
		this.identidy = identidy;
		this.areacode = areacode;
		this.imageDirName = imageDirName;
	}
	
	public static void ExecutorClaw (ImageClawer imageClawer) {
		threadPool.execute(imageClawer);
	}
	
	public void run() {
		if (StringUtils.isBlank(imageUrl)) {
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + identidy + "][" + imageUrl + "][Fail]");
			return;
		}
		String dirPath = imgeFilePath + targetInfo + "/" + imageDirName + "/" + areacode + "/";
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		byte[][] imageClawRes = http.GetImageByteArr(imageUrl);
		if (imageClawRes == null || imageClawRes[0] ==null || imageClawRes[1] == null) {
			return;
		}
		String fileType = new String(imageClawRes[1]);
		if (StringUtils.equals(fileType, "txt") || StringUtils.isBlank(fileType)) {
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + identidy + "][" + imageUrl + "][Fail]");
			return;
		}
		String filePath = "";
		if (!StringUtils.isBlank(identidy)) {
			filePath = dirPath + identidy + "." + fileType;
		} else {
			return;
		}
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
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + identidy + "][" + imageUrl + "][Error]");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + identidy + "][" + imageUrl + "][Error]");
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
		LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + identidy + "][" + imageUrl + "][Succ]");
	}
	
	public static void main(String[] args) {
		ImageClawer i = new ImageClawer("http://i1.s2.dpfile.com/pc/146a7fe725a14850a8d216c0135d6952(249x249)/thumb.jpg", 1, "dianping", "00102","17648042", "shop");
		new Thread(i).start();
	}

}
