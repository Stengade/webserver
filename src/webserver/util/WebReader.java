package webserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class WebReader {
	private InputStream stream;
	
	public WebReader(Socket socket) throws IOException {
		this.stream = socket.getInputStream();
	}

	public String readLine() {
		StringBuilder builder = new StringBuilder();
		
		while(true) {
			try {
				int c = this.stream.read();
				if(c == -1 || c == '\n')break;
				builder.append((char)c);
			} catch (IOException e) {
				return null;
			}
		}
		
		return builder.toString().trim();
	}

	public void read(byte[] buffer) {
		try {
			this.stream.read(buffer);
		} catch (IOException e) {
			return;
		}
	}

	public int readInt() {
		try {
			return this.stream.read();
		} catch (IOException e) {
			return -1;
		}
	}

}
