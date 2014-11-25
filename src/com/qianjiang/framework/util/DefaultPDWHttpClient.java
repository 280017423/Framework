package com.qianjiang.framework.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Base64;

import com.qianjiang.framework.R;
import com.qianjiang.framework.app.JsonResult;
import com.qianjiang.framework.app.QJApplicationBase;
import com.qianjiang.framework.encrypt.CMyEncrypt;
import com.qianjiang.framework.model.HttpCacheConfigModel;

/**
 * 网络访问辅助类
 * 
 * @author wang.xy
 * 
 */
/**
 * @author alina
 * @version 
 *          2012-08-02，zeng.ww，增加httpGet,getString,parseUrl,decodeUrl,encodeUrl等方法
 * <BR>
 *          2012-10-31，tan.xx，增加doHttpExecutefilter相关请求处理，get，
 *          post新增带参数requestType处理等方法<BR>
 *          2013-03-21，tan.xx，修改相同请求抛弃等处理，增加线程ID标记，增加接口只返回字符串的封装支持<br>
 *          2013-04-18，tan.xx，HTTP Header增加移动终端信息方法<br>
 *          2013-05-11，tan.xx，增加本地数据缓存机制处理，添加cacheFileData等相关方法<br>
 *          2013-07-23，tan.xx，优化buildContent方法
 */
public class DefaultPDWHttpClient implements IPDWHttpClient {

	private static final int EIGHTY = 80;
	private static final int TEN = 10;
	private static final int PRINT_MAX_LENGTH = 1024;
	private static final String TAG = "DefaultPDWHttpClient";
	private static final String CACHE_FILE_TAG = "DefaultPDWHttpClient_cache_file";
	private static final String CACHE_TAG = "DefaultPDWHttpClient_cache";
	private static final int TIMEOUT_SHORT_IN_MS = 5000;
	private static final int TIMEOUT_MIDDLE_IN_MS = 10000;
	private static final int UPLOAD_FILE_TIMEOUT_MIDDLE_IN_MS = 30000;
	private static final int BUFFERSIZE = 4096;
	private static final String CHARSET = "UTF-8";
	private static final String INTERROGATION = "?";
	private static final String AJAX_APPEND_HEADER = "ajax";
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	private static final String SPLIT_FLAG_AND = "&";
	private static final String SPLIT_FLAG_EQUAL = "=";

	// 本地文件保存目录
	private static final String CACHED_DIR = "http_cache_dir";
	// 本地文件保存后缀名
	private static final String CACHE_FILE_EXT = ".bin";
	// 标记线程ID
	private static long threadId = 100;

	// private static final Object mSync = new Object();
	// 缓存正在处理的请求 此处缓存数据不大所以没有采用map
	private static List<RequestModel> REQUEST_LIST = Collections.synchronizedList(new ArrayList<RequestModel>());

	public static int MAX_CACHE_REQUEST_COUNT = 6;

	/**
	 * 登录成功后的Cookie信息
	 */
	private static CookieStore COOKIE_STORE;

	private ReadWriteLock rwLock;

	private Lock readLock;

	private Lock writeLock;

	public DefaultPDWHttpClient() {
		rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}

	/**
	 * 
	 * @param cookieStore
	 *            存储coockie
	 */
	@Override
	public void setCookieStore(CookieStore cookieStore) {
		COOKIE_STORE = cookieStore;
	}

