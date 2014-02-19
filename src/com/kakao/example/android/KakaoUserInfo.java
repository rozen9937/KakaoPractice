package com.kakao.example.android;

public class KakaoUserInfo {

	private String nickName;
	private String profileImageUrl;
	
	public KakaoUserInfo(String nickName, String url) {
		this.nickName = nickName;
		this.profileImageUrl = url;
	}
	
	public String getNickName () {
		return nickName;
	}
	
	public String getImageUrl () {
		return profileImageUrl;
	}
}
