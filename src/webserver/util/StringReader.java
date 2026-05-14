package webserver.util;

public class StringReader {
	private int pointer = 0;
	private final String str;
	
	public StringReader(String str) {
		this.str = str;
	}

	public String readLine() {
		StringBuilder builder = new StringBuilder();
		while(this.pointer < this.str.length()) {
			int c = this.str.charAt(this.pointer++);
			if(c == '\n')break;
			builder.append((char)c);
		}
		return builder.toString().trim();
	}
}
