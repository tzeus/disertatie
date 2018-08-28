package com.tudoreloprisan.email;

public class EmailPayLoad {
	private final String	subject;
	private final String	body;

	public EmailPayLoad(String subject, String body) {		
		this.subject = subject;
		this.body = body;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

}
