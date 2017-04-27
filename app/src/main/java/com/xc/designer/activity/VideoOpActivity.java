package com.xc.designer.activity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.xc.designer.R;
import com.xc.designer.adapter.CommentAdapter;
import com.xc.designer.bean.AnMessage;
import com.xc.designer.bean.User;
import com.xc.designer.bean.Video;
import com.xc.designer.customView.TitleLayout;
import com.xc.designer.interfaces.Note;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/21.
 */

public class VideoOpActivity extends AppCompatActivity implements View.OnClickListener {

    private static boolean FULL_SCREEN=false;
    private ProgressDialog progressDialog;
    private RelativeLayout playLayout;
    private RelativeLayout buttonLayout;

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView listView;

    private VideoView videoView;
    private TextView videoName;
    private TextView videoDescr;
    private ImageButton download;
    private ImageButton addLike;
    private ImageButton videoComment;
    private ImageButton maxView;
    private ImageButton oldView;
    private TitleLayout toolbar;

    //对视频进行评论
    private RelativeLayout rlComment;
    private TextView hideDown;
    private EditText comment_contents;
    private Button send;
    private Video video;
    private User user;

    private int mHeight;
    private int mWidth;
    private int playLayoutHeight;
    private int playLayoutWidth;

    private List<AnMessage> ansMsgList;
    private List<Note> dataList=new ArrayList<Note>();
    private CommentAdapter adapter;
    private int current=0;
    private int added=0;

    private List<String> uservideos=new ArrayList<String>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        video = (Video) data.get("video");
        initView();
        initEvent();
        user=Utility.getUserInfo(getApplicationContext());
        initUserVideos();
        if (uservideos.contains(video.getVid()+"")){
            addLike.setImageResource(R.drawable.ic_action_heart_red);
        }

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        adapter=new CommentAdapter(dataList);
        listView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataSupport.deleteAll(AnMessage.class,"videoid=?",video.getVid()+"");
                queryAnsMsgByVideoId();
            }
        });
        queryAnsMsgByVideoId();



        if (getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            toolbar.setVisibility(View.GONE);
            maxView.setVisibility(View.GONE);
            oldView.setVisibility(View.VISIBLE);
            videoComment.setVisibility(View.GONE);
            videoName.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            RelativeLayout.LayoutParams params=new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            playLayout.setLayoutParams(params);
            RelativeLayout.LayoutParams videoparams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            videoparams.addRule(RelativeLayout.CENTER_IN_PARENT);
            videoView.setLayoutParams(videoparams);
        }
    }

    private void initView() {
        playLayout = (RelativeLayout) findViewById(R.id.player_layout);
        buttonLayout=(RelativeLayout)findViewById(R.id.video_button_layout);


        listView=(RecyclerView)findViewById(R.id.ansmsg_list);
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.ansmsg_refresh);

        download = (ImageButton) findViewById(R.id.video_download);
        addLike=(ImageButton)findViewById(R.id.video_addlike);
        videoComment=(ImageButton)findViewById(R.id.video_comment);
        maxView=(ImageButton)findViewById(R.id.max_view);
        oldView=(ImageButton)findViewById(R.id.old_view);
        toolbar=(TitleLayout)findViewById(R.id.toolbar);

        rlComment=(RelativeLayout)findViewById(R.id.rl_comment);
        hideDown=(TextView)findViewById(R.id.hide_down);
        comment_contents=(EditText)findViewById(R.id.comment_content);
        send=(Button)findViewById(R.id.comment_send);

        videoName=(TextView)findViewById(R.id.video_name);
        String name=video.getName().substring(0,video.getName().lastIndexOf("."));
        videoName.setText(name);
        videoDescr = (TextView) findViewById(R.id.video_play_descr);
        videoDescr.setText(video.getDescr());
        videoView = (VideoView) findViewById(R.id.video_play_view);
        Uri uri=Uri.parse("http://192.168.191.1:8080/Designer/video/" + video.getName());
        MediaController mediaController = new MediaController(this);


        mediaController.setMinimumHeight(1);
        videoView.setVideoURI(uri);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
    }

    private void initEvent(){
        download.setOnClickListener(this);
        addLike.setOnClickListener(this);
        videoComment.setOnClickListener(this);
        maxView.setOnClickListener(this);
        oldView.setOnClickListener(this);

        send.setOnClickListener(this);
        hideDown.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_download:
                videoDownload("http://192.168.191.1:8080/Designer/down_videoDown?file_name="+video.getName());
                break;
            case R.id.video_addlike:
                added++;
                addLike();
                break;
            case R.id.video_comment:
                rlComment.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.comment_send:
                String contents=comment_contents.getText().toString();
                Date date=new Date();
                AnMessage anMessage=new AnMessage();
                anMessage.setVideoid(video.getVid());
                anMessage.setFdate(date.toString());
                anMessage.setUsername(user.getName());
                anMessage.setUserid(user.getUid());
                anMessage.setContents(contents);
                DataSupport.deleteAll(AnMessage.class,"videoid=?",video.getVid()+"");
                sendAns(anMessage);
                break;
            case R.id.hide_down:
                rlComment.setVisibility(View.GONE);
                break;
            case R.id.max_view:
                fullScreen();
                break;
            case R.id.old_view:
                if (getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                    videoView.resume();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    /*RelativeLayout.LayoutParams oldLayout=new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    playLayout.setLayoutParams(oldLayout);
                    oldLayout.height=mHeight;
                    videoView.setLayoutParams(oldLayout);*/

                    maxView.setVisibility(View.VISIBLE);
                    oldView.setVisibility(View.GONE);


                }
                break;
            default:
        }
    }

    public void videoDownload(String address) {
        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(address));
