package com.jinba.spider.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.jinba.pojo.ProxyCheckResEntity;
import com.jinba.spider.proxy.ProxyQueue;
import com.jinba.utils.LoggerUtil;


/**
 * 数据抓取核心，http请求发起端
 * @author leei
 */
public class HttpsMethod {
	
	private HttpClient client  = null;
	private BasicCookieStore cookieStore = new BasicCookieStore();
	private HttpGet get = null;
	private HttpPost post = null;
	private static int retryCount = 4;
	private static final String DEFAULTCHARACTER = "UTF-8";
	private RequestConfig.Builder config = RequestConfig.custom();
	private HttpClientBuilder clientBuilder = HttpClientBuilder.create();
	private int identidy;
	private HttpHost proxy;
	private String getHtml = "";
	private String getException = "";
	private int getStatus = 0;
	private String postHtml = "";
	private String postException = "";
	private int postStatus = 0;

	public HttpsMethod() {
		this(0);
	}
	
	public HttpsMethod(int identidy) {
		this.identidy = identidy;
		this.config.setAuthenticationEnabled(true);
		this.config.setConnectTimeout(30000);
		this.config.setSocketTimeout(10000);
		this.clientBuilder = HttpClientBuilder.create();
		this.clientBuilder.setMaxConnTotal(100);
		this.clientBuilder.setMaxConnPerRoute(500);
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");  
			X509TrustManager tm = new X509TrustManager() {  
				public void checkClientTrusted(X509Certificate[] xcs,  
						String string) throws CertificateException {  
				}  
				public void checkServerTrusted(X509Certificate[] xcs,  
						String string) throws CertificateException {  
				}  
				public X509Certificate[] getAcceptedIssuers() {  
					return null;  
				}  
			};  
			ctx.init(null, new TrustManager[] { tm }, null);
			LayeredConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx);
			this.client = this.clientBuilder.setSSLSocketFactory(ssf).setDefaultRequestConfig(this.config.build()).setDefaultCookieStore(cookieStore).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
	}
	
	public HttpsMethod(int identidy, CloseableHttpClient client) {
		this.identidy = identidy;
		this.client = client;
	}
	
	public HttpsMethod(int identidy, BasicCookieStore cookieStore) {
		this.identidy = identidy;
		this.config.setAuthenticationEnabled(true);
		this.config.setConnectTimeout(30000);
		this.config.setSocketTimeout(30000);
		this.clientBuilder = HttpClientBuilder.create();
		this.clientBuilder.setMaxConnTotal(100);
		this.clientBuilder.setMaxConnPerRoute(500);
		this.cookieStore = cookieStore;
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");  
			X509TrustManager tm = new X509TrustManager() {  
				public void checkClientTrusted(X509Certificate[] xcs,  
						String string) throws CertificateException {  
				}  
				public void checkServerTrusted(X509Certificate[] xcs,  
						String string) throws CertificateException {  
				}  
				public X509Certificate[] getAcceptedIssuers() {  
					return null;  
				}  
			};  
			ctx.init(null, new TrustManager[] { tm }, null);
			LayeredConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx);
			this.client = this.clientBuilder.setSSLSocketFactory(ssf).setDefaultRequestConfig(this.config.build()).setDefaultCookieStore(cookieStore).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
	}
	
	public HttpsMethod(BasicCookieStore cookieStore) {
		this(0, cookieStore);
	}
	
	public BasicCookieStore getCookieStore() {
		return cookieStore;
	}

