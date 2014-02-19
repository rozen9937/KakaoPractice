package com.kakao.example.android.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.kakao.api.Kakao;
import com.kakao.api.KakaoResponseHandler;
import com.kakao.example.android.KakaoManager;
import com.kakao.example.android.KakaoPracticeApplication;
import com.kakao.example.android.KakaoUserInfo;
import com.kakao.example.android.R;
import com.kakao.example.android.util.MessageUtil;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class SplashActivity extends Activity {
    private static final long SPLAH_DELAY = 100;
    
    private Kakao kakao; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        KakaoManager.setApp(this, getApplication());
        kakao = KakaoManager.getKakao(getApplicationContext());
        
        new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (kakao.hasTokens()) {
					localUser();
				} else {
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
				}
			}
		}, SPLAH_DELAY);
    }
    
	private void localUser() {
		KakaoResponseHandler localUserHandler = new KakaoResponseHandler(getApplicationContext()) {
			@Override
			public void onError(int httpStatus, int kakaoStatus, JSONObject result) {
				if (kakaoStatus == Kakao.STATUS_INVALID_GRANT) {
					MessageUtil.toastForError(getApplicationContext(), httpStatus, kakaoStatus, result);
					// TODO When kakaoStatus is STATUS_INVALID_GRANT or access token is sent to KAkaoTokenListener as null 
					// or empty string logout | App restart | Terminate
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
					return;
				}
				String message = getString(R.string.re_localUser);

				new AlertDialog.Builder(SplashActivity.this)
					.setMessage(Html.fromHtml(message))
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Re-call localUser
							localUser();
						}
					})
					.create().show();
			}
			
			@Override
			public void onComplete(int httpStatus, int kakaoStatus, JSONObject result) {
				friends();
				
				String myNickName = "";
				String myProfileImageUrl = "";
				try {
					myNickName = result.getString("nickname");
					myProfileImageUrl = result.getString("profile_image_url"); 
				} catch (JSONException e) {
					e.printStackTrace();
				}
				KakaoUserInfo myProfile = new KakaoUserInfo(myNickName, myProfileImageUrl);
				KakaoManager.getApp().setMyProfile(myProfile);
			}
		};
		kakao.localUser(localUserHandler);
	}
	
	private void friends() {
		KakaoResponseHandler friendsHandler = new KakaoResponseHandler(getApplicationContext()) {
			@Override
			public void onError(int httpStatus, int kakaoStatus, JSONObject result) {
				if (kakaoStatus == Kakao.STATUS_INVALID_GRANT) {
					MessageUtil.toastForError(getApplicationContext(), httpStatus, kakaoStatus, result);
					// TODO When kakaoStatus is STATUS_INVALID_GRANT or access token is sent to KakaoTokenListener as null
					// or empty string logout | App restart | Terminate
					return;
				}
				
				String message = getString(R.string.re_friendsList);
				
				new AlertDialog.Builder(SplashActivity.this)
					.setMessage(message)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Re-call friends list
							friends();
						}
					})
					.create().show();
			}
			
			@Override
			public void onComplete(int httpStatus, int kakaoStatus, JSONObject result) {
				startActivity(new Intent(SplashActivity.this, KakaoActivity.class));
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				
				int friendsCount = 0;
				try {
					String jsonKeyFriendsCount = getResources().getString(R.string.json_key_friends_count);
					friendsCount = result.getInt(jsonKeyFriendsCount);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				KakaoManager.getApp().setFriendsCount(friendsCount);
				finish();
			}
		};
		kakao.friends(friendsHandler);
	}
}
