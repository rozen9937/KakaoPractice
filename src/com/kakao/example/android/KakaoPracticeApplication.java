package com.kakao.example.android;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import android.app.Application;
import android.content.Context;

public class KakaoPracticeApplication extends Application {

	private KakaoUserInfo myProfile;
	private int friendsCound;
	
	public void setMyProfile(KakaoUserInfo profile) {
		myProfile = profile;
	}
	
	public KakaoUserInfo getMyProfile() {
		return myProfile;
	}
	
	public void setFriendsCount(int count) {
		friendsCound = count;
	}
	
	public int getFriendsCount() {
		return friendsCound;
	}
	
	public void onCreate() {
		super.onCreate();
		initImageLoader(getApplicationContext());
	}
	
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}
