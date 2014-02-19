package com.kakao.example.android.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kakao.api.Kakao;
import com.kakao.api.KakaoResponseHandler;
import com.kakao.api.Logger;
import com.kakao.example.android.KakaoManager;
import com.kakao.example.android.R;
import com.kakao.example.android.util.MessageUtil;

public class LoginActivity extends Activity implements OnClickListener {

	private KakaoResponseHandler loginResponseHandler;

	private Kakao kakao;
	Button btnLogin;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		kakao = KakaoManager.getKakao(getApplicationContext());
		
		btnLogin = (Button)findViewById(R.id.login_activity_btn_login);
		btnLogin.setOnClickListener(this);
		
		loginResponseHandler = new KakaoResponseHandler(getApplicationContext()) {

			@Override
			public void onStart() {
				super.onStart();
				btnLogin.setVisibility(View.GONE);
			}
			
			@Override
			public void onComplete(int httpStatus, int kakaoStatus, JSONObject result) {
				moveToMainActivity();
			}

			@Override
			public void onError(int httpStatus, int kakaoStatus, JSONObject result) {
				MessageUtil.toastForError(getApplicationContext(), httpStatus, kakaoStatus, result);
				btnLogin.setVisibility(View.VISIBLE);
			}

		};
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		/*
		Kakao.hasTokens() does not check validity of access token.
        It checks existence of access token and refresh token set in SDK by 3rd party app.
        Actual token check is done when calling API.
		*/
        if (kakao.hasTokens()) {
			// If token is set, call localUser.
			localUser();
		} else {
            /*
            If token is not set, run authorize.
            This is a method to check and re-authorize when access token and refresh token is not acquired after onActivityResult() is called by pressing [Agree and start] button from Kakaotalk.
            Calling this is not mandatory.
            */
            kakao.authorize(loginResponseHandler);
		}
	}

	/**
	 * Get my information.
	 */
	private void localUser() {
		KakaoResponseHandler localUserHandler = new KakaoResponseHandler(getApplicationContext()) {
			
			@Override
			public void onError(int httpStatus, int kakaoStatus, JSONObject result) {
				
				if (kakaoStatus == Kakao.STATUS_INVALID_GRANT) {
					MessageUtil.toastForError(getApplicationContext(), httpStatus, kakaoStatus, result);
					// TODO When kakaoStatus is STATUS_INVALID_GRANT or access token is sent to KakaoTokenListener as null or empty string logout | App restart | Terminate
					return;
				}
				
				String message = getString(R.string.retry);
				
				new AlertDialog.Builder(LoginActivity.this)
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
				Logger.getInstance().d("localUser():onComplete() - httpStatus: " + httpStatus + ", kakaoStatus: " + kakaoStatus + ", result: " + result);
				moveToMainActivity();
			}
		};
		
		kakao.localUser(localUserHandler);
	}
	
	private void moveToMainActivity() {
		startActivity(new Intent(LoginActivity.this, KakaoActivity.class));
		finish();
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if (id == R.id.login_activity_btn_login) 
			kakao.login(this, loginResponseHandler);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		kakao.onActivityResult(requestCode, resultCode, data, this, loginResponseHandler);
	}

}
