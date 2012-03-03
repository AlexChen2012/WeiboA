package com.weiboa.data;

public class TweetStatus {
	
	private Long id;
	private String text;
	private String userName;
	private long time;
	private String url;
	
	public TweetStatus(Long id, String text, String username, long time, String url){
		this.id = id;
		this.text = text;
		this.userName = username;
		this.time = time;
		this.url = url;
	}
	
	
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	
}
