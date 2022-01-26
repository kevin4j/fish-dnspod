package com.kevin.fish.utils;

import com.kevin.fish.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kevin
 */
public class HttpClientUtils {

	private final static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

	private static final String DEFAULT_CHARSET = "GBK";

	private static final int maxConnectCnt = 400;

	private static final int maxPreRouteConnectCnt = 100;

	private static RequestConfig defaultRequestConfig;

	private static PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

	private static final int TIMEOUT = 30000;

	private static final ThreadLocal<String> responseCharsetThreadLocal = new ThreadLocal<String>();

	// 请求配置池
	private static Map<Integer, RequestConfig> requestConfigMap = new HashMap<>();
	
	static {

		defaultRequestConfig = getRequestConfig(TIMEOUT);
		
		// 设置连接池最大连接数
		manager.setMaxTotal(maxConnectCnt);

		// 设置每个路由最大连接数
		// 这个参数的默认值为2，如果不设置这个参数值默认情况下对于同一个目标机器的最大并发连接只有2个
		manager.setDefaultMaxPerRoute(maxPreRouteConnectCnt);
		
	}
	
	private static RequestConfig getRequestConfig(Integer socketTimeout) {
		RequestConfig config=requestConfigMap.get(socketTimeout);
		
		if(config==null) {
			config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(TIMEOUT).build();
			requestConfigMap.put(socketTimeout, config);
		}
		
		return config;
	}

	// 异常自动恢复处理, 使用HttpRequestRetryHandler接口实现请求的异常恢复
	private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
		// 自定义的恢复策略
		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			// 设置恢复策略，在发生异常时候将自动重试3次
			if (executionCount >= 3) {
				return false;
			}

			if (exception instanceof NoHttpResponseException) {
				return true;
			}

			if (exception instanceof SSLHandshakeException) {
				return false;
			}

			HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
			boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
			if (!idempotent) {
				return true;
			}
			return false;
		}
	};

	// 使用ResponseHandler接口处理响应，HttpClient使用ResponseHandler会自动管理连接的释放，解决了对连接的释放管理
	private static ResponseHandler<byte[]> responseHandler = new ResponseHandler<byte[]>() {
		// 自定义响应处理
		@Override
		public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			HttpEntity entity = response.getEntity();
			if (entity != null) {

				String charset = ContentType.get(entity) == null || ContentType.get(entity).getCharset() == null ? DEFAULT_CHARSET : ContentType.get(entity).getCharset().name();

				responseCharsetThreadLocal.set(charset);
				// 如果response启用了gzip编码，则使用gzip先解码
				if (String.valueOf(response.getFirstHeader("Content-Encoding")).toLowerCase().indexOf("gzip") > -1) {
					entity = new GzipDecompressingEntity(entity);
				}

				return EntityUtils.toByteArray(entity);
			} else {
				return null;
			}
		}
	};
	
	// 启用gzip编码
	public static HttpRequestInterceptor gzipInterceptor=new HttpRequestInterceptor() {
		@Override
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
			if (!request.containsHeader("Accept-Encoding")) {
				request.addHeader("Accept-Encoding", "gzip");
			}
		}
	};

	/**
	 * Get方式提交,URL中不包含查询参数, 格式：http://www.g.cn
	 * 
	 * @param url
	 *            提交地址
	 * @param params
	 *            查询参数集, 键/值对
	 * @param requestCharset
	 *            参数提交编码集
	 * @return 响应消息
	 */
	public static byte[] getContent(String url, Map<String, String> params, String requestCharset, Integer timeout) {
		if (url == null || StringUtils.isEmpty(url)) {
			return null;
		}
		
		long startTime = System.currentTimeMillis();
		List<NameValuePair> paramsList = getParamsList(params);
		if (paramsList != null && paramsList.size() > 0) {
			requestCharset = (requestCharset == null ? DEFAULT_CHARSET : requestCharset);
			String formatParams = URLEncodedUtils.format(paramsList, requestCharset);
			url = (url.indexOf("?")) < 0 ? (url + "?" + formatParams) : (url.substring(0, url.indexOf("?") + 1) + formatParams);
		}
		CloseableHttpClient httpclient = getHttpClient(requestCharset, url.indexOf("https") == 0);
		
		HttpGet hg = new HttpGet(url);
		hg.setConfig(timeout == null ? defaultRequestConfig : getRequestConfig(timeout));
		
		// 发送请求，得到响应
		byte[] responseByte = null;
		try {
			logger.debug("准备请求花费时长："+(System.currentTimeMillis() - startTime));
			responseByte = httpclient.execute(hg, responseHandler);
		} catch (ClientProtocolException e) {
			throw new RuntimeException("客户端连接协议错误", e);
		} catch (IOException e) {
			throw new RuntimeException("IO操作异常,查看是否超过请求设定时间:" + hg.getConfig().getSocketTimeout() + "毫秒!", e);
		} finally {
			abortConnection(hg, httpclient);
		}
		return responseByte;
	}

	public static byte[] getContent(String url) {
		return getContent(url, null);
	}

	public static byte[] getContent(String url, Map<String, String> params) {
		return getContent(url, params, null);
	}
	
	public static byte[] getContent(String url, Map<String, String> params, String requestCharset) {
		return getContent(url, params, requestCharset, null);
	}

	/**
	 * Get方式提交,URL中不包含查询参数, 格式：http://www.g.cn
	 * 
	 * @param url
	 *            提交地址
	 * @param params
	 *            查询参数集, 键/值对
	 * @param requestCharset
	 *            参数提交编码集
	 * @return 响应消息
	 */
	public static String get(String url, Map<String, String> params, String requestCharset, String responseCharset, Integer timeout) {

		// 发送请求，得到响应
		String responseStr = null;
		try {
			byte[] responseByte = getContent(url, params, requestCharset, timeout);
			responseStr = new String(responseByte, StringUtils.isBlank(responseCharset) ? responseCharsetThreadLocal.get()
					: responseCharset);
		} catch (IOException e) {
			throw new RuntimeException("IO操作异常,查看是否超过请求设定时间:" + TIMEOUT + "毫秒!", e);
		}

		return responseStr;
	}

	public static String get(String url) {
		return get(url, null);
	}

	public static String get(String url, Map<String, String> params) {
		return get(url, params, null);
	}

	public static String get(String url, Map<String, String> params, String requestCharset) {
		return get(url, params, requestCharset, null);
	}
	
	public static String get(String url, Map<String, String> params, String requestCharset, String responseCharset) {
		return get(url, params, requestCharset, responseCharset, null);
	}

	public static String post(String url) {
		return post(url, null);
	}

	public static String post(String url, Map<String, String> params) {
		return post(url, params, null);
	}
	
	public static String post(String url, Map<String, String> params, String requestCharset) {
		return post(url, params, requestCharset, null);
	}
	
	public static String post(String url, Map<String, String> params, String requestCharset, String responseCharset) {
		return post(url, params, requestCharset, responseCharset, null);
	}
	
	public static String post(String url, Map<String, String> params, String requestCharset, String responseCharset, Integer timeout, Header... headers) {
		return postCommon(url, params, requestCharset, responseCharset, headers);
	}

	public static String post(String url, final String body, final String requestCharset, String responseCharset, Header... headers) {
		return postCommon(url, body, requestCharset, responseCharset, headers);
	}
	
	public static String postCommon(String url, final Object value, final String requestCharset, String responseCharset, Header... headers) {
		// 创建HttpClient实例
		CloseableHttpClient httpclient = getHttpClient(requestCharset, url.indexOf("https") == 0);
		return postCommon(httpclient, url, value, requestCharset, responseCharset, headers);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String postCommon(CloseableHttpClient httpclient, String url, final Object value, final String requestCharset, String responseCharset, Header... headers) {
		if (url == null || StringUtils.isEmpty(url)) {
			return null;
		}
		
		HttpEntity entity = null;
		try {
			if(value instanceof String) {
				entity = new StringEntity((String)value, requestCharset);
			}else if(value instanceof Map) {
				if (requestCharset == null || StringUtils.isEmpty(requestCharset)) {
					entity = new UrlEncodedFormEntity(getParamsList((Map)value));
				} else {
					entity = new UrlEncodedFormEntity(getParamsList((Map)value), requestCharset);
				}
			}
		} catch (UnsupportedCharsetException e1) {
			throw new RuntimeException("不支持的字符集", e1);
		} catch (UnsupportedEncodingException ee) {
			throw new RuntimeException("不支持的编码集", ee);
		}
		HttpPost hp = new HttpPost(url);
		hp.setEntity(entity);
		if (headers != null && headers.length > 0) {
			for (Header header : headers) {
				if (hp.getFirstHeader(header.getName()) != null) {
					hp.setHeader(header);
				} else {
					hp.addHeader(header);
				}
			}
		}

		// 发送请求，得到响应
		String responseStr = null;
		try {
			byte[] responseByte = httpclient.execute(hp, responseHandler);
			responseStr = new String(responseByte, StringUtils.isBlank(responseCharset) ? responseCharsetThreadLocal.get()
					: responseCharset);
		} catch (ClientProtocolException e) {
			throw new RuntimeException("客户端连接协议错误", e);
		} catch (IOException e) {
			throw new RuntimeException("IO操作异常", e);
		} finally {
			abortConnection(hp, httpclient);
		}
		return responseStr;
	}

	/**
	 * Post方式提交,忽略URL中包含的参数,解决SSL双向数字证书认证
	 * 
	 * @param url
	 *            提交地址
	 * @param params
	 *            提交参数集, 键/值对
	 * @param requestCharset
	 *            参数编码集
	 * @param keystoreUrl
	 *            密钥存储库路径
	 * @param keystorePassword
	 *            密钥存储库访问密码
	 * @param truststoreUrl
	 *            信任存储库绝路径
	 * @param truststorePassword
	 *            信任存储库访问密码, 可为null
	 * @return 响应消息
	 */
	public static String post(String url, Map<String, String> params, String requestCharset, final URL keystoreUrl, final String keystorePassword,
			final URL truststoreUrl, final String truststorePassword, String responseCharset) {
		CloseableHttpClient httpClient = getHttpClient(requestCharset, keystoreUrl, keystorePassword, truststoreUrl, truststorePassword,
				url.indexOf("https") == 0);
		return postCommon(httpClient, url, params, requestCharset, responseCharset);
	}

	/**
	 * Post方式提交,忽略URL中包含的参数,解决SSL双向数字证书认证
	 * 
	 * @param url
	 *            提交地址
	 * @param params
	 *            提交参数集, 键/值对
	 * @param charset
	 *            参数编码集
	 * @param responseCharset
	 *            响应编码集
	 */
	public static String upload(String url, Map<String, Object> params, String charset, String responseCharset) {

		if (url == null || StringUtils.isEmpty(url)) {
			return null;
		}

		CloseableHttpClient httpclient = getHttpClient(charset, url.indexOf("https") == 0);

		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		if (params != null && params.size() > 0) {
			for (Map.Entry<String, Object> map : params.entrySet()) {

				if (map.getValue() instanceof File) {
					multipartEntityBuilder.addPart(map.getKey(), new FileBody((File) map.getValue()));
				} else if (map.getValue() instanceof String) {
					multipartEntityBuilder.addPart(map.getKey(), new StringBody((String) map.getValue(), ContentType.TEXT_PLAIN));
				} else if (map.getValue() instanceof byte[]) {
					multipartEntityBuilder.addPart(map.getKey(), new ByteArrayBody((byte[]) map.getValue(), map.getKey()));
				}
			}
		}

		HttpPost hp = null;
		String responseStr = null;
		try {
			hp = new HttpPost(url);
			hp.setEntity(multipartEntityBuilder.build());
			byte[] responseByte = httpclient.execute(hp, responseHandler);
			responseStr = new String(responseByte, StringUtils.isBlank(responseCharset) ? responseCharsetThreadLocal.get()
					: responseCharset);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("keystore文件不存在", e);
		} catch (IOException e) {
			throw new RuntimeException("I/O操作失败或中断 ", e);
		} finally {
			abortConnection(hp, httpclient);
		}
		return responseStr;
	}

	private static CloseableHttpClient getHttpClient(final String charset, boolean isHttps) {
		return getHttpClient(charset, null, null, null, null, isHttps);
	}

	private static CloseableHttpClient getHttpClient(final String charset, final URL keystoreUrl, final String keystorePassword,
			final URL truststoreUrl, final String truststorePassword, final boolean isHttps) {

		// 使用连接池创建连接
		CloseableHttpClient httpClient = null;
		SSLConnectionSocketFactory socketFactory=null;
		HttpClientBuilder httpClientBuilder=HttpClients.custom().setConnectionManager(manager)
				.setConnectionManagerShared(true)
				.setRetryHandler(requestRetryHandler).addInterceptorFirst(gzipInterceptor);

		// 设置httpclient证书
		if (keystoreUrl != null) {
			KeyStore keyStore;
			KeyStore trustStore;
			try {
				keyStore = createKeyStore(keystoreUrl, keystorePassword);
				trustStore = createKeyStore(truststoreUrl, keystorePassword);
				SSLContext sslContext = SSLContexts.custom()
						.loadKeyMaterial(keyStore, keystorePassword.toCharArray())
						.loadKeyMaterial(trustStore, keystorePassword.toCharArray()).build();
				socketFactory = new SSLConnectionSocketFactory(sslContext);
			} catch (KeyStoreException e) {
				throw new ServiceException("keytore解析异常", e);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("指定的加密算法不可用", e);
			} catch (CertificateException e) {
				throw new RuntimeException("信任证书过期或解析异常", e);
			} catch (IOException e) {
				throw new RuntimeException("I/O操作失败或中断 ", e);
			} catch (KeyManagementException e) {
				throw new RuntimeException("处理密钥管理的操作异常", e);
			} catch (UnrecoverableKeyException e) {
				throw new RuntimeException("keystore中的密钥无法恢复异常", e);
			}
		} else if (isHttps) {

			// 如果是https访问
			SSLContext ctx = null;
			try {
				ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[] { new X509TrustManager() {
					
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						
					}
					
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						
					}
				}}, null);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}

			socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
		}
		
		httpClientBuilder.setSSLSocketFactory(socketFactory);
		httpClient = httpClientBuilder.build();

		// 打log显示连接池信息
		{
			PoolStats poolStats = manager.getTotalStats();

			logger.debug("当前httpclient连接池信息-最大连接数:{},正在执行数:{},空闲连接数:{},阻塞连接数:{}", new Object[] { poolStats.getMax(), poolStats.getLeased(),
					poolStats.getAvailable(), poolStats.getPending() });
		}
		
		return httpClient;
	}

	/**
	 * 释放HttpClient连接
	 * 
	 * @param hrb 请求对象
	 * @param httpClient client对象
	 */
	private static void abortConnection(final HttpRequestBase hrb, final CloseableHttpClient httpClient) {

		if (hrb != null) {
			hrb.abort();
		}

		try {
			httpClient.close();
		}catch (Exception e) {
			logger.error("关闭httpClient出现异常", e);
		}
	}

	/**
	 * 从给定的路径中加载此 KeyStore
	 * 
	 * @param url
	 *            keystore URL路径
	 * @param password
	 *            keystore访问密钥
	 * @return keystore 对象
	 */
	private static KeyStore createKeyStore(final URL url, final String password) throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException {
		if (url == null) {
			throw new IllegalArgumentException("Keystore url may not be null");
		}
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream is = null;
		try {
			is = url.openStream();
			keystore.load(is, password != null ? password.toCharArray() : null);
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
		}
		return keystore;
	}

	/**
	 * 将传入的键/值对参数转换为NameValuePair参数集
	 * 
	 * @param paramsMap
	 *            参数集, 键/值对
	 * @return NameValuePair参数集
	 */
	private static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (paramsMap != null && paramsMap.size() > 0) {
			for (Map.Entry<String, String> map : paramsMap.entrySet()) {
				params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
			}
		}

		return params;
	}

	public static void main(String[] args) throws Exception {


	}
}
