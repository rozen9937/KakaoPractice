package com.example.kakaoimi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class PagerSlidingTabStrip extends HorizontalScrollView {

	public interface IconTabProvider {
		public int getPageIconResId(int position);
	}

	private LinearLayout.LayoutParams tabLayoutParams;

	private final PageListener pageListener = new PageListener();
	public OnPageChangeListener delegatePageListener;

	private LinearLayout tabsContainer;
	private ViewPager pager;
	
	private int tabCount;

	private int currentTabPosition = 0;
	private float currentTabPositionOffset = 0f;

	private Paint rectPaint;

	private int scrollOffset = 0;
	private int indicatorHeight = 0;
	private int underlineHeight = 0;
	private int tabPadding = 0;

	private int lastScrollX = 0;

	private int tabBackgroundResId = 0;

	private Locale locale;
	
	private ViewGroup mViewGroupTitle;
	private TextView mTextViewTitle;
	private TextView mTextViewFriendNum;
	private ImageButton mImageButtonEdit;
	private ImageButton mImageButtonSettings;
	private ImageButton mImageButtonNewChat;

	public PagerSlidingTabStrip(Context context) {
		this(context, null);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setFillViewport(true);
		setWillNotDraw(false);

		tabsContainer = new LinearLayout(context);
		tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
		tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(tabsContainer);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
		underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
		tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
		scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
		
		// get custom attrs

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

		indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, indicatorHeight);
		underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, underlineHeight);
		tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, tabPadding);
		tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, tabBackgroundResId);
		scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, scrollOffset);
		
		a.recycle();

		rectPaint = new Paint();
		rectPaint.setAntiAlias(true);
		rectPaint.setStyle(Style.FILL);

		tabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

		if (locale == null) {
			locale = getResources().getConfiguration().locale;
		}
	}
	
	public void setTitleLayout(ViewGroup layout) {
		mViewGroupTitle = layout;
		mTextViewTitle = (TextView) mViewGroupTitle.findViewById(R.id.text_title);
		mTextViewFriendNum = (TextView) mViewGroupTitle.findViewById(R.id.text_title_friend_num);
		mTextViewFriendNum.setText("100");

		mImageButtonEdit = (ImageButton) mViewGroupTitle.findViewById(R.id.imagebutton_edit);
		mImageButtonNewChat = (ImageButton) mViewGroupTitle.findViewById(R.id.imagebutton_new_chat);
		mImageButtonSettings = (ImageButton) mViewGroupTitle.findViewById(R.id.imagebutton_settings);
	}

	public void setViewPager(ViewPager pager) {
		this.pager = pager;

		if (pager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}

		pager.setOnPageChangeListener(pageListener);

		notifyDataSetChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.delegatePageListener = listener;
	}

	public void notifyDataSetChanged() {

		tabsContainer.removeAllViews();

		tabCount = pager.getAdapter().getCount();

		for (int i = 0; i < tabCount; i++) {
			if (pager.getAdapter() instanceof IconTabProvider) {
				addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
			} 		
		}

		setCurrentItem(currentTabPosition);

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}

				currentTabPosition = pager.getCurrentItem();
				scrollToChild(currentTabPosition, 0);
			}
		});
	}
	
    private void setCurrentItem(int item) {
        if (pager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        currentTabPosition = item;
        pager.setCurrentItem(item);

        updateTitleIcon(item);
        updateTitleText(item);

        final int tabCount = tabsContainer.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            final View child = tabsContainer.getChildAt(i);
            final boolean isSelected = (i == item);
            child.setSelected(isSelected);
        }
    }

	private void updateTitleIcon(int item) {
		Log.i("TAG", "updateTitleIcon in!");
		Log.i("TAG", "updateTitleIcon mImageButtonEdit is " + (mImageButtonEdit == null? "null" : "NOT null"));
		switch(item){
		case 0:
			mImageButtonEdit.setVisibility(View.VISIBLE);
			mImageButtonSettings.setVisibility(View.VISIBLE);
			mImageButtonNewChat.setVisibility(View.GONE);
			break;
		case 1:
			mImageButtonEdit.setVisibility(View.VISIBLE);
			mImageButtonSettings.setVisibility(View.GONE);
			mImageButtonNewChat.setVisibility(View.VISIBLE);
			break;
		case 2:
			mImageButtonEdit.setVisibility(View.VISIBLE);
			mImageButtonSettings.setVisibility(View.GONE);
			mImageButtonNewChat.setVisibility(View.GONE);
			break;
		case 3:
			mImageButtonEdit.setVisibility(View.GONE);
			mImageButtonSettings.setVisibility(View.GONE);
			mImageButtonNewChat.setVisibility(View.GONE);
			break;
	}
	}
	
	private void updateTitleText(int item) {
		switch(item){
			case 0:
				mTextViewTitle.setText(R.string.title_friend);
				mTextViewFriendNum.setVisibility(View.VISIBLE);
				break;
			case 1:
				mTextViewTitle.setText(R.string.title_chatting);
				mTextViewFriendNum.setVisibility(View.GONE);
				break;
			case 2:
				mTextViewTitle.setText(R.string.title_search_friend);
				mTextViewFriendNum.setVisibility(View.GONE);
				break;
			case 3:
				mTextViewTitle.setText(R.string.title_more);
				mTextViewFriendNum.setVisibility(View.GONE);
				break;
		}
	}

	private void addIconTab(final int position, int resId) {
		ImageButton tab = new ImageButton(getContext());
		tab.setImageResource(resId);

		addTab(position, tab);
	}

	private void addTab(final int position, View tab) {
		tab.setFocusable(true);
		tab.setBackgroundResource(tabBackgroundResId);
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(position);
			}
		});
		tab.setPadding(tabPadding, 0, tabPadding, 0);
		
		tabsContainer.addView(tab, position, tabLayoutParams);
	}

	private void scrollToChild(int position, int offset) {
		if (tabCount == 0) {
			return;
		}

		int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;
		if (position > 0 || offset > 0) {
			newScrollX -= scrollOffset;
		}

		if (newScrollX != lastScrollX) {
			lastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isInEditMode() || tabCount == 0) {
			return;
		}

		final int height = getHeight();
		
		// draw underline
		
		rectPaint.setColor(getResources().getColor(R.color.tab_underline_color));
		canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);

		// draw indicator line
		
		rectPaint.setColor(getResources().getColor(R.color.tab_indicator_color));

		View currentTab = tabsContainer.getChildAt(currentTabPosition);
		float lineLeft = currentTab.getLeft();
		float lineRight = currentTab.getRight();

		// if there is an offset, start interpolating left and right coordinates between current and next tab
		if (currentTabPositionOffset > 0f && currentTabPosition < tabCount - 1) {

			View nextTab = tabsContainer.getChildAt(currentTabPosition + 1);
			final float nextTabLeft = nextTab.getLeft();
			final float nextTabRight = nextTab.getRight();

			lineLeft = (currentTabPositionOffset * nextTabLeft + (1f - currentTabPositionOffset) * lineLeft);
			lineRight = (currentTabPositionOffset * nextTabRight + (1f - currentTabPositionOffset) * lineRight);
		}

		canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);
	}

	private class PageListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			currentTabPosition = position;
			currentTabPositionOffset = positionOffset;

			scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));

			invalidate();

			if (delegatePageListener != null) {
				delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				scrollToChild(pager.getCurrentItem(), 0);
			}

			if (delegatePageListener != null) {
				delegatePageListener.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageSelected(int item) {
			setCurrentItem(item);	        
			if (delegatePageListener != null) {
				delegatePageListener.onPageSelected(item);
			}
		}
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		currentTabPosition = savedState.currentPosition;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPosition = currentTabPosition;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPosition;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPosition = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}