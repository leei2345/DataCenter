package com.jinba.spider.core;
import javax.net.ssl.SSLContext;  
  
import javax.net.ssl.TrustManager;  
import javax.net.ssl.X509TrustManager;  

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;  
import java.security.cert.X509Certificate;  

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;  
import org.apache.http.client.HttpClient;  
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;  
  
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

  
public class HttpClientTest {  

    public static void main(String args[]) {  
  
        try {  
        	RequestConfig.Builder config = RequestConfig.custom();
        	HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        	BasicCookieStore cookieStore = new BasicCookieStore();
            HttpClient httpclient = null;
//        	config.setAuthenticationEnabled(true);
    		config.setConnectTimeout(30000);
    		config.setSocketTimeout(10000);
    		clientBuilder = HttpClientBuilder.create();
    		clientBuilder.setMaxConnTotal(100);
    		clientBuilder.setMaxConnPerRoute(500);
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
    		httpclient = clientBuilder.setSSLSocketFactory(ssf).setDefaultRequestConfig(config.build()).setDefaultCookieStore(cookieStore).build();
  
            HttpGet httpget = new HttpGet("https://www.sogou.com");  
            HttpHost proxy = new HttpHost("1.179.189.217", 8080);
            config.setProxy(proxy);
            httpget.setConfig(config.build()); 
			HttpResponse res  = httpclient.execute(httpget);  
            InputStream in = res.getEntity().getContent();
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            char[] buffer = new char[1024];
            while ((reader.read(buffer)) != -1) {
//            	System.out.println(new String(buffer));
            }
            HttpMethod m = new HttpMethod(cookieStore);
            System.out.println(m.GetHtml("http://weixin.sogou.com/weixin?type=2&query=%E4%B8%9C%E5%9F%8E%E5%8C%BA&ie=utf8&w=&sut=&sst0=&lkt=&page=1", HttpResponseConfig.ResponseAsString));
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
    }  
}  