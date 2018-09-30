package com.reed.test;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

/**
 * 获取jenkins crumb
 * @author reed
 *
 */
public class JenkinsMonitor {
	public static final String user = "admin";
	public static final String pwd = "123456";
	public static final String apiToken = "b72301b150180a05631d1950a8ebbc0c";

	public static CredentialsProvider getCredentialsProvider() {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(AuthScope.ANY), new UsernamePasswordCredentials(user, apiToken));
		return credsProvider;
	}

	public static void main(String[] args) throws Exception {

		String protocol = "http";
		String host = "192.168.59.103";
		int port = 8082;
		String key = user + ":" + pwd;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getCredentialsProvider().setCredentials(new AuthScope(host, port),
				new UsernamePasswordCredentials(user, pwd));

		String jenkinsUrl = protocol + "://" + host + ":" + port + "/";

		try {
			// get the crumb from Jenkins
			// do this only once per HTTP session
			// keep the crumb for every coming request
			System.out.println("... issue crumb");
			HttpGet httpGet = new HttpGet(jenkinsUrl + "crumbIssuer/api/json");
			httpGet.setHeader("Authorization", "Basic " + Base64.encodeBase64String(key.getBytes()));
			String crumbResponse = toString(httpclient, httpGet);
			CrumbJson crumbJson = new Gson().fromJson(crumbResponse, CrumbJson.class);

			// add the issued crumb to each request header
			// the header field name is also contained in the json response
			System.out.println("... issue rss of latest builds");
			HttpPost httpost = new HttpPost(jenkinsUrl + "rssLatest");
			httpost.addHeader(crumbJson.crumbRequestField, crumbJson.crumb);
			httpost.setHeader("Authorization", "Basic " + Base64.encodeBase64String(key.getBytes()));
			toString(httpclient, httpost);

		} finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	// helper construct to deserialize crumb json into
	public static class CrumbJson {
		public String crumb;
		public String crumbRequestField;
	}

	private static String toString(DefaultHttpClient client, HttpRequestBase request) throws Exception {
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = client.execute(request, responseHandler);
		System.out.println(responseBody + "\n");
		return responseBody;
	}

}