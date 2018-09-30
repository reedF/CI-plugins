package com.reed.test;

import static com.jayway.restassured.path.json.JsonPath.with;

import java.io.InputStream;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import static com.jayway.restassured.path.json.JsonPath.with;

/**
 * 使用httpclient 请求jenkins rest api
 * 获取jenkins crumb:http://192.168.59.103:8082/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,%22:%22,//crumb)
 * @author reed
 *
 */
public class TestJenkinsRest {

	private static final Logger logger = LoggerFactory.getLogger(TestJenkinsRest.class);
	public static final String url = "http://192.168.59.103:8082/";
	public static final String user = "admin";
	public static final String pwd = "123456";
	public static final String apiToken = "b72301b150180a05631d1950a8ebbc0c";
	public static final String JenkinsCrumb = "a8d825fef37ff4ff5a33e05f8cbfb292";

	// helper construct to deserialize crumb json into
	public static class CrumbJson {
		public String crumb;
		public String crumbRequestField;
	}

	private void makeupHeader(HttpRequestBase http, boolean needGetCrumb) {
		if (needGetCrumb) {
			CrumbJson crumb = getJenkinsCrumb();
			http.setHeader(crumb.crumbRequestField, crumb.crumb);
		}
		http.setHeader("Authorization", "Basic " + Base64.encodeBase64String((user + ":" + pwd).getBytes()));
		http.setHeader("Connection", "close");
	}

	public CrumbJson getJenkinsCrumb() {
		CrumbJson crumbJson = null;
		HttpGet httpGet = new HttpGet(url + "crumbIssuer/api/json");
		makeupHeader(httpGet, false);
		// using try-with-resources and CloseableHttpClient to close http
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpResponse rsp = httpClient.execute(httpGet, this.getHttpClientContext());
			HttpEntity entity = rsp.getEntity();
			String result = EntityUtils.toString(entity);
			System.out.println(result);
			crumbJson = new Gson().fromJson(result, CrumbJson.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return crumbJson;
	}

	/**
	 * 创建Jenkins Job
	 * 
	 * @param jobName
	 * @throws Exception
	 */
	public void creatJenkinsJob(String jobName) {
		if (isJenkinsJobExist(jobName)) {
			logger.info("已经存在job：" + jobName);
		} else {
			HttpPost httpPost = new HttpPost(url + "/createItem?name=" + jobName);
			try (InputStream fileInput = ClassLoader.getSystemResourceAsStream("config.xml")) {
				InputStreamEntity entity = new InputStreamEntity(fileInput);
				entity.setContentEncoding("UTF-8");
				entity.setContentType("text/xml");
				httpPost.setEntity(entity);
				try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
					makeupHeader(httpPost, true);
					HttpResponse response = httpClient.execute(httpPost, this.getHttpClientContext());
					logger.info("reponse:" + response);
					System.out.println(response);
				} catch (Exception e) {
					throw e;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("成功创建job:" + jobName);
		}
	}

	/**
	 * 查询是否存在名为jobName的job
	 * 
	 * @param jobName
	 * @return
	 * @throws Exception
	 */
	public boolean isJenkinsJobExist(String jobName) {
		boolean r = false;
		HttpGet httpGet = new HttpGet(url + "/api/json");
		makeupHeader(httpGet, true);
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpResponse rsp = httpClient.execute(httpGet, this.getHttpClientContext());
			HttpEntity entity = rsp.getEntity();
			String result = EntityUtils.toString(entity);
			List<String> jobList = with(result).getList("jobs.name");
			for (String job : jobList) {
				if (jobName.equals(job)) {
					r = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			r = false;
		}
		return r;
	}

	/**
	 * 删除Jenkins Job
	 * 
	 * @param jobName
	 * @throws Exception
	 */
	public boolean deleteJenkinsJob(String jobName) {
		boolean r = false;
		if (!isJenkinsJobExist(jobName)) {
			logger.info("不存在job:" + jobName);
			r = true;
		} else {
			HttpPost httpPost = new HttpPost(url + "/job/" + jobName + "/doDelete");
			makeupHeader(httpPost, true);
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpResponse response = httpClient.execute(httpPost, this.getHttpClientContext());
				if (response.getStatusLine().getStatusCode() < 400) {
					r = true;
				}
				logger.info("reponse:" + response);
				System.out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	/**
	 * 构建触发Jenkins Job
	 * 
	 * @param jobName
	 * @throws Exception
	 */
	public boolean buildJenkinsJob(String jobName) {
		boolean r = false;

		HttpPost httpPost = new HttpPost(url + "/job/" + jobName + "/build");
		makeupHeader(httpPost, true);
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			try (CloseableHttpResponse response = httpClient.execute(httpPost, this.getHttpClientContext())) {
				if (response.getStatusLine().getStatusCode() < 400) {
					r = true;
				}
				logger.info("reponse:" + response);
				System.out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * 带参数的构建
	 * 
	 * @param jobName
	 * @param parameters
	 * @return
	 */
	public boolean buildJenkinsJobWithParameters(String jobName, Map<String, String> parameters) {
		boolean r = false;

		HttpPost httpPost = new HttpPost(url + "/job/" + jobName + "/buildWithParameters");
		makeupHeader(httpPost, true);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (String key : parameters.keySet()) {
			formparams.add(new BasicNameValuePair(key, parameters.get(key)));
		}
		UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			httpPost.setEntity(urlEntity);
			HttpResponse rsp = httpClient.execute(httpPost, this.getHttpClientContext());
			r = true;
			logger.info("reponse:" + rsp);
			System.out.println(rsp);
		} catch (Exception e) {
			logger.error(null, e);
		}

		return r;
	}

	/**
	 * 终止Jenkins Job构建
	 * 
	 * @param jobName
	 * @return
	 * @throws Exception
	 */
	public boolean stopJenkinsJob(String jobName) {
		boolean r = false;
		if (!isJenkinsJobExist(jobName)) {
			logger.info("不存在job:" + jobName);
		} else {
			HttpPost httpPost = new HttpPost(url + "/job/" + jobName + "/api/json");
			makeupHeader(httpPost, true);
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpResponse resp = httpClient.execute(httpPost, this.getHttpClientContext());
				HttpEntity entity = resp.getEntity();
				String result = EntityUtils.toString(entity);
				int buildNumber = with(result).get("lastBuild.number");
				HttpPost stopJenkinsRequest = new HttpPost(url + "/job/" + jobName + "/" + buildNumber + "/stop");
				HttpResponse response = httpClient.execute(stopJenkinsRequest, this.getHttpClientContext());
				if (response.getStatusLine().getStatusCode() < 400) {
					r = true;
				}
				logger.info("reponse:" + response);
				System.out.println(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	public HttpClientContext getHttpClientContext() {
		HttpClientContext httpClientContext = HttpClientContext.create();
		httpClientContext.setCredentialsProvider(this.getCredentialsProvider());
		// httpClientContext.setAuthCache(this.getAuthCache());
		return httpClientContext;
	}

	public CredentialsProvider getCredentialsProvider() {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(AuthScope.ANY), new UsernamePasswordCredentials(user, apiToken));
		return credsProvider;
	}

	public static void main(String[] args) throws Exception {
		String jobName = "11";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("domain", "www.baidu.com");
		parameters.put("run_id", "222");
		TestJenkinsRest test = new TestJenkinsRest();
		test.deleteJenkinsJob(jobName);
		test.creatJenkinsJob(jobName);
		test.buildJenkinsJob(jobName);
		// test.buildJenkinsJobWithParameters(jobName, parameters);
		test.deleteJenkinsJob(jobName);

	}
}