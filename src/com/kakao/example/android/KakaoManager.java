package com.kakao.example.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.kakao.api.Kakao;
import com.kakao.api.Kakao.KakaoTokenListener;
import com.kakao.api.Kakao.LogLevel;

public class KakaoManager {
	private static KakaoPracticeApplication app;
	private static Context context;
	private static Kakao kakao;
	
	public static void setApp(Context c, Application application) {
		app = (KakaoPracticeApplication) application;
		context = c;
	}
	
	public static KakaoPracticeApplication getApp() {
		if (app != null)
			return app;
		
		app = (KakaoPracticeApplication) context.getApplicationContext();
		return app;
	}
	
	public static Kakao getKakao(Context context) {
		if (kakao != null) {
			return kakao;
		}
		
		try {
			// using device id that was generated unique id
			String clientID = context.getResources().getString(R.string.client_id);
			String clientSecret = context.getResources().getString(R.string.client_secret);
			String clientRedirectUri = "kakao" + context.getResources().getString(R.string.client_id) + "://exec";
			kakao = new Kakao(context, clientID, clientSecret, clientRedirectUri);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Modify to Release when releasing.
		kakao.setLogLevel(LogLevel.Debug);
		final String prefKey = context.getResources().getString(R.string.pref_key);
		final SharedPreferences pref = context.getSharedPreferences(prefKey, Context.MODE_PRIVATE);

        KakaoTokenListener tokenListener = new KakaoTokenListener() {
            public void onSetTokens(String accessToken, String refreshToken) {
                // When applying, must save on location where it's secure.
                if(TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(refreshToken)) {
                    // If accessToken is null or "", logout
                	// Delete saved token.
                    pref.edit().remove("access_token").remove("refresh_token").commit();

                    // TODO When kakaoStatus is STATUS_INVALID_GRANT or access token is sent to KakaoTokenListener as null or empty string logout | App restart | Terminate
                } else {
                    pref.edit().putString("access_token", accessToken).putString("refresh_token", refreshToken).commit();
                }
            }
        };
        kakao.setTokenListener(tokenListener);

        String accessToken = pref.getString("access_token", null);
        String refreshToken = pref.getString("refresh_token", null);
        kakao.setTokens(accessToken, refreshToken);
		
		return kakao;
	}
}
