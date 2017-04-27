package com.xc.designer.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.adapter.UserVideoAdapter;
import com.xc.designer.bean.User;
import com.xc.designer.bean.Video;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/28.
 */

public class UserVideoActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private List<Video> videoList;
    private List<Video> dataList=new ArrayList<>();
    private User user;
    private UserVideoAdapter adapter;

    private ListView listView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uservideo);
        user=(User) Utility.getUserInfo(this);

        listView=(ListView)findViewById(R.id.user_videolist);
        adapter=new UserVideoAdapter(UserVideoActivity.this,R.layout.item_uservideo,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Video video=videoList.get(position);
                Intent intent=new Intent(UserVideoActivity.this,VideoOpActivity.class);
                intent.putExtra("video",video);
                startActivity(intent);
            }
        });
        queryUserVideo();
    }

    private List<Video> queryVideoByVids(long[] ids){
        List<Video> list=new ArrayList<Video>();
        /*String sql="select * from Video where vid in (";
        for (int i=0;i<ids.length;i++){
            if (i==ids.length-1){
                sql=sql+ids[i]+")";
            }else{
                sql=sql+ids[i]+",";
            }
        }
        Cursor cursor=DataSupport.findBySQL(sql);
        if (cursor.moveToFirst()){
            do{
                Video video=new Video();
                int vid=cursor.getInt(cursor.getColumnIndex("vid"));
                String name=cursor.getString(cursor.getColumnIndex("name"));
                String descr=cursor.getString(cursor.getColumnIndex("descr"));
                String path=cursor.getString(cursor.getColumnIndex("path"));
                video.setVid(vid);
                video.setName(name);
                video.setDescr(descr);
                video.setPath(path);
                list.add(video);
            }while (cursor.moveToNext());
        }
        cursor.close();*/
        for (int i=0;i<ids.length;i++){
            List<Video> data=DataSupport.where("vid=?",String.valueOf(ids[i])).find(Video.class);
            if (data!=null) {
                list.add(data.get(0));
            }else {
                String address = "http://192.168.191.1:8080/Designer/down_findVideosByIds?ids=" + ids[i];
                queryUserVideoFromServer(address);
            }
        }
        return list;
    }

    private void queryUserVideo(){
        String idsStr=user.getVideos().replace(" ","");
        String[] idStr=idsStr.split(",");
        if (idsStr.length()==0){
            return;
        }
        long[] ids=getVideosId(idsStr);
        videoList=queryVideoByVids(ids);
        if (videoList.size()>0){
            dataList.clear();
            for (Video video:videoList){
                dataList.add(video);
            }
            adapter.notifyDataSetChanged();
        }else{
            String address="http://192.168.191.1:8080/Designer/down_findVideosByIds?ids="+idsStr;
            queryUserVideoFromServer(address);
        }
    }

    private void queryUserVideoFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(UserVideoActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result= Utility.handleVideoResponse(responseText);
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryUserVideo();
                        }
                    });
                }
            }
        });
    }

    private void showProgressDailog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(UserVideoActivity.this);
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

    private long[] getVideosId(String videoId){
        //获取视频id数组
        String[] idStr=videoId.split(",");
        long[] videoIds=new long[idStr.length];
        for (int i=0;i<idStr.length;i++){
            videoIds[i]=Integer.valueOf(idStr[i].trim());
        }
        return videoIds;
    }
}
