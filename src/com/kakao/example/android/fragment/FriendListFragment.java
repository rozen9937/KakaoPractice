package com.kakao.example.android.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kakao.api.Kakao;
import com.kakao.api.KakaoResponseHandler;
import com.kakao.api.Logger;
import com.kakao.api.StringKeySet;
import com.kakao.example.android.KakaoManager;
import com.kakao.example.android.R;
import com.kakao.example.android.util.MessageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendListFragment extends ListFragment {

	private static final String TAG = "FriendListActivity";
	private static final int ROUNDED_CORNER_RADIUS = 6;
	
	private Kakao kakao;
	private ArrayList<JSONObject> friends;
	private ArrayList<JSONObject> friendsForSearch;
	private FriendListAdapter friendListAdapeter;
	
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		kakao = KakaoManager.getKakao(getActivity());
		
    	options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.image_profile_default_blue)
		.showImageForEmptyUri(R.drawable.image_profile_default_blue)
		.showImageOnFail(R.drawable.image_profile_default_blue)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.displayer(new RoundedBitmapDisplayer(ROUNDED_CORNER_RADIUS))
		.build();
    	
		friendsForSearch = new ArrayList<JSONObject>();
		friends = new ArrayList<JSONObject>();
		friendListAdapeter = new FriendListAdapter(getActivity(), friendsForSearch);
		
		setListAdapter(friendListAdapeter);
		
		getFriends(); 
		
		return super.onCreateView(inflater, container, savedInstanceState);
//		return inflater.inflate(R.layout.fragment_friends_list, container, false);
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setDivider(null);
	}
	
	private void getFriends() {
		kakao.friends(new KakaoResponseHandler(getActivity()) {
			
			@Override
			public void onComplete(int httpStatus, int kakaoStatus, JSONObject result) {
				if (!result.has(StringKeySet.app_friends_info) && !result.has(StringKeySet.friends_info)) {
					Logger.getInstance().e(TAG, getString(R.string.null_friends));
					return;
				}

                // List of friends using this app among Kakaotalk friends
				JSONArray appFriendArray = result.optJSONArray(StringKeySet.app_friends_info);
				JSONArray friendArray = result.optJSONArray(StringKeySet.friends_info);

				if (appFriendArray.length() == 0 && friendArray.length() == 0) 
					MessageUtil.alert(getActivity(), getString(R.string.no_friends));
				
				friends.clear();
				for (int i = 0, n = friendArray.length(); i < n; i++) {
					JSONObject friend = friendArray.optJSONObject(i);
					if (friend != null) {
						friends.add(friend);
					}
				}

				changeList();
			}

			@Override
			public void onError(int httpStatus, int kakaoStatus, JSONObject result) {
				MessageUtil.alert(getActivity(), "onError", "httpStatus: " + httpStatus + ", kakaoStatus: " + kakaoStatus + ", result: " + result);
			}
			
		});
	}

	protected void changeList() {
		friendsForSearch.clear();
		friendListAdapeter.notifyDataSetChanged();
		
		for (JSONObject friend : friends) {
			friendsForSearch.add(friend);
		}
		
		if (friendsForSearch != null && friendsForSearch.size() > 0) 
			getListView().setSelection(0);
		friendListAdapeter.notifyDataSetChanged();		
	}

	public class FriendListAdapter extends ArrayAdapter<JSONObject> {

		public FriendListAdapter(Context context, List<JSONObject> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			switch(position) {
			case 0:
				convertView = View.inflate(getContext(), R.layout.fragment_friends_list, null);
				LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.friends_list_fragment_main);
				ViewGroup vg = (ViewGroup) ll.findViewById(R.id.friend_item_my_profile);
				
		    	TextView tv = (TextView) vg.findViewById(R.id.friend_item_tv_nickname);
				tv.setText(KakaoManager.getApp().getMyProfile().getNickName());
				
				ImageView iv = (ImageView) vg.findViewById(R.id.friend_item_iv_profile);
				imageLoader.displayImage(KakaoManager.getApp().getMyProfile().getImageUrl(), iv, options);
				convertView.setTag(ll);
				return convertView;
//			case 1:
//				return convertView;
//			case 2:
//				return convertView;
//			case 3:
//				return convertView;
//			case 4:
//				return convertView;
//			case 5:
//				return convertView;
			default:
				Holder h;
				
//				if (convertView == null) {
					convertView = View.inflate(getContext(), R.layout.friend_item, null);
					
					h = new Holder();
					h.ivProfile = (ImageView) convertView.findViewById(R.id.friend_item_iv_profile);
					h.tvNickname = (TextView) convertView.findViewById(R.id.friend_item_tv_nickname);
					
					convertView.setTag(h);
//				} else {
//					h = (Holder) convertView.getTag();
//				}
				
				JSONObject friend = getItem(position);
				
				h.tvNickname.setText(friend.optString("nickname"));
//				ImageLoader.getInstance(getContext().getApplicationContext()).loadThumbnailImage(friend.optString("profile_image_url"), h.ivProfile);
				imageLoader.displayImage(friend.optString("profile_image_url"), h.ivProfile, options);
				
				return convertView;					
				
			}
			
		}

		private class Holder {
			ImageView ivProfile;
			TextView tvNickname;
		}
	}
}