package org.jp.wiki;

import java.awt.Component;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import javax.net.ServerSocketFactory;
import org.rsna.util.FileUtil;

public class ProcessListener {

	int port;
	Component component;

	public ProcessListener(int port, Component component) {
		this.port = port;
		this.component = component;
	}

	public boolean check() {
		boolean result = false;
		try {
			URL url = new URL("http://127.0.0.1:"+port);
			URLConnection conn = url.openConnection();
			conn.connect();
			result = true;
		}
		catch (Exception ex) { }
		return result;
	}

	public boolean listen() {
		try {
			(new Listener()).start();
			return true;
		}
		catch (Exception unable) {
			return false;
		}
	}

	class Listener extends Thread {

		final ServerSocket serverSocket;

		public Listener() throws Exception {
			setName("Listener");
			ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
			serverSocket = serverSocketFactory.createServerSocket(port);
		}

		public void run() {
			while (!this.isInterrupted()) {
				try {
					Socket socket = serverSocket.accept();
					socket.close();
					component.setVisible(true);
				}
				catch (Exception ignore) { }
			}
			try { serverSocket.close(); }
			catch (Exception ignore) { }
		}
	}

}
