package com.zhoufeng.tabscrollview;

import android.annotation.SuppressLint;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zhoufeng.tabscrollview.widget.MoreGridView;
import com.zhoufeng.tabscrollview.widget.ObservableScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @author fly
 */
public class MainActivity extends AppCompatActivity implements ObservableScrollView.ScrollViewListener{

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.wrapperFl)
    FrameLayout wrapperFl;
    @BindView(R.id.containerLl)
    LinearLayout containerLl;
    @BindView(R.id.scrollView)
    ObservableScrollView scrollView;

    private boolean firstAlreadyInflated = true;
    private int currentPosition = 0;
    //标志位，用来区分是点击了tab还是手动滑动scrollview
    private boolean tabInterceptTouchEventTag = true;

    private List<Integer> dintances;
    private List<ViewGroup> menuViewGroups;
    private String[] titles = {"热门推荐", "运动健康", "教育培训", "丽人时尚", "休闲娱乐"};
    private int[] menuNums = {5, 9, 2, 15, 6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initViews();
        initListeners();

    }


    private void initViews() {
        if(titles.length > 4){
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        menuViewGroups = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(titles[i]));
            ViewGroup viewGroup = createViewGroup(i);
            menuViewGroups.add(viewGroup);
            containerLl.addView(viewGroup);
        }
    }

    private ViewGroup createViewGroup(int index){
        ViewGroup viewGroup;
        if (titles.length - 1 == index) {
            viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.menu_list_grid_bottom, null);
        } else {
            viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.menu_list_grid, null);
        }
        MoreGridView gridView = viewGroup.findViewById(R.id.gridview);
        TextView textView = viewGroup.findViewById(R.id.menu_title);
        textView.setText(titles[index]);
        String menus[] = new String[menuNums[index]];
        int images[] = new int[menuNums[index]];
        for (int j = 0; j < menuNums[index]; j++) {
            menus[j] = (index + 1) + "菜单" + (j + 1);
            images[j] = R.mipmap.ic_launcher;
        }
        String[] from = {"img", "text"};
        int[] to = {R.id.item_list_icon, R.id.item_list_name};
        SimpleAdapter adapter = new SimpleAdapter(this, getData(menus), R.layout.menu_second_item, from, to);
        gridView.setAdapter(adapter);
        return viewGroup;
    }


    private List<HashMap<String, Object>> getData(String menus[]) {
        List dataList = new ArrayList<HashMap<String, Object>>();
        for (String menu : menus) {
            HashMap<String, Object> map = new HashMap<>(2);
            map.put("img", R.drawable.shoucang_liwu_liaotian);
            map.put("text", menu);
            dataList.add(map);
        }
        return dataList;
    }




    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //获取各层离screen顶部的位置以及计算滑动值相应顶部所需要的距离
        if (firstAlreadyInflated) {
            firstAlreadyInflated = false;

            dintances = new ArrayList<>();

            //先算出第一个position
            int[] firstVgPostion = new int[2];
            menuViewGroups.get(0).getLocationOnScreen(firstVgPostion);
            int firstFloorVgPositionAnchor = firstVgPostion[1];
            dintances.add(0);
            for (int i = 1; i < titles.length; i++) {
                int[] vgPostion = new int[2];
                menuViewGroups.get(i).getLocationOnScreen(vgPostion);
                int currentPosition = vgPostion[1];
                dintances.add(currentPosition - firstFloorVgPositionAnchor);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        wrapperFl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //让tab来处理滑动
                tabInterceptTouchEventTag = true;
                return false;
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentPosition = tab.getPosition();
                //手动滑动页面时则不再次处理滑动
                if (!tabInterceptTouchEventTag) {
                    return;
                }
                scrollView.computeScroll();

                for (int i = 0; i < dintances.size(); i++) {
                    if (currentPosition == i) {
                        scrollView.smoothScrollTo(0, dintances.get(i));
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        scrollView.setScrollViewListener(this);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //让scrollview处理滑动
                tabInterceptTouchEventTag = false;
                return false;
            }
        });
    }

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        //让tab来处理滑动
        if (tabInterceptTouchEventTag) {
            return;
        }
        int index = 0;
        for (int i = 1; i < dintances.size(); i++) {
            if (y < dintances.get(i)) {
                index = i - 1;
                break;
            }
        }
        if (y < dintances.get(dintances.size() - 1)) {
            if (currentPosition != index) {
                doScroll(scrollView);
                tabLayout.getTabAt(index).select();
            }
        } else {
            if (currentPosition != dintances.size() - 1) {
                doScroll(scrollView);
                tabLayout.getTabAt(dintances.size() - 1).select();
            }
        }

    }

    /**
     * 防止内存溢出
     * @param scrollview
     */
    public void doScroll(final View scrollview) {
        scrollview.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollview.computeScroll();
            }
        }, 150);
    }
}
