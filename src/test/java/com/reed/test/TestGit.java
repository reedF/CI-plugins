package com.reed.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.CommitAction;
import org.gitlab4j.api.models.CommitAction.Action;
import org.gitlab4j.api.models.CommitAction.Encoding;
import org.gitlab4j.api.models.Namespace;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Visibility;

public class TestGit {

	public static final String HOST_URL = "http://192.168.59.103";//
	public static final String API_TOKEN = "5AMtZ-uXsRC_kuV3YUyT";
	public static final String PROJECT_NAME = "test-demo";
	public static final String USER_NAME = "a1";
	public static final String USER_PWD = "12345678";
	public static final String DEFAULT_BRANCH = "master";
	public static final String ROOT_PATH = System.getProperty("user.dir");

	private static GitLabApi gitLabApi = new GitLabApi(HOST_URL, API_TOKEN);

	public static List<File> getUnCommitFiles(String dir) {
		List<File> r = null;
		if (StringUtils.isNotBlank(dir)) {
			r = new ArrayList<>();
			File root = new File(dir);
			if (root != null) {
				if (root.isFile()) {
					r.add(root);
				} else {
					File[] fs = root.listFiles();
					if (fs != null) {
						for (File f : fs) {
							if (f != null) {
								r.addAll((getUnCommitFiles(f.getAbsolutePath())));
							}
						}
					}
				}
			}
		}
		return r;
	}

	public static String readFileContent(File f) {
		String r = null;
		if (f != null && f.isFile()) {
			try (InputStream in = new FileInputStream(f)) {
				r = IOUtils.toString(in, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	public static String getAuthToken(String userName, String pwd) {
		String r = null;
		try {
			GitLabApi gitLabApi = GitLabApi.oauth2Login(HOST_URL, USER_NAME, USER_PWD.toCharArray());
			r = gitLabApi.getAuthToken();
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static String getPrivateToken(String userName, String pwd) {
		String r = null;
		try {
			GitLabApi gitLabApi = GitLabApi.login(HOST_URL, USER_NAME, USER_PWD);
			r = gitLabApi.getAuthToken();
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static Namespace getNamespaceByName(String userName, String pwd, String name) {
		Namespace r = null;
		try {
			GitLabApi gitLabApi = GitLabApi.login(HOST_URL, USER_NAME, USER_PWD);
			List<Namespace> ns = gitLabApi.getNamespaceApi().getNamespaces();
			for (Namespace n : ns) {
				if (n != null && name.equals(n.getName())) {
					r = n;
					break;
				}
			}
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * 根据名称获取 gitlab namespace
	 * @param name 可以是用户账号名或组名（group）
	 * @return
	 */
	public static Namespace getNamespaceByName(String name) {
		Namespace r = null;
		try {
			List<Namespace> ns = gitLabApi.getNamespaceApi().findNamespaces(name);
			for (Namespace n : ns) {
				if (n != null && name.equals(n.getName())) {
					r = n;
					break;
				}
			}
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static List<Project> getProjects() {
		List<Project> r = null;
		try {
			// gitLabApi = GitLabApi.login(HOST_URL, USER_NAME, USER_PWD);
			// gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO);
			r = gitLabApi.getProjectApi().getProjects();
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	public Project getProject(String namespace, String projectName) {
		Project p = null;
		if (StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(projectName)) {
			try {
				p = gitLabApi.getProjectApi().getProject(namespace, projectName);
			} catch (GitLabApiException e) {
				e.printStackTrace();
			}
		}

		return p;
	}

	/**
	 * 创建
	 * @param namespaceId 注：使用git group name获取到的namespace时，当前用户需在此group内且有master以上权限
	 * @param projectName
	 * @return
	 */
	public static Project createProject(int namespaceId, String projectName) {
		Project r = new Project().withNamespaceId(namespaceId).withName(projectName)
				//path为namespace与projectName的下一级目录，并非替代namespace的目录
				//.withPath(path)
				.withDescription("My project for demonstration.").withIssuesEnabled(true).withMergeRequestsEnabled(true)
				.withWikiEnabled(true).withSnippetsEnabled(true).withPublic(true).withVisibility(Visibility.PUBLIC);

		try {
			r = gitLabApi.getProjectApi().createProject(r);
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static boolean pushProject(Integer projectId, List<File> files) {
		boolean r = false;
		List<CommitAction> actions = new ArrayList<>();
		try {
			gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO);
			for (File f : files) {
				if (f != null) {
					CommitAction act = new CommitAction();
					act.setAction(Action.CREATE);
					act.setContent(readFileContent(f));
					act.setEncoding(Encoding.TEXT);
					// remove root dir
					act.setFilePath(f.getPath().replace(ROOT_PATH, "").replaceAll("\\\\", "/"));
					actions.add(act);
				}
			}
			gitLabApi.getCommitsApi().createCommit(projectId, DEFAULT_BRANCH, "init", DEFAULT_BRANCH, null, null,
					actions);
			r = true;
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static boolean deleteProject(Integer projectId) {
		boolean r = false;
		try {
			gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO);
			gitLabApi.getProjectApi().deleteProject(projectId);
			r = true;
		} catch (GitLabApiException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static void main(String[] args) {
		TestGit.getPrivateToken(USER_NAME, USER_PWD);
		Namespace n = TestGit.getNamespaceByName(USER_NAME, USER_PWD, USER_NAME);
		Project project = TestGit.createProject(n.getId(), PROJECT_NAME);
		TestGit.pushProject(project.getId(), getUnCommitFiles(ROOT_PATH + "/src"));
		List<Project> projects = TestGit.getProjects();
		for (Project p : projects) {
			if (p != null && p.getName().equals(PROJECT_NAME)) {
				TestGit.deleteProject(p.getId());
			}
		}

	}

}