	/**
	 * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
	 * 
	 * @param url
	 *            url地址
	 * @param httpParams
	 *            http参数
	 * @param postParams
	 *            参数
	 * @return json数据
	 * @throws NetworkException
	 *             网络异常
	 * @throws MessageException
	 *             业务异常
	 */
	@Override
	public JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams) throws NetworkException,
			MessageException {
		return post(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, httpParams, postParams);
	}

	/**
	 * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
	 * 
	 * @param url
	 *            url地址
	 * @param requestType
	 *            请求处理类型 详见HttpClientUtil类"http请求处理类型" 常量
	 * @param httpParams
	 *            http参数
	 * @param postParams
	 *            参数
	 * @return json数据
	 * @throws NetworkException
	 *             网络异常
	 * @throws MessageException
	 *             业务异常
	 */
	@Override
	public JsonResult post(String url, int requestType, HttpParams httpParams, List<NameValuePair> postParams)
			throws NetworkException, MessageException {
		return getJosnResultConnect(url, requestType, postParams, false, false);
	}

	/**
	 * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
	 * 
	 * @param url
	 *            url地址
	 * @param httpParams
	 *            http参数
	 * @param postParams
	 *            参数
	 * @param file
	 *            参数
	 * @return json数据
	 * @throws NetworkException
	 *             网络异常
	 * @throws MessageException
	 *             业务异常
	 * @throws UnsupportedEncodingException
	 *             编码不支持异常
	 */
	@Override
	public JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams, List<File> fileList)
			throws NetworkException, MessageException, UnsupportedEncodingException {
		EvtLog.d(TAG, "post begin, " + url);

		if (!NetUtil.isNetworkAvailable()) {
			HttpClientUtil.LAST_REQUEST_IS_OK = false;
			throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
		}

		JsonResult jsonResult = null;

		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader(AJAX_APPEND_HEADER, "true");
		httpPost.setHeader("Accept-Encoding", "gzip");
		httpPost.addHeader(QJApplicationBase.KEY_CLIENT_INFO, QJApplicationBase.getInstance().getClientInfo());
		if (httpParams != null) {
			httpPost.setParams(httpParams);
		}

		MultipartEntity entity = new MultipartEntity();
		if (postParams != null && !postParams.isEmpty()) {
			if (EvtLog.IS_DEBUG_LOGGABLE) {
				printPostData(postParams);
			}
			for (NameValuePair entry : postParams) {
				// 修改编码为utf-8编码，解决服务器乱码的问题
				entity.addPart(entry.getName(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
				EvtLog.d(TAG, "Name:" + entry.getName() + " Value:" + entry.getValue());
			}
		}
		for (int i = 0; i < fileList.size(); i++) {
			File tempFile = fileList.get(i);
			entity.addPart(tempFile.getPath(), new FileBody(tempFile));
		}
		httpPost.setEntity(entity);
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_SHORT_IN_MS);
		HttpConnectionParams.setSoTimeout(httpParameters, UPLOAD_FILE_TIMEOUT_MIDDLE_IN_MS);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		setHttpClientInterceptor(client);
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		if (COOKIE_STORE != null) {
			client.setCookieStore(COOKIE_STORE);
			printCookies(COOKIE_STORE);
		}
		EvtLog.d(TAG, "client.execute begin ");
		try {
			HttpResponse httpResponse = client.execute(httpPost);

			int status = httpResponse.getStatusLine().getStatusCode();
			EvtLog.d(TAG, "client.execute end, status: " + status);
			String returnString = EntityUtils.toString(httpResponse.getEntity());
			if (status == HttpStatus.SC_OK) {
				try {
					jsonResult = new JsonResult(returnString);
					if (EvtLog.IS_DEBUG_LOGGABLE) {
						printResponse(returnString);
					}
				} catch (JSONException e) {
					EvtLog.w(TAG, e);
				}
				if (client.getCookieStore().getCookies() != null && client.getCookieStore().getCookies().size() > 0) {
					COOKIE_STORE = client.getCookieStore();
				}
			} else {
				EvtLog.e(TAG, "server response: " + returnString + ";  status:" + status, false);
				// BottomTab.toast(PackageUtil.getString(R.string.msg_operate_fail_try_again));
			}
		} catch (Exception e) {
			if ((e instanceof IOException) || (e instanceof ClientProtocolException)) {
				EvtLog.e(TAG, "NetworkException", false);
				HttpClientUtil.LAST_REQUEST_IS_OK = false;
				throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
			} else {
				// 修改网络问题引起的缺陷为warning级别
				EvtLog.w(TAG, e);
			}
		} finally {
			EvtLog.d(TAG, "post end, " + url);
		}
		if (jsonResult != null) {
			HttpClientUtil.LAST_REQUEST_IS_OK = true;
		}
		return jsonResult;
	}

	/**
	 * 设置请求和响应的intercepter以通知服务器
	 * 
	 * @param client
	 *            客户端请求的DefaultHttpClient
	 */
	private void setHttpClientInterceptor(DefaultHttpClient client) {
		// Adding a new request intercepter in order to be able to notify the
		// server
		// that we support compressed http data.
		client.addRequestInterceptor(new HttpRequestInterceptor() {
			@Override
			public void process(HttpRequest request, HttpContext context) {
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
					EvtLog.d(TAG, "Use gzip to notify server");
				}
			}
		});
		// Adding a new response intercepter in order to decompress compressed
		// http data
		client.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext context) {
				final HttpEntity entity = response.getEntity();
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}
				}
			}
		});
	}

	/**
	 * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
	 * 
	 * @param url
	 *            url地址
	 * @param getParams
	 *            附加在Url后面的参数
	 * @return json数据
	 * @throws MessageException
	 *             异常信息
	 * @throws NetworkException
	 *             异常信息
	 */
	@Override
	public JsonResult get(String url, List<NameValuePair> getParams) throws NetworkException, MessageException {
		return get(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, null, getParams);
	}

	@Override
	public JsonResult getByString(String url, List<NameValuePair> getParams) throws NetworkException {
		return getJosnResultConnect(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, getParams, true, true);
	}

	/**
	 * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
	 * 
	 * @param url
	 *            url地址
	 * @param requestType
	 *            请求处理类型 详见HttpClientUtil类"http请求处理类型" 常量
	 * @param getParams
	 *            附加在Url后面的参数
	 * @return json数据
	 * @throws MessageException
	 *             异常信息
	 * @throws NetworkException
	 *             异常信息
	 */
	@Override
	public JsonResult get(String url, int requestType, List<NameValuePair> getParams) throws NetworkException,
			MessageException {
		return get(url, requestType, null, getParams);
	}

	/**
	 * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
	 * 
	 * @param url
	 *            url地址
	 * @param httpParams
	 *            http参数
	 * @param getParams
	 *            附加在Url后面的参数
	 * @return json数据
	 * @throws NetworkException
	 *             异常信息
	 * @throws MessageException
	 *             异常信息
	 * 
	 */
	@Override
	public JsonResult get(String url, HttpParams httpParams, List<NameValuePair> getParams) throws NetworkException,
			MessageException {

		return get(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, httpParams, getParams);
	}

	// public Cookie[] getCookieByStr(String str) {
	// String[] cookiestrs = str.split(";");
	// Cookie[] cookies = new Cookie[cookies.length];
	// for (int i = 0; i < cookies.length; i++) {
	// String[] onecookie = cookiestrs[i].split("=");
	// cookies[i] = new Cookie(onecookie(0), onecookie(1));
	// }
	// return cookies;
	// }

	/**
	 * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
	 * 
	 * @param url
	 *            url地址
	 * @param requestType
	 *            请求处理类型 详见HttpClientUtil类"http请求处理类型" 常量
	 * @param httpParams
	 *            http参数
	 * @param getParams
	 *            附加在Url后面的参数
	 * @return json数据
	 * @throws NetworkException
	 *             异常信息
	 * @throws MessageException
	 *             异常信息
	 */
	@Override
	public JsonResult get(String url, int requestType, HttpParams httpParams, List<NameValuePair> getParams)
			throws NetworkException, MessageException {
		return getJosnResultConnect(url, requestType, getParams, true, false);
	}

	/**
	 * getJosnResultConnect 获取连接
	 * 
	 * @param url
	 *            地址
	 * @param requestType
	 *            请求处理类型 详见HttpClientUtil类"http请求处理类型" 常量
	 * @param params
	 *            参数
	 * @param isGet
	 *            是否是get请求
	 * @param isForBackString
	 *            接口是否是直接返回String
	 * @return JsonResult json数据
	 * @throws NetworkException
	 *             网络异常
	 */
	private JsonResult getJosnResultConnect(final String url, final int requestType, final List<NameValuePair> params,
			final boolean isGet, final boolean isForBackString) throws NetworkException {
		JsonResult jsonResult = null;
		HttpCacheConfigModel httpCacheConfigModel = isNeedDataCache(url);
		if (!NetUtil.isNetworkAvailable()) {
			if (httpCacheConfigModel != null) {
				// 使用文件缓存数据
				try {
					jsonResult = getJsonResultByFile(url, params, isForBackString);
				} catch (Exception e) {
					HttpClientUtil.LAST_REQUEST_IS_OK = false;
					throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
				}
				return jsonResult;
			}
			HttpClientUtil.LAST_REQUEST_IS_OK = false;
			throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
		}
		if (httpCacheConfigModel != null) {
			if (!httpCacheConfigModel.IsNetworkDataPriority) {
				// 优先返回缓存数据
				try {
					// 使用文件缓存数据
					jsonResult = getJsonResultByFile(url, params, isForBackString);
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								// 异步取网络数据
								getJsonResultByNetwork(url, requestType, params, isGet, isForBackString, true);
							} catch (NetworkException e) {
								// 网络异常，什么都不做
							}
						}
					}).start();
					return jsonResult;
				} catch (Exception e) {
					// 读取本地异常，继续获取网络数据
				}
			}
		}
		jsonResult = getJsonResultByNetwork(url, requestType, params, isGet, isForBackString, false);
		return jsonResult;
	}

	/**
	 * 从网络获取数据
	 * 
	 * @param url
	 * @param requestType
	 * @param params
	 * @param isGet
	 * @param isForBackString
	 * @param isBackRun
	 *            是否是后台运行
	 * @param @throws NetworkException
	 * @return JsonResult
	 */
	public JsonResult getJsonResultByNetwork(String url, int requestType, List<NameValuePair> params, boolean isGet,
			boolean isForBackString, boolean isBackRun) throws NetworkException {
		String buildUrl = url;
		JsonResult jsonResult = null;
		if (isGet) {
			buildUrl = buildUrl(url, params);
		}
		InputStream is = null;
		HttpURLConnection hrc = null;
		String cookieVal = null;
		threadId++;
		long currentId = threadId;
		try {
			URL comUrl = new URL(buildUrl);
			hrc = (HttpURLConnection) comUrl.openConnection();
			hrc.setReadTimeout(TIMEOUT_MIDDLE_IN_MS);
			hrc.setConnectTimeout(TIMEOUT_MIDDLE_IN_MS);
			if (isGet) {
				hrc.setRequestMethod("GET");
			} else {
				hrc.setDoOutput(true);
				hrc.setRequestMethod("POST");
			}
			hrc.setDoInput(true);
			hrc.setUseCaches(false);
			hrc.setRequestProperty("Charset", CHARSET); // 设置编码
			hrc.setRequestProperty("Connection", "Keep-Alive");
			hrc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			addHeader(hrc);
			if (COOKIE_STORE != null) {
				hrc.addRequestProperty("Cookie", cookiesToString(COOKIE_STORE));
				printCookies(COOKIE_STORE);
			}
			EvtLog.d(TAG, "get begin, url:" + buildUrl);
			if (EvtLog.IS_DEBUG_LOGGABLE && !isGet) {
				printPostData(params);
			}
			doHttpExecutefilter(url, requestType, params, null, hrc, currentId);
			hrc.connect();
			if (!isGet) {
				DataOutputStream os = new DataOutputStream(hrc.getOutputStream());
				String content = buildContent(params);
				EvtLog.d(TAG, "post 参数:" + content);
				byte[] sendData = content.getBytes();
				if (sendData != null && sendData.length > 0) {
					int len = PRINT_MAX_LENGTH;
					int offset = 0;
					while (sendData.length - offset > len) {
						os.write(sendData, offset, len);
						offset += len;
					}
					os.write(sendData, offset, sendData.length - offset);
					os.flush();
					os.close();
				}
			}
			int respCode = hrc.getResponseCode();
			if (respCode == HttpURLConnection.HTTP_OK) {
				is = hrc.getInputStream();
				if (null != is) {
					StringBuffer result = new StringBuffer(1024);
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					String line = "";
					while ((line = reader.readLine()) != null) {
						result.append(line);
					}
					reader.close();
					if (EvtLog.IS_DEBUG_LOGGABLE) {
						printResponse(result.toString());
					}
					cookieVal = hrc.getHeaderField("set-cookie");
					EvtLog.d(TAG, "cookieVal:" + cookieVal);
					saveCookie(url, cookieVal);
					if (!isRelease(currentId)) {
						String returnStr = result.toString();
						// 保存文件
						cacheFileData(url, params, returnStr, isForBackString);
						if (isForBackString) {
							String jsonStr;
							jsonStr = String.format(JsonResult.SUCCES_JSON_STRING,
									Base64.encode(returnStr.getBytes(), Base64.DEFAULT));
							EvtLog.d(CACHE_TAG, "------------------正常返回值  threadId : " + currentId
									+ " 本地拼接数据 jsonStr  ");
							jsonResult = new JsonResult(jsonStr);
						} else {
							jsonResult = new JsonResult(returnStr);
							EvtLog.d(CACHE_TAG, "------------------正常返回值  threadId : " + currentId);
						}
						returnStr = null;
						result = null;
					} else {
						// 被释放
						jsonResult = new JsonResult(JsonResult.DIS_CONNECT_JSON_STRING);
						EvtLog.d(CACHE_TAG, "已被释放连接，返回 status== -1 状态 threadId : " + currentId);
					}
				} else {
					EvtLog.w(TAG, "返回为null");
				}
			} else {
				EvtLog.d(TAG, "server response code  " + respCode + "; msg :" + hrc.getResponseMessage());
				if (!isBackRun && isNeedDataCache(url) != null) {
					// 使用文件缓存数据
					try {
						jsonResult = getJsonResultByFile(url, params, isForBackString);
					} catch (Exception e) {
						HttpClientUtil.LAST_REQUEST_IS_OK = false;
						removeCache(currentId);
						throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
					}
				}
			}
		} catch (IOException ioe) {
			if (!isBackRun && isNeedDataCache(url) != null) {
				// 使用文件缓存数据
				try {
					jsonResult = getJsonResultByFile(url, params, isForBackString);
				} catch (Exception e) {
					HttpClientUtil.LAST_REQUEST_IS_OK = false;
					throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
				}
			} else {
				// 此处不为手动释放时才抛出异常
				if (ioe instanceof MalformedURLException || !isRelease(currentId)) {
					EvtLog.w(CACHE_TAG, "网络异常：" + ioe + " threadId : " + currentId);
					removeCache(currentId);
					HttpClientUtil.LAST_REQUEST_IS_OK = false;
					NetworkException exception = new NetworkException(
							PackageUtil.getString(R.string.network_is_not_available));
					if (ioe instanceof UnknownHostException) {
						exception.setexceptionCode(-1);
					}
					throw exception;
				} else {
					if (ioe instanceof UnknownHostException) {
						NetworkException exception = new NetworkException(
								PackageUtil.getString(R.string.network_is_not_available));
						exception.setexceptionCode(NetworkException.UNKNOWN_HOST_EXCEPTION_CODE);
						throw exception;
					}
					try {
						// 被释放
						jsonResult = new JsonResult(JsonResult.DIS_CONNECT_JSON_STRING);
						EvtLog.d(CACHE_TAG, "释放连接，返回 status== -1 状态 threadId : " + currentId);
					} catch (JSONException e) {
						EvtLog.w(TAG, e);
					}
				}
			}
		} catch (JSONException e) {
			EvtLog.w(TAG, e);
			HttpClientUtil.LAST_REQUEST_IS_OK = false;
			if (!isBackRun && isNeedDataCache(url) != null) {
				// 使用文件缓存数据
				try {
					jsonResult = getJsonResultByFile(url, params, isForBackString);
				} catch (Exception ex) {
					HttpClientUtil.LAST_REQUEST_IS_OK = false;
					removeCache(currentId);
					throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
				}
			}
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (hrc != null) {
					hrc.disconnect();
				}
			} catch (IOException e) {
				EvtLog.w(TAG, e);
			}
		}
		if (jsonResult != null) {
			HttpClientUtil.LAST_REQUEST_IS_OK = true;
		}
		removeCache(currentId);
		return jsonResult;
	}

	/**
	 * @throws Exception
	 *             缓存数据转换为JsonResult
	 * 
	 * @param url
	 *            请求地址
	 * @param params 请求参数
	 * @param isForBackString
	 *            接口是否是直接返回String
	 * @return JsonResult
	 * @throws
	 */
	public static JsonResult getJsonResultByFile(String url, List<NameValuePair> params, boolean isForBackString)
			throws Exception {
		String returnStr = getCacheFlieData(url, params);
		JsonResult jsonResult = null;
		if (isForBackString) {
			String jsonStr;
			jsonStr = String.format(JsonResult.SUCCES_JSON_STRING, Base64.encode(returnStr.getBytes(), Base64.DEFAULT));
			EvtLog.d(CACHE_FILE_TAG, " 缓存数据, 本地拼接数据 jsonStr  ");
			jsonResult = new JsonResult(jsonStr);
		} else {
			jsonResult = new JsonResult(returnStr);
			EvtLog.d(CACHE_FILE_TAG, "返回缓存数据");
		}
		if (EvtLog.IS_DEBUG_LOGGABLE) {
			printResponse(returnStr);
		}
		return jsonResult;
	}

	/**
	 * 添加header 设备标识
	 * 
	 * @param hrc
	 * @return void
	 */
	private void addHeader(HttpURLConnection hrc) {
		if (hrc != null) {
			hrc.addRequestProperty(QJApplicationBase.KEY_CLIENT_INFO, QJApplicationBase.getInstance().getClientInfo());
		}
	}

	/**
	 * 是否需本地缓存接口
	 * 
	 * @param url
	 *            注意此url 为原始方法url 即buildUrl之前的url
	 * @return HttpCacheConfigModel 为null时 不需缓存
	 */
	private HttpCacheConfigModel isNeedDataCache(String url) {
		if (StringUtil.isNullOrEmpty(url)) {
			return null;
		}
		Map<String, HttpCacheConfigModel> httpCacheMap = QJApplicationBase.getInstance().getHttpCacheConfigMap();
		if (httpCacheMap == null || httpCacheMap.isEmpty()) {
			return null;
		}
		if (httpCacheMap.containsKey(url)) {
			return httpCacheMap.get(url);
		}
		return null;
	}

	/**
	 * 生成缓存文件名
	 * 
	 * @param @param url
	 * @param params
	 * @return String
	 */
	private static String createFileName(String url, List<NameValuePair> params) {
		// 增加缓存文件与版本号的匹配
		if (params == null) {
			params = new ArrayList<NameValuePair>();
		}
		try {
			params.add(new BasicNameValuePair(ServerAPIConstant.KEY_VERSION_NAME_FLAG, PackageUtil.getVersionName()));
		} catch (NameNotFoundException e) {
			EvtLog.d(CACHE_FILE_TAG, " getVersionName error  " + e);
		}
		// get 与post请求 本地都拼接成get请求参数格式
		String buildUrl = buildUrl(url, params);
		String shortFileName = CMyEncrypt.getShortUrl(buildUrl);
		return shortFileName;
	}

	/**
	 * 保存本地文件（异步）
	 * 
	 * @param url
	 *            注意此url 为原始方法url 即buildUrl之前的url
	 * @param params
	 * @param jsonStr
	 * @param isForBackString
	 *            是否是只返回string
	 */
	private void cacheFileData(final String url, final List<NameValuePair> params, final String jsonStr,
			final boolean isForBackString) {
		if (StringUtil.isNullOrEmpty(jsonStr)) {
			return;
		}
		if (isNeedDataCache(url) == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				cacheDataToFile(url, params, jsonStr, isForBackString);
			}
		}).start();
	}

	public static boolean cacheDataToFile(final String url, final List<NameValuePair> params, final String jsonStr,
			final boolean isForBackString) {
		// 返回json数据格式时
		if (!isForBackString) {
			try {
				JsonResult jsonResult = new JsonResult(jsonStr);
				if (!jsonResult.isOK()) {
					// 接口返回不成功操作时，不做缓存处理
					return false;
				}
			} catch (JSONException e) {
				EvtLog.d(CACHE_FILE_TAG, "cache jsonStr error " + e);
				return false;
			}
		}
		// 只保存成功状态的数据
		String fileName = createFileName(url, params);
		if (StringUtil.isNullOrEmpty(fileName)) {
			return false;
		}
		String path = PackageUtil.getConfigString(CACHED_DIR);
		if (StringUtil.isNullOrEmpty(path)) {
			return false;
		}
		String fullPath = FileUtil.getFullPath(path, fileName + CACHE_FILE_EXT);
		File cachedFile = new File(fullPath);
		if (!cachedFile.exists()) {
			try {
				cachedFile.createNewFile();
				EvtLog.d(CACHE_FILE_TAG, "创建本地缓存文件成功：" + fullPath);
			} catch (IOException e) {
				EvtLog.w(CACHE_FILE_TAG, "创建本地缓存文件失败：" + e);
			}
		}
		// 是否保存成功,若发生异常，则返回失败
		boolean isResultOk = true;
		try {
			FileOutputStream fos = new FileOutputStream(cachedFile);
			long start = System.currentTimeMillis();
			fos.write(jsonStr.getBytes());
			fos.flush();
			fos.close();
			fos = null;
			long end = System.currentTimeMillis();
			EvtLog.d(CACHE_FILE_TAG, "写入本地缓存文件成功：" + fullPath + " 耗时：" + String.valueOf(end - start) + " ms");
		} catch (IOException e) {
			isResultOk = false;
			EvtLog.w(CACHE_FILE_TAG, "写入缓存文件失败：" + e);
		}
		cachedFile = null;
		return isResultOk;
	}

	/**
	 * 读取本地缓存数据
	 * 
	 * @param url
	 * @param params
	 * @return String
	 */
	private static String getCacheFlieData(String url, List<NameValuePair> params) throws Exception {
		String fileName = createFileName(url, params);
		if (StringUtil.isNullOrEmpty(fileName)) {
			return "";
		}
		String path = PackageUtil.getConfigString(CACHED_DIR);
		if (StringUtil.isNullOrEmpty(path)) {
			return "";
		}
		String fullPath = FileUtil.getFullPath(path, fileName + CACHE_FILE_EXT);
		EvtLog.d(CACHE_FILE_TAG, "读取本地缓存文件：" + fullPath);
		File cachedFile = new File(fullPath);
		if (cachedFile == null || !cachedFile.exists()) {
			return "";
		}
		byte[] fileContent = new byte[(int) cachedFile.length()];

		long start = System.currentTimeMillis();
		FileInputStream fis = new FileInputStream(cachedFile);
		fis.read(fileContent);
		fis.close();
		fis = null;
		long end = System.currentTimeMillis();
		EvtLog.d(CACHE_FILE_TAG, "读取本地缓存文件成功：" + fullPath + " 耗时：" + String.valueOf(end - start) + "ms");

		return new String(fileContent);
	}

	/**
	 * saveCookie 保存Cookie
	 * 
	 * @param url
	 *            地址
	 * @param cookieVal
	 * @return void
	 */
	private void saveCookie(String url, String cookieVal) {
		if (!StringUtil.isNullOrEmpty(cookieVal)) {
			String name = "";
			String domain = "";
			String values = "";

			String[] cookieArr = cookieVal.split(";");
			if (cookieArr != null && cookieArr.length > 1) {
				String[] cookieNameValue = cookieArr[0].split(SPLIT_FLAG_EQUAL);
				if (cookieNameValue != null && cookieNameValue.length > 1) {
					name = cookieNameValue[0];

					values = cookieNameValue[1];
					if (StringUtil.isNullOrEmpty(values)) {
						return;
					}
				}
				String[] domanArr = cookieArr[1].split(SPLIT_FLAG_EQUAL);
				if (domanArr != null && domanArr.length > 1) {
					domain = domanArr[1];
					try {
						domain = new URL(url).getHost();
					} catch (MalformedURLException e) {
						EvtLog.e(TAG, e);
						domain = "api.paidui.cn";
					}
				}
			}
			setCookieStores(domain, name, values);
		}
	}

	/**
	 * buildUrl 拼接get地址 参数
	 * 
	 * @param url
	 *            地址
	 * @param getParams
	 *            参数
	 * @return String 拼接后的参数
	 */
	public static String buildUrl(String url, List<NameValuePair> getParams) {
		if (getParams != null && !getParams.isEmpty()) {
			String returnUrl = url;
			if (url.indexOf(INTERROGATION) < 0) {
				returnUrl = url + INTERROGATION;
			}
			returnUrl = returnUrl + buildContent(getParams);
			EvtLog.d(TAG, returnUrl);
			return returnUrl;
		}
		return url;
	}

	/**
	 * buildContent 拼接post 参数内容
	 * 
	 * @param getParams
	 *            参数
	 * @return String 拼接后的参数
	 */
	private static String buildContent(List<NameValuePair> getParams) {
		String content = "";
		String tempParamters = "";
		for (int i = 0; i < getParams.size(); i++) {
			NameValuePair nameValuePair = getParams.get(i);
			if (nameValuePair != null) {
				String key = StringUtil.isNullOrEmpty(nameValuePair.getName()) ? "" : nameValuePair.getName();
				String value = StringUtil.isNullOrEmpty(nameValuePair.getValue()) ? "" : nameValuePair.getValue();
				tempParamters = tempParamters + SPLIT_FLAG_AND + key + SPLIT_FLAG_EQUAL + URLEncoder.encode(value);
			}
		}
		if (tempParamters.length() > 1) {
			content = tempParamters.substring(1);
		} else {
			content = tempParamters;
		}
		return content;
	}

	private void printPostData(List<NameValuePair> params) {
		if (params == null || params.size() < 1) {
			return;
		}
		EvtLog.d(TAG, "PostData: " + params.size());
		for (int i = 0; i < params.size(); ++i) {
			EvtLog.d(TAG, params.get(i).getName() + ":" + params.get(i).getValue());
		}
	}

	/**
	 * 打印服务器返回的信息，异步执行优化网络相应速度2013-01-28
	 * 
	 * @param s
	 *            服务器返回的信息
	 */
	private static void printResponse(final String s) {
		if (s == null || s.length() == 0 && !EvtLog.IS_DEBUG_LOGGABLE) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				EvtLog.d(TAG, "server response:\n");
				int idxBegin = 0;
				int idxEnd = 0;
				int iStep = PRINT_MAX_LENGTH;
				int length = s.length();
				while (idxBegin < length) {
					if (idxEnd + iStep > length) {
						idxEnd = length;
					} else {
						idxEnd += iStep;
					}
					EvtLog.d(TAG, ">>" + s.substring(idxBegin, idxEnd));

					idxBegin = idxEnd;
				}
			}
		}).start();
	}

	/**
	 * 
	 * @param url
	 *            url地址
	 * @param httpParams
	 *            参数
	 * @return 字节码
	 * @throws ClientProtocolException
	 *             协议异常
	 * @throws IOException
	 *             IO流异常
	 */
	public byte[] getBytes(String url, HttpParams httpParams) throws ClientProtocolException, IOException {
		return getBytes(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, httpParams);

	}

	/**
	 * 
	 * @param url
	 *            url地址
	 * @param requestType
	 *            requestType
	 * @param httpParams
	 *            参数
	 * @return 字节码
	 * @throws ClientProtocolException
	 *             协议异常
	 * @throws IOException
	 *             IO流异常
	 * @see com.qianjiang.framework.util.IPDWHttpClient#getBytes(java.lang.String,
	 *      int, org.apache.http.params.HttpParams)
	 */
	public byte[] getBytes(String url, int requestType, HttpParams httpParams) throws ClientProtocolException,
			IOException {
		byte[] result;
		HttpGet httpGet = new HttpGet(url);
		if (httpParams != null) {
			httpGet.setParams(httpParams);
		} else {
			httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_SHORT_IN_MS);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MIDDLE_IN_MS);
			ConnManagerParams.setMaxTotalConnections(httpParams, TEN);
		}
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), EIGHTY));
		final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		DefaultHttpClient client = new DefaultHttpClient(cm, httpParams);
		threadId++;
		long currentId = threadId;
		doHttpExecutefilter(url, requestType, null, client, null, currentId);
		setHttpClientInterceptor(client);
		if (COOKIE_STORE != null) {
			client.setCookieStore(COOKIE_STORE);
		}
		// 尝试3次
		HttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(httpGet);
		} catch (Exception e) {
			try {
				httpResponse = client.execute(httpGet);
			} catch (Exception ex) {
				httpResponse = client.execute(httpGet);
			}
		}

		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !isRelease(currentId)) {
			HttpEntity entity = httpResponse.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int bytesRead;
			byte[] data = new byte[BUFFERSIZE];
			while ((bytesRead = inputStream.read(data)) != -1) {
				buffer.write(data, 0, bytesRead);
			}
			buffer.flush();
			buffer.close();
			inputStream.close();
			result = buffer.toByteArray();
			entity.consumeContent();
			removeCache(currentId);
			return result;
		}
		removeCache(currentId);
		return null;
	}

	/**
	 * @param url
	 * @param httpParams
	 * @param getParams
	 * @return http响应
	 * @throws NetworkException
	 */
	@Override
	public HttpResponse getResponse(String url, HttpParams httpParams, List<NameValuePair> getParams)
			throws NetworkException {
		return getResponse(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, httpParams, getParams);
	}

	@Override
	public HttpResponse getResponse(String url, int requestType, HttpParams httpParams, List<NameValuePair> getParams)
			throws NetworkException {
		if (!NetUtil.isNetworkAvailable()) {
			HttpClientUtil.LAST_REQUEST_IS_OK = false;
			throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
		}

		threadId++;
		long currentId = threadId;
		String buildUrl = buildUrl(url, getParams);
		HttpGet httpGet = new HttpGet(buildUrl);
		if (httpParams != null) {
			httpGet.setParams(httpParams);
		}
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_SHORT_IN_MS);
		HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_MIDDLE_IN_MS);

		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		doHttpExecutefilter(url, requestType, getParams, client, null, currentId);
		setHttpClientInterceptor(client);

		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		if (COOKIE_STORE != null) {
			client.setCookieStore(COOKIE_STORE);
		}
		EvtLog.d(TAG, "client.execute begin, url: " + buildUrl);
		HttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(httpGet);
		} catch (ClientProtocolException e) {
			EvtLog.e(TAG, e);
		} catch (IOException e) {
			EvtLog.e(TAG, e);
		} finally {
			removeCache(currentId);
		}
		return httpResponse;
	}

	/**
	 * @param response
	 * @return response中的相应内容
	 */
	@Override
	public String getResponseString(HttpResponse response) {
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer("");
		try {
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			String nl = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + nl);
			}
			in.close();
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					EvtLog.w(TAG, e);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 获取网络返回的原始json格式数据
	 * 
	 * @param url
	 *            请求地址
	 * @param getParams
	 *            参数
	 * @return JSONObject 返回类型
	 * @throws
	 */
	// @Override
	// public JSONObject httpGet(String url, List<NameValuePair> getParams) {
	// return httpGet(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, getParams);
	// }

	/**
	 * 获取网络返回的原始json格式数据
	 * 
	 * @param url
	 *            请求地址
	 * @param requestType
	 *            requestType
	 * @param getParams
	 *            参数
	 * @return JSONObject 返回类型
	 * @throws
	 */
	// @Override
	// public JSONObject httpGet(String url, int requestType,
	// List<NameValuePair> getParams) {
	// JSONObject jsonResult = null;
	// try {
	// String result = getString(url, requestType, getParams);
	// if (result != null) {
	// jsonResult = new JSONObject(result);
	// }
	// } catch (Exception e) {
	// EvtLog.e(TAG, e);
	// }
	// return jsonResult;
	// }

	/**
	 * 获取接口返回的字符内容
	 * 
	 * @param url
	 *            接口地址
	 * @param getParams
	 *            参数
	 * @return String 返回类型
	 * @throws
	 */
	// @Override
	// public String getString(String url, List<NameValuePair> getParams) {
	// return getString(url, HttpClientUtil.DEFALUT_REQUEST_TYPE, getParams);
	// }
	//
	// @Override
	// public String getString(String url, int requestType, List<NameValuePair>
	// getParams) {
	// String jsonResult = "";
	// try {
	// HttpResponse response = getResponse(url, requestType, null, getParams);
	// jsonResult = getResponseString(response);
	// } catch (Exception e) {
	// EvtLog.e(TAG, e);
	// }
	// return jsonResult;
	// }

	/*********************** 微博使用相关方法 **************************/
	/**
	 * 把请求地址中的参数装入到bundle对象中.
	 * 
	 * @param url
	 *            请求地址
	 * @return a dictionary bundle of keys and values
	 */
	public static Bundle parseUrl(String url) {
		// hack to prevent MalformedURLException
		url = url.replace("weiboconnect", "http");
		try {
			URL u = new URL(url);
			Bundle b = decodeUrl(u.getQuery());
			b.putAll(decodeUrl(u.getRef()));
			return b;
		} catch (MalformedURLException e) {
			return new Bundle();
		}
	}

	/**
	 * 对Url进行解码
	 * 
	 * @param s
	 *            请求地址
	 * @return 解码后的值
	 */
	public static Bundle decodeUrl(String s) {
		Bundle params = new Bundle();
		if (s != null) {
			String[] array = s.split(SPLIT_FLAG_AND);
			for (String parameter : array) {
				String[] v = parameter.split(SPLIT_FLAG_EQUAL);
				if (v.length == 2) {
					params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
				} else if (v.length == 1) {
					params.putString(URLDecoder.decode(v[0]), "");
				}
			}
		}
		return params;
	}

	/**
	 * 输出当前Cookie
	 * 
	 * @param cookieStore
	 *            参数
	 * @return void 返回类型
	 * @throws
	 */
	private static void printCookies(CookieStore cookieStore) {
		if (EvtLog.IS_DEBUG_LOGGABLE && cookieStore != null) {
			for (Cookie cookie : cookieStore.getCookies()) {
				EvtLog.d(TAG,
						"domain: " + cookie.getDomain() + " name: " + cookie.getName() + " value: " + cookie.getValue());
			}
		}
	}

	/**
	 * cookiesToString
	 * 
	 * @param cookieStore
	 * @return String 例如ASP.NET_SessionId=5cu1fqdvvifq1tr3qk005zdy;
	 *         domain=api.paidui.cn;
	 */
	private String cookiesToString(CookieStore cookieStore) {
		String cookesStr = "";
		for (Cookie cookie : cookieStore.getCookies()) {
			EvtLog.d(TAG, " cookiesToString  :" + "domain: " + cookie.getDomain() + " name: " + cookie.getName()
					+ " value: " + cookie.getValue());
			cookesStr = cookie.getName() + SPLIT_FLAG_EQUAL + cookie.getValue() + ";" + "doman=" + cookie.getDomain();
			break;
		}
		return cookesStr;

	}

	/**
	 * 设置CookieStore
	 * 
	 * @param domain
	 *            域名
	 * @param name
	 *            Cookie名称
	 * @param values
	 *            值 参数
	 * @throws
	 */
	@Override
	public void setCookieStores(String domain, String name, String values) {
		BasicClientCookie2 cookie = new BasicClientCookie2(name, values);
		cookie.setDomain(domain);
		cookie.setValue(values);
		CookieStore cookieStore = new BasicCookieStore();
		cookieStore.addCookie(cookie);
		setCookieStore(cookieStore);
	}

	/**
	 * 获取当前请求的Cookies
	 * 
	 * @return 返回
	 * @throws
	 */
	@Override
	public String getCookies() {
		StringBuilder cookies = new StringBuilder();
		if (COOKIE_STORE != null) {
			for (Cookie cookie : COOKIE_STORE.getCookies()) {
				String cookieString = cookie.getName() + SPLIT_FLAG_EQUAL + cookie.getValue() + "; domain="
						+ cookie.getDomain() + ";";
				cookies.append(cookieString);
			}
		}
		return cookies.toString();
	}

	/**
	 * A private class which handles the content inflation.
	 */
	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			EvtLog.d(TAG, "Use gzip stream to unzip the content");
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}

	/**
	 * doHttpExecutefilter HttpClient 处理器 此方法在client.execute 前调用
	 * 
	 * @param url
	 *            注意此url 为原始方法url 即buildUrl之前的url
	 * @param requestType
	 *            处理类型
	 * @param mParams
	 *            参数
	 * @param mClient
	 *            DefaultHttpClient
	 * @param mConn
	 *            HttpURLConnection 添加HttpURLConnection类型的支持
	 * 
	 * @return boolean 是否执行本请求 为预留返回值
	 */
	private boolean doHttpExecutefilter(String url, int requestType, List<NameValuePair> mParams,
			DefaultHttpClient mClient, HttpURLConnection mConn, long threadId) {
		// 是否执行当前操作
		boolean isCurrentHttpDoExecute = false;
		// synchronized (mSync) {
		List<RequestModel> tempCacheList;
		switch (requestType) {
			case HttpClientUtil.CANCEL_SAME_URL_PREVIOUS_REQUEST:
				tempCacheList = getRequestCache(url);
				if (!tempCacheList.isEmpty()) {
					writeLock.lock();
					for (RequestModel mRequestModel : tempCacheList) {
						if (mClient != null) {
							DefaultHttpClient client = mRequestModel.HttpClient;
							if (client != null) {
								// 取消上次相同URL操作
								REQUEST_LIST.remove(mRequestModel);
								client.getConnectionManager().shutdown();
								EvtLog.d(CACHE_TAG, "取消上次相同URL操作 :" + url + "删除缓存--threadId : "
										+ mRequestModel.threadId + "-现缓存大小：" + REQUEST_LIST.size());
							}
						} else {
							HttpURLConnection conn = mRequestModel.Conn;
							if (conn != null) {
								// 取消上次相同URL操作
								REQUEST_LIST.remove(mRequestModel);
								conn.disconnect();
								EvtLog.d(CACHE_TAG, "取消上次相同URL操作 :" + url + "删除缓存--threadId : "
										+ mRequestModel.threadId + "-现缓存大小：" + REQUEST_LIST.size());
							}
						}
					}
					writeLock.unlock();
				}
				isCurrentHttpDoExecute = true;
				break;
			case HttpClientUtil.CANCEL_REPEAT_PREVIOUS_REQUEST:
				tempCacheList = getRequestCache(url, mParams);
				if (!tempCacheList.isEmpty()) {
					writeLock.lock();
					for (RequestModel mRequestModel : tempCacheList) {
						if (mClient != null) {
							DefaultHttpClient client = mRequestModel.HttpClient;
							if (client != null) {
								// 取消上次相同url与参数操作
								REQUEST_LIST.remove(mRequestModel);
								client.getConnectionManager().shutdown();
								EvtLog.d(CACHE_TAG, "取消上次重复操作 :" + url + "删除缓存--threadId : " + mRequestModel.threadId
										+ "-现缓存大小：" + REQUEST_LIST.size());
							}
						} else {
							HttpURLConnection conn = mRequestModel.Conn;
							if (conn != null) {
								// 取消上次相同url与参数操作
								REQUEST_LIST.remove(mRequestModel);
								conn.disconnect();
								EvtLog.d(CACHE_TAG, "取消上次重复操作:" + url + "删除缓存--threadId : " + mRequestModel.threadId
										+ "-现缓存大小：" + REQUEST_LIST.size());
							}
						}
					}
					writeLock.unlock();
				}
				isCurrentHttpDoExecute = true;

				break;
			case HttpClientUtil.NORMAL_REQUEST:
			default:
				isCurrentHttpDoExecute = true;
				// 啥都不做，直接跳过
				break;
		}

		// 添加到缓存中
		RequestModel mRequestModel = new RequestModel();
		if (mParams != null) {
			String mParamsStr = "";
			int size = mParams.size();
			for (int i = 0; i < size; i++) {
				NameValuePair mNameValuePair = mParams.get(i);
				if (mNameValuePair != null) {
					mParamsStr += mNameValuePair.getName() + SPLIT_FLAG_EQUAL + mNameValuePair.getValue()
							+ SPLIT_FLAG_AND;
				}
			}
			mRequestModel.ParamsStr = mParamsStr;
		}
		mRequestModel.Conn = mConn;
		mRequestModel.HttpClient = mClient;
		mRequestModel.Url = url;
		mRequestModel.threadId = threadId;
		addRequestCache(mRequestModel);
		EvtLog.d(CACHE_TAG,
				"缓存正在处理的请求 cache the httpClient  url : " + url + " ParamsStr : " + mRequestModel.ParamsStr
						+ "threadId : " + mRequestModel.threadId + "-现缓存大小：" + REQUEST_LIST.size() + "---缓存大小："
						+ REQUEST_LIST.size());
		// }
		return isCurrentHttpDoExecute;
	}

	private void addRequestCache(RequestModel mRequestModel) {
		writeLock.lock();
		REQUEST_LIST.add(mRequestModel);
		int size = REQUEST_LIST.size();
		// 删除 防止跨线程访问异常时未删除的缓存
		if (size > MAX_CACHE_REQUEST_COUNT) {
			for (int i = 0; i < size - MAX_CACHE_REQUEST_COUNT; i++) {
				REQUEST_LIST.remove(i);
				EvtLog.d(CACHE_TAG, "清理缓存---现缓存大小：" + REQUEST_LIST.size());
			}
		}
		writeLock.unlock();
	}

	/***
	 * 判断缓存中连接是否被释放
	 * 
	 * @param threadId
	 * @return boolean
	 */
	private boolean isRelease(long threadId) {
		boolean isRelease = true;
		readLock.lock();
		try {
			for (RequestModel mRequestModel : REQUEST_LIST) {
				if (mRequestModel != null && threadId == mRequestModel.threadId) {
					isRelease = false;
					break;
				}
			}
		} catch (Exception e) {
			// 避免跨线程引起问题(应该不会出现)
			EvtLog.w(CACHE_TAG, "已处理异常： isRelease is error:" + e.getMessage());
		} finally {
			readLock.unlock();
		}
		return isRelease;
	}

	/***
	 * 
	 * getRequestCache 根据条件获取正在请求的缓存集合
	 * 
	 * @param url
	 *            注意此url 为原始方法url 即buildUrl之前的url
	 * @return List<RequestModel>
	 */
	public List<RequestModel> getRequestCache(String url) {
		List<RequestModel> tempCacheList = new ArrayList<RequestModel>();
		readLock.lock();
		try {
			if (StringUtil.isNullOrEmpty(url) || REQUEST_LIST.isEmpty()) {
				return tempCacheList;
			}
			for (RequestModel mRequestModel : REQUEST_LIST) {
				if (mRequestModel != null && url.equals(mRequestModel.Url)) {
					tempCacheList.add(mRequestModel);
				}
			}
		} catch (Exception e) {
			// 避免跨线程引起问题(应该不会出现)
			EvtLog.w(CACHE_TAG, "已处理异常： getRequestCache is error:" + e.getMessage());
		} finally {
			readLock.unlock();
		}
		return tempCacheList;
	}

	/***
	 * 
	 * getRequestCache 根据条件获取正在请求的缓存集合
	 * 
	 * @param url
	 *            注意此url 为原始方法url 即buildUrl之前的url
	 * @param nameValuePairList
	 * @return List<RequestModel>
	 */
	public List<RequestModel> getRequestCache(String url, List<NameValuePair> nameValuePairList) {
		List<RequestModel> tempCacheList = new ArrayList<RequestModel>();
		readLock.lock();
		try {
			if (StringUtil.isNullOrEmpty(url) || REQUEST_LIST.isEmpty()) {
				return tempCacheList;
			}
			for (RequestModel mRequestModel : REQUEST_LIST) {
				String mParamsStr = mRequestModel.ParamsStr;
				if (mRequestModel != null && url.equals(mRequestModel.Url)
						&& isEqualsParamsStr(mParamsStr, nameValuePairList)) {
					tempCacheList.add(mRequestModel);
				}
			}
		} catch (Exception e) {
			// 避免跨线程引起问题(应该不会出现)
			EvtLog.w(CACHE_TAG, "已处理异常： getRequestCache is error:" + e.getMessage());
		} finally {
			readLock.unlock();
		}
		return tempCacheList;
	}

	/**
	 * 清除请求缓存
	 * 
	 * @param threadId
	 *            线程ID
	 */
	public void removeCache(long threadId) {
		writeLock.lock();
		try {
			for (RequestModel mRequestModel : REQUEST_LIST) {
				if (mRequestModel != null && threadId == mRequestModel.threadId) {
					REQUEST_LIST.remove(mRequestModel);
					EvtLog.d(CACHE_TAG, "线程请求已经完成，  清除缓存---- threadId:" + mRequestModel.threadId + " 现在缓存大小："
							+ REQUEST_LIST.size());
					break;
				}
			}
		} catch (Exception e) {
			// 避免跨线程引起问题(应该不会出现)
			EvtLog.w(CACHE_TAG, "已处理异常： getRequestCache is error:" + e.getMessage());
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * isEqualsParamsStr 比较条件是否相等
	 * 
	 * @param mParamsStr
	 * @param nameValuePairList
	 * @return boolean
	 */
	private static boolean isEqualsParamsStr(String mParamsStr, List<NameValuePair> nameValuePairList) {
		if (StringUtil.isNullOrEmpty(mParamsStr)) {
			if (nameValuePairList == null || nameValuePairList.size() == 0) {
				return true;
			}
			return false;
		}
		String[] mParamsStrArr = mParamsStr.split(SPLIT_FLAG_AND);
		if (mParamsStrArr != null) {
			if (nameValuePairList == null) {
				return false;
			}
			int size = nameValuePairList.size();
			if (mParamsStrArr.length != size) {
				return false;
			}
			List<String> tParamsList = new ArrayList<String>();
			// 传入参数

			for (int k = 0; k < size; k++) {
				NameValuePair mNameValuePair = nameValuePairList.get(k);
				if (mNameValuePair != null) {
					String paramsStr = mNameValuePair.getName() + SPLIT_FLAG_EQUAL + mNameValuePair.getValue();
					tParamsList.add(paramsStr);
				}
			}
			for (int i = 0, length = mParamsStrArr.length; i < length; i++) {
				String localParamsStr = mParamsStrArr[i];
				if (!tParamsList.contains(localParamsStr)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 封装请求缓存实体类
	 * 
	 * @author tan.xx
	 * @date 2012-10-30 下午3:26:30
	 * @version 2012-10-30 下午3:26:30 tan.xx
	 */
	private class RequestModel {
		public String ParamsStr = "";
		// 此url 为原始方法url 即buildUrl之前的url
		public String Url = "";
		public HttpURLConnection Conn;
		public DefaultHttpClient HttpClient;
		public long threadId;
	}

}
