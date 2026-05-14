package webserver.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class WebWriter {
	private OutputStream stream;

	public WebWriter(Socket socket) throws IOException {
		this.stream = socket.getOutputStream();
	}

	public boolean write(String string) {
		return this.write(string.getBytes());
	}
	
	public boolean write(byte[] bytes) {
		try {
			this.stream.write(bytes);
			this.stream.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean writeLine(String string) {
		return this.write(string.trim()+"\r\n");
	}

}
