package com.zaaach.citypicker;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zaaach.citypicker.adapter.CityListAdapter;
import com.zaaach.citypicker.adapter.InnerListener;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.adapter.decoration.DividerItemDecoration;
import com.zaaach.citypicker.adapter.decoration.SectionItemDecoration;
import com.zaaach.citypicker.db.DBManager;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocateState;
import com.zaaach.citypicker.model.LocatedCity;
import com.zaaach.citypicker.view.SideIndexBar;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2018/8/2.
 */

public class CityPickerPopup {
    private static CityPickerPopup mCityPickerPopup;
    private PopupWindow mPopupWindow = null;
    private Context mContext;

    private View mContentView;
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private TextView mOverlayTextView;
    private SideIndexBar mIndexBar;
    private EditText mSearchBox;
    private TextView mCancelBtn;

    private LinearLayoutManager mLayoutManager;
    private CityListAdapter mAdapter;
    private List<City> mAllCities;
    private List<HotCity> mHotCities;
    private List<City> mResults;
    private ImageView mClearAllBtn;
    private DBManager dbManager;
    private ISelectCityListener mListener;
    private boolean enableAnim = false;
    private int mAnimStyle = com.zaaach.citypicker.R.style.DefaultCityPickerAnimation;
    private LocatedCity mLocatedCity;
    private int locateState;
    private OnPickListener mOnPickListener;


    private CityPickerPopup() {

    }

    public static CityPickerPopup getInstance() {
        if (mCityPickerPopup == null) {
            synchronized (CityPickerPopup.class) {
                if (mCityPickerPopup == null) {
                    mCityPickerPopup = new CityPickerPopup();
                }
            }
        }
        return mCityPickerPopup;
    }


    public void getCityPopupwindow(Context context, View view, ISelectCityListener listener) {
        this.mContext = context;
        this.mListener = listener;
        mPopupWindow = new PopupWindow();

        View contentView = LayoutInflater.from(context).inflate(R.layout.cp_dialog_city_picker, null);

        mContentView = contentView;

        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        //设置popuwindow外部可以点击
        mPopupWindow.setOutsideTouchable(true);
        //popuwindow里填充的listView拥有焦点
        mPopupWindow.setFocusable(true);
        initView();
        mPopupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.color.cp_color_grid_item_bg));
        mPopupWindow.setContentView(contentView);
        mPopupWindow.showAsDropDown(view, 0, 5);
    }

    private void initView() {
        initHotCities();

        dbManager = new DBManager(mContext);
        mAllCities = dbManager.getAllCities();
        mAllCities.add(0, mLocatedCity);
        mAllCities.add(1, new HotCity("热门城市", "未知", "0"));
        mResults = mAllCities;
        mRecyclerView = mContentView.findViewById(R.id.cp_city_recyclerview);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionItemDecoration(mContext, mAllCities), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext), 1);
        mAdapter = new CityListAdapter(mContext, mAllCities, mHotCities, locateState);
        mAdapter.setInnerListener(new InnerListener() {
            @Override
            public void dismiss(int position, City data) {

                if (mListener != null) {

                    mListener.selectedCityListener(data);


                }

            }

            @Override
            public void locate() {

            }
        });
        mAdapter.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //确保定位城市能正常刷新
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mAdapter.refreshLocationItem();
                }
            }
        });

        mEmptyView = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_empty_view);
        mOverlayTextView = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_overlay);

        mIndexBar = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_side_index_bar);
        mIndexBar.setOverlayTextView(mOverlayTextView)
                .setOnIndexChangedListener(new SideIndexBar.OnIndexTouchedChangedListener() {
                    @Override
                    public void onIndexChanged(String index, int position) {


                    }
                });

        mSearchBox = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_search_box);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mCancelBtn = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_cancel);

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

    }

    private void initHotCities() {
        if (mHotCities == null || mHotCities.isEmpty()) {
            mHotCities = new ArrayList<>();
            mHotCities.add(new HotCity("北京", "北京", "101010100"));
            mHotCities.add(new HotCity("上海", "上海", "101020100"));
            mHotCities.add(new HotCity("广州", "广东", "101280101"));
            mHotCities.add(new HotCity("深圳", "广东", "101280601"));
            mHotCities.add(new HotCity("天津", "天津", "101030100"));
            mHotCities.add(new HotCity("杭州", "浙江", "101210101"));
            mHotCities.add(new HotCity("南京", "江苏", "101190101"));
            mHotCities.add(new HotCity("成都", "四川", "101270101"));
            mHotCities.add(new HotCity("武汉", "湖北", "101200101"));
        }

    }

    //设置当前位置
    public void getCurrentLocal(String city, String province, String code) {
        mLocatedCity = new LocatedCity(city, province, code);
        locateState = LocateState.SUCCESS;

    }

    public interface ISelectCityListener {

        void selectedCityListener(City city);

    }
}
