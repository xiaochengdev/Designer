package com.xc.designer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.xc.designer.activity.LeMsgAddActivity;
import com.xc.designer.activity.LoginActivity;
import com.xc.designer.activity.QueryResultActivity;
import com.xc.designer.activity.UserDocActivity;
import com.xc.designer.activity.UserVideoActivity;
import com.xc.designer.activity.UserinfoActivity;
import com.xc.designer.bean.LeMessage;
import com.xc.designer.bean.User;
import com.xc.designer.util.Utility;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    private RelativeLayout headerLayout;
    public static User user;
    private String recordsStr;
    private List<String> recordList=new ArrayList<>();

    private NavigationView navView;
    private View headerView;
    private TextView username;
    private TextView useremail;

    private SearchView searchView;
    private ListView searchList;
    private ArrayAdapter<String> recadapter;

    private ViewPager viewPager;
    private PagerAdapter adapter;
    private List<View> mViews = new ArrayList<View>();

    private LinearLayout main_view;
    private LinearLayout video_view;
    private LinearLayout document_view;
    private LinearLayout exam_view;
    private LinearLayout comment_view;

    private ImageButton main_view_btn;
    private ImageButton video_view_btn;
    private ImageButton document_view_btn;
    private ImageButton exam_view_btn;
    private ImageButton comment_view_btn;

    private FloatingActionButton lefloatButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        refresh();

        initView();
        initEvent();
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_database);
        }
        navView.setCheckedItem(R.id.home);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Snackbar.make(getCurrentFocus(),item.getTitle() + " pressed", Snackbar.LENGTH_LONG).show();
                mDrawerLayout.closeDrawers();
                Intent intent=null;
                switch (item.getItemId()){
                    case R.id.home:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.user_info:
                       intent=new Intent(MainActivity.this, UserinfoActivity.class);
                        intent.putExtra("user",user);
                        startActivity(intent);
                        break;
                    case R.id.user_movie:
                        intent = new Intent(MainActivity.this, UserVideoActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.user_document:
                        intent = new Intent(MainActivity.this, UserDocActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.logo_out:
                        intent=new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    default:
                }
                return true;
            }
        });

        //获取用户信息
        Intent intent=getIntent();
        Bundle data=intent.getExtras();
        user= (User) data.get("user");
        if(user!=null) {
            username.setText(user.getName());
            useremail.setText(user.getEmail());
            //saveUser();
            Utility.saveUser(this,user);
        }
    }

    private void initView(){
        //获取DrawerLayout中的控件
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navView=(NavigationView)findViewById(R.id.nav_view);
        headerView=navView.getHeaderView(0);
        username= (TextView) headerView.findViewById(R.id.user_name);
        useremail=(TextView)headerView.findViewById(R.id.user_mail);

        //搜索框
        searchView=(SearchView)findViewById(R.id.search);
        searchView.setFocusableInTouchMode(true);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("请输入查找内容");
        searchList=(ListView)findViewById(R.id.search_list);

        viewPager = (ViewPager)findViewById(R.id.viewpager);

        //获取tab栏的LinearLayout控件
        main_view = (LinearLayout)findViewById(R.id.main_view);
        video_view = (LinearLayout)findViewById(R.id.video_view);
        document_view = (LinearLayout)findViewById(R.id.document_view);
        exam_view = (LinearLayout)findViewById(R.id.exam_view);
        comment_view=(LinearLayout)findViewById(R.id.comment_view);

        //获取tab栏的ImageButton控件
        main_view_btn = (ImageButton)findViewById(R.id.main_view_btn);
        video_view_btn = (ImageButton)findViewById(R.id.video_view_btn);
        document_view_btn = (ImageButton)findViewById(R.id.document_view_btn);
        exam_view_btn = (ImageButton)findViewById(R.id.exam_view_btn);
        comment_view_btn=(ImageButton)findViewById(R.id.comment_view_btn);

        //加载布局
        LayoutInflater inflater = LayoutInflater.from(this);
        View tab01 = inflater.inflate(R.layout.activity_company_desc, null);
        View tab02 = inflater.inflate(R.layout.activity_video_list, null);
        View tab03 = inflater.inflate(R.layout.activity_document_list, null);
        View tab04 = inflater.inflate(R.layout.activity_exam_list, null);
        View tab05 = inflater.inflate(R.layout.activity_leavemsg_list, null);


        lefloatButton=(FloatingActionButton)tab05.findViewById(R.id.lemsg_float_button);


        //添加到UI界面列表中
        mViews.add(tab01);
        mViews.add(tab02);
        mViews.add(tab03);
        mViews.add(tab04);
        mViews.add(tab05);

        //创建ViewPager的 PageAdapter
        adapter = new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                // 销毁View
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // 初始化View
                View view = mViews.get(position);
                container.addView(view);
                return view;
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return mViews.size();
            }
        };
        viewPager.setAdapter(adapter);

        initRecord();
        recadapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,recordList);
        searchList.setAdapter(recadapter);
        searchList.setTextFilterEnabled(true);
    }

    private void initRecord(){
        recordsStr=Utility.getSearchRecord(this);
        String[] records=recordsStr.split(",");
        for (int i=0;i<records.length;i++){
            if (!records[i].equals("")){
                recordList.add(records[i]);
            }
        }
    }


    private void initEvent() {
        // 设置事件
        main_view_btn.setOnClickListener(this);
        video_view_btn.setOnClickListener(this);
        document_view_btn.setOnClickListener(this);
        exam_view_btn.setOnClickListener(this);
        comment_view_btn.setOnClickListener(this);
        lefloatButton.setOnClickListener(this);

        //实现滑动切换的动画效果
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //ViewPager 改变时改变图标的颜色
            @Override
            public void onPageSelected(int arg0) {
                int currentItem = viewPager.getCurrentItem();
                resetImg();
                switch (currentItem) {
                    case 0:
                        main_view_btn.setImageResource(R.drawable.ic_action_home_red);
                        break;
                    case 1:
                        video_view_btn.setImageResource(R.drawable.ic_action_movie_red);
                        break;
                    case 2:
                         document_view_btn.setImageResource(R.drawable.ic_action_document_red);
                        break;
                    case 3:
                        exam_view_btn.setImageResource(R.drawable.ic_action_database_red);
                        break;
                    case 4:
                        comment_view_btn.setImageResource(R.drawable.ic_action_monolog_red);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(MainActivity.this,query,Toast.LENGTH_SHORT).show();
                searchList.setVisibility(View.GONE);
                if (!recordList.contains(query)){
                    recordsStr=recordsStr+","+query;
                    Utility.saveSearchRecord(MainActivity.this,recordsStr);
                    initRecord();
                    recadapter.notifyDataSetChanged();
                }
                queryByCondititon(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    recordsStr=Utility.getSearchRecord(MainActivity.this);
                    String[] records=recordsStr.split(",");
                    recadapter.notifyDataSetChanged();
                    recadapter.getFilter().filter(newText);
                    searchList.setVisibility(View.VISIBLE);

                }else{
                    searchList.clearTextFilter();
                    searchList.setVisibility(View.GONE);
                }
                return false;
            }

        });

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected=searchList.getAdapter().getItem(position).toString();
                searchView.setQuery(selected,true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.logo_out:
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
        }
        return true;
    }

    private void resetImg(){
        main_view_btn.setImageResource(R.drawable.ic_action_home);
        video_view_btn.setImageResource(R.drawable.ic_action_movie);
        document_view_btn.setImageResource(R.drawable.ic_action_document);
        exam_view_btn.setImageResource(R.drawable.ic_action_database);
        comment_view_btn.setImageResource(R.drawable.ic_action_monolog_white);
    }
    @Override
    public void onClick(View v) {
        //实现点击按钮切换的效果
        switch (v.getId()) {
            case R.id.main_view_btn:

                viewPager.setCurrentItem(0);
                main_view_btn.setImageResource(R.drawable.ic_action_home_red);
                break;
            case R.id.video_view_btn:

                viewPager.setCurrentItem(1);
                video_view_btn.setImageResource(R.drawable.ic_action_movie_red);
                break;
            case R.id.document_view_btn:
                viewPager.setCurrentItem(2);
                document_view_btn.setImageResource(R.drawable.ic_action_document_red);
                break;
            case R.id.exam_view_btn:
                viewPager.setCurrentItem(3);
                exam_view_btn.setImageResource(R.drawable.ic_action_database_red);
                break;
            case R.id.comment_view_btn:
                viewPager.setCurrentItem(4);
                comment_view_btn.setImageResource(R.drawable.ic_action_monolog_red);
                break;
            case R.id.lemsg_float_button:
                Intent intent=new Intent(MainActivity.this, LeMsgAddActivity.class);
                intent.putExtra("user",user);
                startActivityForResult(intent,1);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK){
                    refresh();
                }
                break;
            default:
        }
    }

    private void refresh(){
        DataSupport.deleteAll(LeMessage.class);
    }

    public void queryByCondititon(String condition){
        Intent intent=new Intent(MainActivity.this, QueryResultActivity.class);
        intent.putExtra("condition",condition);
        startActivity(intent);
    }
}
