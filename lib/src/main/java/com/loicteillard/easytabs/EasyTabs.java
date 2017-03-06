package com.loicteillard.easytabs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import loic.teillard.easytabs.R;

public class EasyTabs extends LinearLayout {

    public static final int SEP_DEFAULT_COLOR = Color.parseColor("#b7b7b7");

    public static final int INDICATOR_TEXT = 0;
    public static final int INDICATOR_VALUE = 1;
    public static final int INDICATOR_MATCH_PARENT = 2;

    private View mIndicator;
    private boolean mSeparatorsEnabled;
    private boolean mIndicatorsEnabled;
    private boolean mBoldForSelected;
    private ArrayList<View> mTabs;
    private int mSelectedColor, mUnselectedColor, mSeparatorColor;
    private int mSeparatorWidth, mSeparatorSize;
    private ViewPager mViewPager;

    // ---------------------------------------------------------------------------------------------------------------------

    public EasyTabs(Context context) {
        super(context);
        initialize(null);
    }

    public EasyTabs(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public EasyTabs(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private void initialize(AttributeSet attrs) {

        // Read custom values
        TypedArray attrsArray = getContext().obtainStyledAttributes(attrs, R.styleable.EasyTabsAttrs, 0, 0);
        initAttributesArray(attrsArray);
        attrsArray.recycle();

        // Prepare current viewgroup layout
        setOrientation(VERTICAL);
        LinearLayout.LayoutParams lParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(lParams);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private void populate() {
        // Prepare layout for tabs
        LinearLayout.LayoutParams lParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final LinearLayout layoutTabs = new LinearLayout(getContext());
        layoutTabs.setOrientation(HORIZONTAL);
        layoutTabs.setLayoutParams(lParams);

        // Add childs views
        for (int i = 0; i < getChildCount(); i++) {

            View view = getChildAt(i);

            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                addTab(prepareTab(textView), i);
            }
        }

        // Clear views (childs can have only one parent)
        removeAllViews();

        // Create custom stuff
        mIndicator = createIndicator();

        // Add tabs items
        for (View view : getTabs()) {
            layoutTabs.addView(view);
            if (mSeparatorsEnabled) layoutTabs.addView(createSeparator());
        }

        // Add views
        addView(layoutTabs);

        // At the end, add views to the main viewgroup
        if (mIndicatorsEnabled) addView(mIndicator);

        // Listener to change state
        getViewPager().clearOnPageChangeListeners();
        getViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switchState(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Initial state on the first item
        switchState(0);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    public ViewPager getViewPager() {
        if (mViewPager == null) throw new IllegalStateException("No ViewPager found, please add a viewpager as a child of the layout !");
        return mViewPager;
    }

    // ---------------------------------------------------------------------------------------------------------------------

    public PagerAdapter getPagerAdapter() {
        if (mViewPager == null) throw new IllegalStateException("No ViewPager found, please set one !");
        if (mViewPager.getAdapter() == null) throw new IllegalStateException("No Adapter found for this viewpager, please set one !");
        if (mViewPager.getAdapter().getCount() != getTabs().size()) throw new IllegalStateException("Adapter must have the same number of items than tabs !");
        return mViewPager.getAdapter();
    }

    // ---------------------------------------------------------------------------------------------------------------------

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        populate();
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private void initAttributesArray(TypedArray attrsArray) {

        if (attrsArray == null) return;

        mSelectedColor = attrsArray.getColor(R.styleable.EasyTabsAttrs_etab_selected_color, Color.BLACK);
        mUnselectedColor = attrsArray.getColor(R.styleable.EasyTabsAttrs_etab_unselected_color, Color.BLACK);
        mSeparatorSize = attrsArray.getInt(R.styleable.EasyTabsAttrs_etab_indicator_size, INDICATOR_TEXT);
        mSeparatorWidth = attrsArray.getDimensionPixelSize(R.styleable.EasyTabsAttrs_etab_indicator_width, 0);
        mSeparatorsEnabled = attrsArray.getBoolean(R.styleable.EasyTabsAttrs_etab_separators, false);
        mIndicatorsEnabled = attrsArray.getBoolean(R.styleable.EasyTabsAttrs_etab_indicators, true);
        mBoldForSelected = attrsArray.getBoolean(R.styleable.EasyTabsAttrs_etab_bold_for_selected, false);
        mSeparatorColor = attrsArray.getColor(R.styleable.EasyTabsAttrs_etab_separator_color, SEP_DEFAULT_COLOR);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private void switchState(int selected) {

        int selectedColor = mSelectedColor;
        int unselectedColor = mUnselectedColor;

        for (int i = 0; i < getPagerAdapter().getCount(); i++) {
            View view = getTabs().get(i);
            if (view instanceof TextView) {
                final TextView tab = (TextView) getTabs().get(i);
                if (view instanceof EasyTabTextView) {
                    EasyTabTextView easyTabTextView = (EasyTabTextView) tab;
                    if (easyTabTextView.getSelectedColor() != 0) selectedColor = easyTabTextView.getSelectedColor();
                    if (easyTabTextView.getUnselectedColor() != 0) unselectedColor = easyTabTextView.getUnselectedColor();
                }
                tab.setTextColor((i == selected) ? selectedColor : unselectedColor);
                if (i == selected) mIndicator.setBackgroundColor(selectedColor);
                tab.setTypeface(null, i == selected && mBoldForSelected ? Typeface.BOLD : Typeface.NORMAL);

                if (i == selected) {
                    tab.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (mIndicator.getMeasuredWidth() > 0)  mIndicator.animate().translationX(tab.getX()).setDuration(200);
                                    else mIndicator.setTranslationX(tab.getX());
                                    int padding = 0;
                                    int tabWidth = tab.getMeasuredWidth();
                                    switch (mSeparatorSize) {
                                        case INDICATOR_TEXT:
                                            padding = ETUtils.getTextWidth(tab);
                                            break;
                                        case INDICATOR_VALUE:
                                            padding = mSeparatorWidth;
                                            break;
                                        case INDICATOR_MATCH_PARENT:
                                            padding = tabWidth;
                                            break;
                                    }
                                    ETUtils.setDimensionLayout(mIndicator, padding, -1);
                                    ETUtils.setMarginsLayout(mIndicator, (tabWidth - padding) >> 1, -1, (tabWidth - padding) >> 1, -1);
                                }
                            }
                    );
                }
            }

        }

        getViewPager().setCurrentItem(selected, true);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private TextView prepareTab(TextView tab) {
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(0, 0, 0, ETUtils.dpToPx(5));

        LinearLayout.LayoutParams textViewParams1 = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams1.weight = 1f;
        textViewParams1.gravity = Gravity.CENTER;
        tab.setLayoutParams(textViewParams1);

        return tab;
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private View createSeparator() {

        View view = new View(getContext());
        view.setBackgroundColor(mSeparatorColor);

        LinearLayout.LayoutParams params = new LayoutParams(ETUtils.dpToPx(1), ETUtils.dpToPx(15));
        params.gravity = Gravity.RIGHT;
        view.setLayoutParams(params);

        return view;
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private View createIndicator() {
        View view = new View(getContext());
        LinearLayout.LayoutParams indicatorParams = new LayoutParams(0, ETUtils.dpToPx(3));
        indicatorParams.gravity = Gravity.TOP;
        view.setLayoutParams(indicatorParams);

        return view;
    }

    // ---------------------------------------------------------------------------------------------------------------------

    public void addTab(View view, final int index) {
        if (view == null) return;
        getTabs().add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchState(index);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------------------------------

    public ArrayList<View> getTabs() {
        if (mTabs == null) mTabs = new ArrayList<>();
        return mTabs;
    }

    // ---------------------------------------------------------------------------------------------------------------------
}
