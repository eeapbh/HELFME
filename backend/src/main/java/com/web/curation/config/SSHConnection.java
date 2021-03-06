package com.web.curation.config;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHConnection {
	private final static String HOST = "i4b203.p.ssafy.io";
	private final static Integer PORT = 22;
	private final static String SSH_USER = "ubuntu";
	private final static String S_PATH_FILE_PRIVATE_KEY = "/var/www/html/dist/I4B203T.ppk";
	// private final static String SSH_PW = "";
	
	private Session session;
	
	public void closeSSH() {
		session.disconnect();
	}
	
	public SSHConnection() {
		try {
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			jsch.addIdentity(S_PATH_FILE_PRIVATE_KEY);
			session = jsch.getSession(SSH_USER, HOST, PORT);
			// session.setPassword(SSH_PW);
			session.setConfig(config);
			session.connect();
			session.setPortForwardingL(3333, "127.0.0.1", 3306);
		} catch (JSchException e) {
			e.printStackTrace();
		}		
	}

}
