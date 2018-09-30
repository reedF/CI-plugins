package com.reed.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;

public class TestJenkins {
	public static final String url = "http://192.168.59.103:8082/";
	public static final String user = "admin";
	public static final String pwd = "123456";
	public static final String jobName = "maven-test";
	// public static final String JenkinsCrumb =
	// "a8d825fef37ff4ff5a33e05f8cbfb292";
	public static final String configXml = "config.xml";

	public static Map<String, Job> listJob(JenkinsServer js) {
		Map<String, Job> r = null;
		try {
			r = js.getJobs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static void createJob(JenkinsServer js) {
		try (InputStream fileInput = ClassLoader.getSystemResourceAsStream(configXml)) {
			// 需定义完整的config.xml，否则会创建失败
			String jobXml = IOUtils.toString(fileInput, "utf-8");
			js.createJob(jobName, jobXml, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteJob(JenkinsServer js) {
		try {
			js.deleteJob(jobName,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void buildJob(JenkinsServer js) {
		try {
			JobWithDetails job = js.getJob(jobName);
			QueueReference queueRef = job.build(true);

			System.out.println("Ref:" + queueRef.getQueueItemUrlPart());

			job = js.getJob(jobName);
			QueueItem queueItem = js.getQueueItem(queueRef);
			while (!queueItem.isCancelled() && job.isInQueue()) {
				System.out.println("In Queue " + job.isInQueue());
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				job = js.getJob(jobName);
				queueItem = js.getQueueItem(queueRef);
			}
			System.out.println("ended waiting.");

			System.out.println("cancelled:" + queueItem.isCancelled());

			if (queueItem.isCancelled()) {
				System.out.println("Job has been canceled.");
				return;
			}

			job = js.getJob(jobName);
			Build lastBuild = job.getLastBuild();

			boolean isBuilding = lastBuild.details().isBuilding();
			while (isBuilding) {
				System.out.println("Is building...(" + lastBuild.getNumber() + ")");
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				isBuilding = lastBuild.details().isBuilding();
			}

			System.out.println("Finished.");
			System.out.println("Result: " + lastBuild.details().getResult());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try (JenkinsServer js = new JenkinsServer(URI.create(url), user, pwd)) {
			listJob(js);
			createJob(js);
			buildJob(js);		
			deleteJob(js);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
