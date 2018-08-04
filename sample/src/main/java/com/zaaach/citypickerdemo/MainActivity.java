package com.zaaach.citypickerdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.CityPickerPopup;
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

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, InnerListener, SideIndexBar.OnIndexTouchedChangedListener, TextWatcher, View.OnClickListener {
    private TextView currentTV;
    private CheckBox hotCB;
    private CheckBox animCB;
    private CheckBox enableCB;
    private Button themeBtn;
    private Button btnPop;

    private static final String KEY = "current_theme";

    private List<HotCity> hotCities;
    private int anim;
    private int theme;
    private boolean enable;
    private PopupWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            theme = savedInstanceState.getInt(KEY);
            setTheme(theme > 0 ? theme : R.style.DefaultCityPickerTheme);
        }

        setContentView(R.layout.activity_main);

        currentTV = findViewById(R.id.tv_current);
        hotCB = findViewById(R.id.cb_hot);
        animCB = findViewById(R.id.cb_anim);
        enableCB = findViewById(R.id.cb_enable_anim);
        themeBtn = findViewById(R.id.btn_style);
        btnPop = findViewById(R.id.btn_pop);
        if (theme == R.style.DefaultCityPickerTheme) {
            themeBtn.setText("默认主题");
        } else if (theme == R.style.CustomTheme) {
            themeBtn.setText("自定义主题");
        }

        hotCB.setOnCheckedChangeListener(this);
        animCB.setOnCheckedChangeListener(this);
        enableCB.setOnCheckedChangeListener(this);
        btnPop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置当前已经获取到的位置
                CityPickerPopup.getInstance().getCurrentLocal("","","");
                CityPickerPopup.getInstance().getCityPopupwindow(MainActivity.this, btnPop, new CityPickerPopup.ISelectCityListener() {
                    @Override
                    public void selectedCityListener(City city) {

                        Toast.makeText(MainActivity.this, ""+city.getName(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (themeBtn.getText().toString().startsWith("自定义")) {
                    themeBtn.setText("默认主题");
                    theme = R.style.DefaultCityPickerTheme;
                } else if (themeBtn.getText().toString().startsWith("默认")) {
                    themeBtn.setText("自定义主题");
                    theme = R.style.CustomTheme;
                }
                recreate();
            }
        });

        findViewById(R.id.btn_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CityPicker.getInstance()
                        .setFragmentManager(getSupportFragmentManager())
                        .enableAnimation(enable)
                        .setAnimationStyle(anim)
                        .setLocatedCity(null)
                        .setHotCities(hotCities)
                        .setOnPickListener(new OnPickListener() {
                            @Override
                            public void onPick(int position, City data) {
                                currentTV.setText(data == null ? "杭州" : String.format("当前城市：%s，%s", data.getName(), data.getCode()));
                                if (data != null) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            String.format("点击的数据：%s，%s", data.getName(), data.getCode()),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onLocate() {
                                //开始定位，这里模拟一下定位
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        CityPicker.getInstance().locateComplete(new LocatedCity("深圳", "广东", "101280601"), LocateState.SUCCESS);
                                    }
                                }, 3000);
                            }
                        })
                        .show();
            }
        });
    }

    private void showPopWindow() {
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.cp_dialog_city_picker, null);

        float y = btnPop.getY();

        int h = btnPop.getHeight();


        WindowManager windowManager = this.getWindowManager();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int height1 = defaultDisplay.getHeight();
        Log.d("yikai", "y = " + y + "  h = " + h + " height1=" + height1);
        int hight = (int) (height1 - y - h - 500);
        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, hight, true);
        //设置popuwindow外部可以点击
        mPopupWindow.setOutsideTouchable(true);
        //popuwindow里填充的listView拥有焦点
        mPopupWindow.setFocusable(true);
        initView(contentView);
        initData(contentView);
        mPopupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.color.cp_color_grid_item_bg));
        mPopupWindow.setContentView(contentView);
        //popupWindow.showAtLocation(btnPop, Gravity.BOTTOM, 0, 100);
        mPopupWindow.showAsDropDown(btnPop, 0, 5);

    }

    private View mContentView;
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private TextView mOverlayTextView;
    private SideIndexBar mIndexBar;
    private EditText mSearchBox;
    private TextView mCancelBtn;
    private ImageView mClearAllBtn;

    private LinearLayoutManager mLayoutManager;
    private CityListAdapter mAdapter;
    private List<City> mAllCities;
    private List<HotCity> mHotCities;
    private List<City> mResults;

    private DBManager dbManager;

    private boolean enableAnim = false;
    private int mAnimStyle = com.zaaach.citypicker.R.style.DefaultCityPickerAnimation;
    private LocatedCity mLocatedCity;
    private int locateState;
    private OnPickListener mOnPickListener;

    private void initView(View mContentView) {
        initHotCities();
        initLocatedCity();

        dbManager = new DBManager(this);
        mAllCities = dbManager.getAllCities();
        mAllCities.add(0, mLocatedCity);
        mAllCities.add(1, new HotCity("热门城市", "未知", "0"));
        mResults = mAllCities;
        mRecyclerView = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_city_recyclerview);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionItemDecoration(this, mAllCities), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this), 1);
        mAdapter = new CityListAdapter(this, mAllCities, mHotCities, locateState);
        mAdapter.setInnerListener(this);
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
                .setOnIndexChangedListener(this);

        mSearchBox = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_search_box);
        mSearchBox.addTextChangedListener(this);

        mCancelBtn = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_cancel);
              mClearAllBtn = mContentView.findViewById(com.zaaach.citypicker.R.id.cp_clear_all);
        mCancelBtn.setOnClickListener(this);
             mClearAllBtn.setOnClickListener(this);


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

    private void initLocatedCity() {
        if (mLocatedCity == null) {
            mLocatedCity = new LocatedCity(getString(com.zaaach.citypicker.R.string.cp_locating), "未知", "0");
            locateState = LocateState.FAILURE;
        } else {
            locateState = LocateState.SUCCESS;
        }
    }

    private void initData(View contentView) {

    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_hot:
                if (isChecked) {
                    hotCities = new ArrayList<>();
                    hotCities.add(new HotCity("北京", "北京", "101010100"));
                    hotCities.add(new HotCity("上海", "上海", "101020100"));
                    hotCities.add(new HotCity("广州", "广东", "101280101"));
                    hotCities.add(new HotCity("深圳", "广东", "101280601"));
                    hotCities.add(new HotCity("杭州", "浙江", "101210101"));
                } else {
                    hotCities = null;
                }
                break;
            case R.id.cb_anim:
                anim = isChecked ? R.style.CustomAnim : R.style.DefaultCityPickerAnimation;
                break;
            case R.id.cb_enable_anim:
                enable = isChecked;
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY, theme);
    }

    @Override
    public void dismiss(int position, City data) {

        Toast.makeText(this, "" + data.getName(), Toast.LENGTH_LONG).show();
        mPopupWindow.dismiss();
    }

    @Override
    public void locate() {

    }

    @Override
    public void onIndexChanged(String index, int position) {
        //滚动RecyclerView到索引位置
        mAdapter.scrollToSection(index);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String keyword = s.toString();
        if (TextUtils.isEmpty(keyword)) {
            mClearAllBtn.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
            mResults = mAllCities;
            ((SectionItemDecoration) (mRecyclerView.getItemDecorationAt(0))).setData(mResults);
            mAdapter.updateData(mResults);
        } else {
            mClearAllBtn.setVisibility(View.VISIBLE);
            //开始数据库查找
            mResults = dbManager.searchCity(keyword);
            ((SectionItemDecoration) (mRecyclerView.getItemDecorationAt(0))).setData(mResults);
            if (mResults == null || mResults.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mAdapter.updateData(mResults);
            }
        }
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == com.zaaach.citypicker.R.id.cp_cancel) {
            dismiss(-1, null);
        }
    }



}