//	public static String initProxyMap() {
//		long nowTimeStemp = System.currentTimeMillis();
//		if (nowTimeStemp - timeStemp > INTERVALTIME) {
//			proxyMap = ProxyChecker.initProxyMap();
//			if (proxyMap == null) {
//				proxyMap = new ConcurrentHashMap<String, List<HttpHost>>();
//			}
//			LoggerUtil.HttpInfoLog("[HttpThings Proxy Boxs Init Complment]");
//			timeStemp = nowTimeStemp;
//		}
//		return "[HttpThings Proxy Boxs Init Complment]";
//	}

	public void SetConnectionTimeOutThreshold(Method method, int timeOut) {
		this.config.setConnectTimeout(timeOut);
		this.config.setSocketTimeout(timeOut);
		if (method.equals(Method.Get)) {
			if (this.get == null) {
				this.get = new HttpGet();
			}
			this.get.setConfig(config.build());
		} else {
			if (this.post == null) {
				
				this.post = new HttpPost();
			}
			this.post.setConfig(config.build());
		}
	}

	public void AddHeader(Method method, String name, String value) {
		if (method.equals(Method.Get)) {
			if (this.get == null) {
				this.get = new HttpGet();
			}
			this.get.addHeader(name, value);
		} else {
			if (this.post == null) {
				this.post = new HttpPost();
			}
			this.post.addHeader(name, value);
		}
	}
	
	public void RemoveAllHeaders (Method method) {
		if (method.equals(Method.Get)) {
			if (this.get == null) {
				this.get = new HttpGet();
			}
			Header[] headers = this.get.getAllHeaders();
			for (Header header : headers) {
				this.get.removeHeader(header);
			}
		} else {
			if (this.post == null) {
				this.post = new HttpPost();
			}
			Header[] headers = this.post.getAllHeaders();
			for (Header header : headers) {
				this.post.removeHeader(header);
			}
		}
	}

	public String GetHtml(String url, HttpResponseConfig httpResponseConfig) {
		boolean responseAsStream = false;
		boolean getLocation = false;
		if (httpResponseConfig == null) {
			getLocation = true;
			RequestConfig.Builder builder = this.config;
			builder.setRelativeRedirectsAllowed(false);
			builder.setCircularRedirectsAllowed(false);
			builder.setRedirectsEnabled(false);
			try {
				SSLContext ctx = SSLContext.getInstance("SSL");  
				X509TrustManager tm = new X509TrustManager() {  
					public void checkClientTrusted(X509Certificate[] xcs,  
							String string) throws CertificateException {  
					}  
					public void checkServerTrusted(X509Certificate[] xcs,  
							String string) throws CertificateException {  
					}  
					public X509Certificate[] getAcceptedIssuers() {  
						return null;  
					}  
				};  
				ctx.init(null, new TrustManager[] { tm }, null);
				LayeredConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx);
				this.client = this.clientBuilder.setSSLSocketFactory(ssf).setDefaultRequestConfig(this.config.build()).setDefaultCookieStore(cookieStore).build();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} 
		} else {
			responseAsStream = httpResponseConfig.isYesOrNo();
		}
		HttpHost proxy = null;
		String locationHeader = "";
		for (int retryIndex = 1; retryIndex <= retryCount; retryIndex++) {
			if (this.get == null) {
				this.get = new HttpGet();
			}
			this.get.setConfig(config.build());
			if (retryIndex >= retryCount) {
				this.get.abort();
				this.get.releaseConnection();
				LoggerUtil.HttpInfoLog("[数据获取][url=" + url + "][status=" + this.getStatus + "][exception=" + this.getException + "]");
				break;
			}
			if (identidy != 0) {
				ProxyCheckResEntity p = ProxyQueue.getProxy(identidy);
				proxy = new HttpHost(p.host, p.port);
				if (proxy != null) {
					this.config.setProxy(proxy);
					this.get.setConfig(config.build());
				}
			}
			if (proxy == null) {
				LoggerUtil.HttpInfoLog("[" + url + "][第" + retryIndex + "次抓取尝试]");
			} else {
				LoggerUtil.HttpInfoLog("[" + url + "][第" + retryIndex + "次抓取尝试][proxy " + proxy.toHostString() + "]");
			}
			try {
				URI uri = new URI(url);
				this.get.setURI(uri);
				HttpResponse response = this.client.execute(this.get);
				if (getLocation) {
					try {
						locationHeader = response.getFirstHeader("Location").getValue();
						break;
					} catch (Exception e) {
						this.get.abort();
						this.get.releaseConnection();
						continue;
					}
				}
				this.getStatus = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				ContentType contentType = ContentType.getOrDefault(entity);
				entity = new BufferedHttpEntity(entity);
				Charset charset = contentType.getCharset() != null ? contentType.getCharset() : getCharsetFromByte(EntityUtils.toByteArray(entity));
				String responseCharset = "";
				if (charset != null) {
					responseCharset = charset.toString();
				}
				if (StringUtils.isBlank(responseCharset)) {
					responseCharset = DEFAULTCHARACTER;
				} else if (StringUtils.equals(responseCharset.toLowerCase(), "gb2312")) {
					responseCharset = "GBK";
				}
				if (responseAsStream) {
					Header[] headers = response.getAllHeaders();
					boolean isGzip = false;
					for (Header header : headers) {
						String isGzipStr = header.getValue().toLowerCase();
						if (isGzipStr.contains("gzip")) {
							isGzip = true;
						}
					}
					InputStream is = entity.getContent();
					BufferedReader reader = null;
					if (isGzip) {
						GZIPInputStream gzipIs = new GZIPInputStream(is);
						reader = new BufferedReader(new InputStreamReader(gzipIs, responseCharset));
					} else {
						reader = new BufferedReader(new InputStreamReader(is,	responseCharset));
					}
					String line = "";
					while ((line = reader.readLine()) != null) {
						this.getHtml += line;
					}
					if (is != null) {
						is.close();
					}
					if (reader != null) {
						reader.close();
					}
				} else {
					this.getHtml = EntityUtils.toString(entity);
				}
			} catch (SocketTimeoutException e) {
				this.getException = "SocketTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (ConnectTimeoutException e) {
				this.getException = "ConnectTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (UnknownHostException e) {
				this.getException = "UnknownHostException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (IOException e) {
				this.getException = "IOException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (URISyntaxException e) {
				this.getException = "URISyntaxException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (Exception e) {
				this.getException = e.getMessage();
				e.printStackTrace();
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} finally {
				this.get.abort();
				this.get.releaseConnection();
			}
			if ((this.getStatus == 200 && !StringUtils.isBlank(this.getHtml)) || this.getStatus == 404) {
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][html=" + this.getHtml + "]");
				break;
			} else {
				this.getException = "response_null";
				continue;
			}
		}
		if ((this.getStatus == 302) && (StringUtils.isBlank(this.getHtml))) {
			this.getHtml = locationHeader;
		}
		return this.getHtml;
	}
	
	public void RemoveProxy () {
		this.proxy = null;
	}

	public String GetLocationUrl(String url) {
		return GetHtml(url, null);
	}

	public String GetLocationUrl(String url, String body,
			HttpRequestConfig httpRequsetConfig) {
		return GetHtml(url, body, httpRequsetConfig, null);
	}

	public String GetHtml(String url, String body, HttpRequestConfig httpRequestConfig, HttpResponseConfig httpResponseConfig) {
		boolean getLocation = false;
		boolean useStringEntity = httpRequestConfig.isYesOrNo();
		boolean responseStream = false;
		if (httpResponseConfig == null) {
			getLocation = true;
		} else {
			responseStream = httpResponseConfig.isYesOrNo();
		}
		String locationHeader = "";
		for (int retryIndex = 1; retryIndex <= retryCount; retryIndex++) {
			this.post = new HttpPost();
			this.post.setConfig(config.build());
			postHtml = "";
			if (retryIndex >= retryCount) {
				this.post.abort();
				this.post.releaseConnection();
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][body=" + body + "][status=" + this.postStatus + "][exception=" + this.postException + "]");
				break;
			}
			if (identidy != 0) {
				ProxyCheckResEntity p = ProxyQueue.getProxy(identidy);
				proxy = new HttpHost(p.host, p.port);
				if (proxy != null) {
					this.config.setProxy(proxy);
					this.post.setConfig(config.build());
				}
			}
			if (proxy == null) {
				LoggerUtil.HttpInfoLog("[" + url + "][第" + retryIndex + "次抓取尝试]");
			} else {
				LoggerUtil.HttpInfoLog("[" + url + "][第" + retryIndex + "次抓取尝试][proxy " + proxy.toHostString() + "]");
			}
			try {
				URI uri = new URI(url);
				this.post.setURI(uri);
				if (useStringEntity) {
					List<NameValuePair> nameValueList = new ArrayList<NameValuePair>();
					String[] params = body.split("&");
					for(String param : params) {
						String[] nameValue = param.split("\\=");
						nameValueList.add(new BasicNameValuePair(nameValue[0], nameValue.length == 1 ? "" : nameValue[1]));
					}
					UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValueList, Consts.UTF_8);
					this.post.setEntity(urlEncodedFormEntity);
				} else {
					InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
					HttpEntity inputStreamEntity = new InputStreamEntity(is);
					this.post.setEntity(inputStreamEntity);
				}
				HttpResponse response = this.client.execute(this.post);
				if (getLocation) {
					locationHeader = response.getFirstHeader("Location") == null ? "":response.getFirstHeader("Location").getValue();
					break;
				}
				this.postStatus = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				ContentType contentType = ContentType.getOrDefault(entity);
				entity = new BufferedHttpEntity(entity);
				Charset charset = contentType.getCharset() != null?contentType.getCharset():getCharsetFromByte(EntityUtils.toByteArray(entity));
				String responseCharset = "";
				if (charset != null) {
					responseCharset = charset.toString();
				}
				if (StringUtils.isBlank(responseCharset)) {
					responseCharset = "UTF-8";
				}
				if (StringUtils.equals("GB2312", responseCharset)) {
					responseCharset = "GBK";
				}
				if (responseStream) {
					Header[] headers = response.getAllHeaders();
					boolean isGzip = false;
					for (Header header : headers) {
						String isGzipStr = header.getValue().toLowerCase();
						if (isGzipStr.contains("gzip")) {
							isGzip = true;
							break;
						}
					}
					InputStream is = entity.getContent();
					BufferedReader reader = null;
					if (isGzip) {
						GZIPInputStream gzipIs = new GZIPInputStream(is);
						reader = new BufferedReader(new InputStreamReader(gzipIs,
								responseCharset));
					} else {
						reader = new BufferedReader(new InputStreamReader(is,
								responseCharset));
					}
					String line;
					while ((line = reader.readLine()) != null) {
						this.postHtml += line;
					}
				} else {
					this.postHtml = EntityUtils.toString(entity);
				}
			} catch (SocketTimeoutException e) {
				this.postException = "SocketTimeoutException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (ConnectTimeoutException e) {
				this.postException = "ConnectTimeoutException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (UnknownHostException e) {
				this.postException = "UnknownHostException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (IOException e) {
				this.postException = "IOException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (URISyntaxException e) {
				this.postException = "URISyntaxException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (Exception e) {
				this.postException = e.getMessage();
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} finally {
				this.post.abort();
				this.post.releaseConnection();
			}
			if ((this.postStatus == 200) && (!StringUtils.isBlank(this.postHtml))) {
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][body=" + body + "][html=" + this.postHtml + "]");
				break;
			} else {
				this.postException = "response null";
				continue;
			}
		}
		if ((this.getStatus == 302) && (StringUtils.isBlank(this.getHtml))) {
			this.getHtml = locationHeader;
		}
		return this.postHtml;
	}
	
	/**
	 * 发送HTTP_POST请求
	 * 
	 * @see 该方法会自动关闭连接,释放资源
	 * @param reqURL
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param files
	 *            上传文件
	 * @param decodeCharset
	 *            解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
	 * @return 远程主机响应正文
	 */
	public String DaMa(String url, Map<String, String> params, Map<String, File> imagefile) {
		for (int retryIndex = 1; retryIndex <= retryCount; retryIndex++) { 
			this.post = new HttpPost();
			this.post.setConfig(config.build());
			this.postHtml = "";
			if (retryIndex >= retryCount) {
				this.post.abort();
				this.post.releaseConnection();
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][body=" + params + "][status=" + this.postStatus + "][exception=" + this.postException + "]");
				break;
			}
			LoggerUtil.HttpInfoLog("[" + url + "][第" + retryIndex + "次抓取尝试]");
			try {
				URI uri = new URI(url);
				this.post.setURI(uri);
				MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue());
				}
				for (Map.Entry<String, File> entry : imagefile.entrySet()) {
					multipartEntityBuilder.addBinaryBody(entry.getKey(), entry.getValue());
				}
				HttpEntity reqentity = multipartEntityBuilder.build();
				this.post.setEntity(reqentity);
				HttpResponse response = this.client.execute(this.post);
				this.postStatus = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				ContentType contentType = ContentType.getOrDefault(entity);
				entity = new BufferedHttpEntity(entity);
				Charset charset = contentType.getCharset() != null?contentType.getCharset():getCharsetFromByte(EntityUtils.toByteArray(entity));
				String responseCharset = "";
				if (charset != null) {
					responseCharset = charset.toString();
				}
				if (StringUtils.isBlank(responseCharset)) {
					responseCharset = "UTF-8";
				}
				if (StringUtils.equals("GB2312", responseCharset)) {
					responseCharset = "GBK";
				}
				this.postHtml = EntityUtils.toString(entity);
			} catch (SocketTimeoutException e) {
				this.postException = "SocketTimeoutException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (ConnectTimeoutException e) {
				this.postException = "ConnectTimeoutException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (UnknownHostException e) {
				this.postException = "UnknownHostException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (IOException e) {
				this.postException = "IOException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (URISyntaxException e) {
				this.postException = "URISyntaxException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (Exception e) {
				this.postException = e.getMessage();
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} finally {
				this.post.abort();
				this.post.releaseConnection();
			}
			if ((this.postStatus == 200) && (!StringUtils.isBlank(this.postHtml))) {
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][body=" + params + "][html=" + this.postHtml + "]");
				break;
			} else {
				this.postException = "response null";
				continue;
			}
			
		}
		return this.postHtml;
	}
	
	/**
	 * 发送HTTP_POST请求
	 * 
	 * @see 该方法会自动关闭连接,释放资源
	 * @param reqURL
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param files
	 *            上传文件
	 * @param decodeCharset
	 *            解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
	 * @return 远程主机响应正文
	 */
	public String ReporyDaMaError(String url) {
		for (int retryIndex = 1; retryIndex <= retryCount; retryIndex++) { 
			this.get = new HttpGet();
			this.get.setConfig(config.build());
			this.getHtml = "";
			if (retryIndex >= retryCount) {
				this.get.abort();
				this.get.releaseConnection();
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][status=" + this.postStatus + "][exception=" + this.postException + "]");
				break;
			}
			LoggerUtil.HttpInfoLog("[" + url + "][第" + retryIndex + "次抓取尝试]");
			try {
				URI uri = new URI(url);
				this.get.setURI(uri);
				HttpResponse response = this.client.execute(this.get);
				this.getStatus = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				ContentType contentType = ContentType.getOrDefault(entity);
				entity = new BufferedHttpEntity(entity);
				Charset charset = contentType.getCharset() != null?contentType.getCharset():getCharsetFromByte(EntityUtils.toByteArray(entity));
				String responseCharset = "";
				if (charset != null) {
					responseCharset = charset.toString();
				}
				if (StringUtils.isBlank(responseCharset)) {
					responseCharset = "UTF-8";
				}
				if (StringUtils.equals("GB2312", responseCharset)) {
					responseCharset = "GBK";
				}
				this.getHtml = EntityUtils.toString(entity);
			} catch (SocketTimeoutException e) {
				this.getException = "SocketTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (ConnectTimeoutException e) {
				this.getException = "ConnectTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (UnknownHostException e) {
				this.postException = "UnknownHostException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (IOException e) {
				this.getException = "IOException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (URISyntaxException e) {
				this.getException = "URISyntaxException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (Exception e) {
				this.getException = e.getMessage();
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} finally {
				this.get.abort();
				this.get.releaseConnection();
			}
			if ((this.getStatus == 200) && (!StringUtils.isBlank(this.getHtml))) {
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][html=" + this.postHtml + "]");
				break;
			} else {
				this.getException = "response null";
				continue;
			}
		}
		return this.getHtml;
	}
	
	public String LoopDaMa(String url) {
		for (int retryIndex = 1; retryIndex <= retryCount; retryIndex++) { 
			this.get = new HttpGet();
			this.get.setConfig(config.build());
			this.getHtml = "";
			if (retryIndex >= retryCount) {
				this.get.abort();
				this.get.releaseConnection();
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][status=" + this.postStatus + "][exception=" + this.postException + "]");
				break;
			}
			LoggerUtil.HttpInfoLog("[" + url + "][第" + retryIndex + "次抓取尝试]");
			try {
				URI uri = new URI(url);
				this.get.setURI(uri);
				HttpResponse response = this.client.execute(this.get);
				this.getStatus = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				ContentType contentType = ContentType.getOrDefault(entity);
				entity = new BufferedHttpEntity(entity);
				Charset charset = contentType.getCharset() != null?contentType.getCharset():getCharsetFromByte(EntityUtils.toByteArray(entity));
				String responseCharset = "";
				if (charset != null) {
					responseCharset = charset.toString();
				}
				if (StringUtils.isBlank(responseCharset)) {
					responseCharset = "UTF-8";
				}
				if (StringUtils.equals("GB2312", responseCharset)) {
					responseCharset = "GBK";
				}
				this.getHtml = EntityUtils.toString(entity);
			} catch (SocketTimeoutException e) {
				this.getException = "SocketTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (ConnectTimeoutException e) {
				this.getException = "ConnectTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (UnknownHostException e) {
				this.postException = "UnknownHostException";
				this.post.abort();
				this.post.releaseConnection();
				continue;
			} catch (IOException e) {
				this.getException = "IOException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (URISyntaxException e) {
				this.getException = "URISyntaxException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (Exception e) {
				this.getException = e.getMessage();
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} finally {
				this.get.abort();
				this.get.releaseConnection();
			}
			if ((this.getStatus == 200) && (!StringUtils.isBlank(this.getHtml))) {
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][html=" + this.postHtml + "]");
				break;
			} else {
				this.getException = "response null";
				continue;
			}
		}
		return this.getHtml;
	}
	
	
	@SuppressWarnings("static-access")
	public byte[][] GetImageByteArr(String url) {
		byte[][] fileData = null;
		byte[] imageDataArr = null;
		byte[] imageTypeArr = null;
		RequestConfig.Builder builderInner = this.config;
		builderInner.setRelativeRedirectsAllowed(false);
		builderInner.setCircularRedirectsAllowed(false);
		builderInner.setRedirectsEnabled(false);
		builderInner.setConnectTimeout(20000);
		builderInner.setSocketTimeout(20000);
		String imageType = "txt";
		for (int retryIndex = 1; retryIndex <= retryCount; retryIndex++) {
			if (this.get == null) {
				this.get = new HttpGet();
			}
			this.get.setConfig(config.build());
			if (retryIndex >= retryCount) {
				this.get.abort();
				this.get.releaseConnection();
				LoggerUtil.HttpDebugLog("[图片数据获取][url=" + url + "][status=" + this.getStatus + "][exception=" + this.getException + "]");
				break;
			}
			if (identidy != 0) {
				ProxyCheckResEntity p = ProxyQueue.getProxy(identidy);
				proxy = new HttpHost(p.host, p.port);
				if (proxy != null) {
					builderInner.setProxy(proxy);
					this.get.setConfig(builderInner.build());
				}
			}
			if (proxy == null) {
				LoggerUtil.HttpInfoLog("[图片数据获取][" + url + "][第" + retryIndex + "次抓取尝试]");
			} else {
				LoggerUtil.HttpInfoLog("[图片数据获取][" + url + "][第" + retryIndex + "次抓取尝试][proxy " + proxy.toHostString() + "]");
			}
			try {
				URI uri = new URI(url);
				this.get.setURI(uri);
				HttpResponse response = this.client.execute(this.get);
				Header header = response.getFirstHeader("Content-Type");
				if (header != null) {
					String value = header.getValue();
					if ((value.contains("image")) || (value.contains("Image"))) {
						if (value.toLowerCase().contains("jpeg")) {
							imageType = "jpg";
						} else if (value.toLowerCase().contains("jpg")) {
							imageType = "jpg";
						} else if (value.toLowerCase().contains("png")) {
							imageType = "png";
						} else if (value.toLowerCase().contains("gif")) {
							imageType = "gif";
						}
					}
				}
				imageTypeArr = imageType.getBytes();
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				IOUtils.copy(is, outStream);
				Thread.currentThread().sleep(2000);
				is.close();
				imageDataArr = outStream.toByteArray();
				fileData = new byte[][] { imageDataArr, imageTypeArr };
				outStream.close();
			} catch (SocketTimeoutException e) {
				this.getException = "SocketTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (ConnectTimeoutException e) {
				this.getException = "ConnectTimeoutException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (UnknownHostException e) {
				this.getException = "UnknownHostException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (IOException e) {
				this.getException = "IOException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (URISyntaxException e) {
				this.getException = "URISyntaxException";
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} catch (Exception e) {
				this.getException = e.getMessage();
				e.printStackTrace();
				this.get.abort();
				this.get.releaseConnection();
				continue;
			} finally {
				this.get.abort();
				this.get.releaseConnection();
			}
			if (!StringUtils.equals("txt", imageType) && imageDataArr != null) {
				LoggerUtil.HttpDebugLog("[数据获取][url=" + url + "][html=" + this.getHtml + "]");
				break;
			} else {
				this.getException = "response_null";
				continue;
			}
		}
		return fileData;
	}

	public void setProxy(String ip, int port) {
		HttpHost proxy = new HttpHost(ip, port);
		this.config.setProxy(proxy);
		this.client = this.clientBuilder.setDefaultRequestConfig(this.config.build()).build();
	}
	
	public void SetTimeOut(int timeout) {
		this.config.setConnectTimeout(timeout);
		this.config.setSocketTimeout(timeout);
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");  
			X509TrustManager tm = new X509TrustManager() {  
				public void checkClientTrusted(X509Certificate[] xcs,  
						String string) throws CertificateException {  
				}  
				public void checkServerTrusted(X509Certificate[] xcs,  
						String string) throws CertificateException {  
				}  
				public X509Certificate[] getAcceptedIssuers() {  
					return null;  
				}  
			};  
			ctx.init(null, new TrustManager[] { tm }, null);
			LayeredConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx);
			this.client = this.clientBuilder.setSSLSocketFactory(ssf).setDefaultRequestConfig(this.config.build()).setDefaultCookieStore(cookieStore).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
	}

	public static Charset getCharsetFromByte(byte[] byteArray) {
		String content = new String(byteArray);
		Charset charset = null;
		Pattern pattern = Pattern
				.compile("<[mM][eE][tT][aA][^>]*([cC][Hh][Aa][Rr][Ss][Ee][Tt][\\s]*=[\\s\\\"']*)([\\w\\d-_]*)[^>]*>");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
			charset = Charset.forName(matcher.group(2));
		else {
			charset = getCharsetFromBOM(byteArray);
		}

		return charset;
	}

	private static Charset getCharsetFromBOM(byte[] byteArray) {
		Charset charset = null;
		if ((byteArray == null) || (byteArray.length < 2)) {
			return charset;
		}
		int p = (byteArray[0] & 0xFF) << 8 | byteArray[1] & 0xFF;
		switch (p) {
		case 61371:
			charset = Charset.forName("UTF-8");
			break;
		case 65534:
			charset = Charset.forName("Unicode");
			break;
		case 65279:
			charset = Charset.forName("UTF-16BE");
			break;
		default:
			charset = Charset.forName("GBK");
		}
		return charset;
	}
	
	public static void main(String[] args) {}
	
}