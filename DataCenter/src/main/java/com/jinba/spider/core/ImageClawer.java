package com.jinba.spider.core;

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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.pojo.ImageType;
import com.jinba.utils.ConfigUtils;
import com.jinba.utils.LoggerUtil;

public class ImageClawer implements Runnable {

	private static String imgeFirstPath;
	private static String entityImagePath;
	private static String newsImagePath;
	private static ExecutorService threadPool = null;
	private static BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private static final String THREADPOOLCONF = "imgclaw.thread.pool";
	private static final String IMAGEPATHCONF = "image.dir.path";
	private static final String ENTITYIMAGECONF = "entity.img.path";
	private static final String NEWSIMAGECONF = "news.img.path";
	protected HttpMethod http = null;
	private String imageUrl;
	private int targetId;
	private String pathHead;
	private String path;
	private String imgName;
	private String completeImgName;

	static {
		imgeFirstPath = ConfigUtils.getValue(IMAGEPATHCONF);
		entityImagePath = ConfigUtils.getValue(ENTITYIMAGECONF);
		newsImagePath = ConfigUtils.getValue(NEWSIMAGECONF);
		String threadPoolStr = ConfigUtils.getValue(THREADPOOLCONF);
		threadPool = new ThreadPoolExecutor(Integer.parseInt(threadPoolStr), Integer.parseInt(threadPoolStr), 60000, TimeUnit.MILLISECONDS, queue);
	}
	
	public ImageClawer (ImageType type, String url, int targetId, String path, String imgName) {
		if (type.equals(ImageType.EntityImage)) {
			pathHead = imgeFirstPath + entityImagePath;
		} else if (type.equals(ImageType.NewsImage)) {
			pathHead = imgeFirstPath + newsImagePath;
		}
		this.http = new HttpMethod(targetId);
		this.imageUrl = url;
		this.targetId = targetId;
		this.path = path;
		this.imgName = imgName;
	}
	
	public void addHeader (String key, String value) {
		http.AddHeader(Method.Get, key, value);
	}
	
	public static void ExecutorClaw (ImageClawer imageClawer) {
		threadPool.execute(imageClawer);
	}
	
	public String getCompleteImgName() {
		return completeImgName;
	}

	public void run() {
		if (StringUtils.isBlank(imageUrl)) {
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + path + "][" + imageUrl + "][Fail]");
			return;
		}
		String dirPath = pathHead + path;
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File jpgImg = new File(dirPath + imgName + ".jpg");
		if (jpgImg.exists()) {
			return;
		}
		File pngImg = new File(dirPath + imgName + ".png");
		if (pngImg.exists()) {
			return;
		}
		byte[][] imageClawRes = http.GetImageByteArr(imageUrl);
		if (imageClawRes == null || imageClawRes[0] ==null || imageClawRes[1] == null) {
			return;
		}
		String fileType = new String(imageClawRes[1]);
		if (StringUtils.equals(fileType, "txt") || StringUtils.isBlank(fileType)) {
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + path + "][" + imageUrl + "][Fail]");
			return;
		}
		String filePath = "";
		if (!StringUtils.isBlank(imgName)) {
			filePath = dirPath + imgName + "." + fileType;
		} else {
			return;
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		this.completeImgName = imgName + "." + fileType;
		OutputStream imageStream = null;
		try {
			imageStream = new FileOutputStream(filePath);
			byte[] imageArr = imageClawRes[0];
			imageStream.write(imageArr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + path + "][" + imageUrl + "][Error]");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + path + "][" + imageUrl + "][Error]");
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
		LoggerUtil.ImageInfoLog("[ImageClaw][Queue Size " + queue.size() + "][" + targetId + "][" + path + "][" + imageUrl + "][Succ]");
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml","scheduled.xml"});
		application.start();
	}

}
