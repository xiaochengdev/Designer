package com.xc.designer.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.StaticPagerAdapter;
import com.jude.rollviewpager.hintview.ColorPointHintView;
import com.jude.rollviewpager.hintview.TextHintView;
import com.xc.designer.R;
import com.xc.designer.activity.VideoOpActivity;
import com.xc.designer.adapter.VideoAdapter;
import com.xc.designer.bean.Video;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/20.
 */

public class ChooseVideoFragment extends Fragment {
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private RollPagerView rollPagerView;
    private VideoAdapter adapter;
    private TestNormalAdapter recAdapter;
    private List<Video> dataList=new ArrayList<>();
    private List<Video> videoList;
    private List<Video> recVideos=new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean refresh=false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_video,container,false);
        recyclerView=(RecyclerView) view.findViewById(R.id.video_recycler_view);
        rollPagerView=(RollPagerView)view.findViewById(R.id.roll_pager_view);

        //设置播放时间间隔
        rollPagerView.setPlayDelay(2000);
        //设置透明度
        rollPagerView.setAnimationDurtion(500);
        //设置适配器
        recAdapter=new TestNormalAdapter(recVideos);
        rollPagerView.setAdapter(recAdapter);

        //设置指示器（顺序依次）
        //自定义指示器图片
        //设置圆点指示器颜色
        //设置文字指示器
        //隐藏指示器
        //mRollViewPager.setHintView(new IconHintView(this, R.drawable.point_focus, R.drawable.point_normal));
        rollPagerView.setHintView(new ColorPointHintView(getContext(), Color.BLUE,Color.WHITE));
        //rollPagerView.setHintView(new TextHintView(getContext()));
        //mRollViewPager.setHintView(null);

        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.video_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh=true;
                DataSupport.deleteAll(Video.class);
                queryVideos();
            }
        });
        GridLayoutManager layoutManager=new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new VideoAdapter(dataList);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private class TestNormalAdapter extends StaticPagerAdapter {
        List<Video> videos;
        public TestNormalAdapter(List<Video> videos){
            this.videos=videos;
        }

        @Override
        public View getView(ViewGroup container, final int position) {
            ImageView view = new ImageView(container.getContext());
            Video video=videos.get(position);
            String name=video.getName();
            String imgPath="http://192.168.191.1:8080/Designer/video/bitmap/"
                    +name.substring(0,name.lastIndexOf("."))+".jpg";
            Glide.with(container.getContext()).load(Uri.parse(imgPath))
                    .error(R.drawable.photo12).into(view);
            //view.setImageResource(imgs[position]);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Video video=recVideos.get(position);
                    Intent intent=new Intent(v.getContext(), VideoOpActivity.class);
                    intent.putExtra("video",video);
                    v.getContext().startActivity(intent);
                }
            });
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return view;
        }


        @Override
        public int getCount() {
            return videos.size();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryVideos();
    }
    private void queryVideos(){
        videoList= DataSupport.findAll(Video.class);
        if (videoList.size()>0){
            dataList.clear();
            recVideos.clear();
            for (Video video:videoList){
                dataList.add(video);
            }

            for (int i=(videoList.size()-4);i<videoList.size();i++){
                recVideos.add(videoList.get(i));
            }

            adapter.notifyDataSetChanged();
            recAdapter.notifyDataSetChanged();
        }else{
            String address="http://192.168.191.1:8080/Designer/down_videolist";
            queryFromServer(address,"videolist");
        }
        refresh=false;
    }
    private void queryFromServer(String address,final String type){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result= Utility.handleVideoResponse(responseText);
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryVideos();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
    private void showProgressDailog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDailog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