//指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("/download/", video.getName());
//获取下载管理器
        DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
//将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);
    }

    private void initUserVideos(){
        String userviderStr=user.getVideos()==null?"":user.getVideos();
        String[] videos;
        if (!userviderStr.equals("")){
            videos=userviderStr.split(",");
            for (int i=0;i<videos.length;i++){
                uservideos.add(videos[i].trim());
            }
        }
    }

    public void addLike(){
        //String userviderStr=user.getVideos()==null?"":user.getVideos();


        String videoid=(video.getVid()+"").trim();

        if (uservideos.contains(videoid)||added%2==0){
            //Toast.makeText(VideoOpActivity.this,"已存在收藏夹中",Toast.LENGTH_SHORT).show();
            uservideos.remove(videoid);
            String videostr = uservideos.toString();
            videostr = videostr.substring(1, videostr.length() - 1).trim();
            videostr=videostr.replace(" ","");
            user.setVideos(videostr);
            addVideoToLike("delete");
        }else {
            uservideos.add(video.getVid() + "");
            String videostr = uservideos.toString();
            videostr = videostr.substring(1, videostr.length() - 1).trim();
            videostr=videostr.replace(" ","");
            user.setVideos(videostr);
            addVideoToLike("add");
        }
    }

    private void showProgressDailog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDailog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void fullScreen(){
        mWidth=videoView.getWidth();
        mHeight=videoView.getHeight();
        playLayoutWidth=playLayout.getWidth();
        playLayoutHeight=playLayout.getHeight();


        if (getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void queryAnsMsgByVideoId(){
        Integer videoid=video.getVid();
        ansMsgList= DataSupport.where("videoid=?",videoid+"").find(AnMessage.class);
        if (ansMsgList.size()>0||current>0){
            current=0;
            dataList.clear();
            for (AnMessage anMessage:ansMsgList){
                dataList.add(anMessage);
            }
            adapter.notifyDataSetChanged();
            //refreshLayout.setRefreshing(false);
        }else{
            current++;
            String address="http://192.168.191.1:8080/Designer/msg_findAnswerMsgByVidoId?id="+videoid;
            queryFromServer(address);
        }
    }
    private void queryFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(VideoOpActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result = Utility.handleAnMessageResponse(responseText);
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryAnsMsgByVideoId();
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
    private void sendAns(final AnMessage anMessage){
        String address="http://192.168.191.1:8080/Designer/msg_answerToVideo";
        RequestBody requestBody=new FormBody.Builder()
                .add("answerMessage.contents",anMessage.getContents())
                .add("answerMessage.fdate",anMessage.getFdate())
                .add("userid",anMessage.getUserid().toString())
                .add("answerMessage.username",anMessage.getUsername())
                .add("id",anMessage.getVideoid().toString()).build();
        showProgressDailog();
        HttpUtil.sendOkHttpRequestWithParam(address,requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(VideoOpActivity.this,"发表失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                try {
                    JSONObject json = new JSONObject(responseText);
                    int code=json.getInt("code");
                    if (code>0){
                        result=true;
                    }
                }catch (JSONException e){

                }

                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            Toast.makeText(VideoOpActivity.this,"回复成功",Toast.LENGTH_SHORT).show();
                            queryAnsMsgByVideoId();
                            refreshLayout.setRefreshing(false);
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else{
                    Toast.makeText(VideoOpActivity.this,"回复失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addVideoToLike(final String type){

        String address="http://192.168.191.1:8080/Designer/user_addUserVideo";
        RequestBody requestBody=new FormBody.Builder()
                .add("id",user.getUid()+"")
                .add("user.videos",user.getVideos())
                .build();
        showProgressDailog();
        HttpUtil.sendOkHttpRequestWithParam(address,requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(VideoOpActivity.this,"操作失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                try {
                    JSONObject json = new JSONObject(responseText);
                    int code=json.getInt("code");
                    if (code>0){
                        result=true;
                    }
                }catch (JSONException e){

                }

                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            String msg="操作成功";
                            if (type.equals("add")){
                                msg="添加成功";
                                addLike.setImageResource(R.drawable.ic_action_heart_red);
                            }else if(type.equals("delete")){
                                msg="删除成功";
                                addLike.setImageResource(R.drawable.ic_action_heart);
                            }
                            Toast.makeText(VideoOpActivity.this,msg,Toast.LENGTH_SHORT).show();
                            Utility.saveUser(VideoOpActivity.this,user);
                            queryAnsMsgByVideoId();
                            refreshLayout.setRefreshing(false);
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else{
                    Toast.makeText(VideoOpActivity.this,"操作失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
