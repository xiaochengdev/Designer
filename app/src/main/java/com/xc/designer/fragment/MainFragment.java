package com.xc.designer.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.StaticPagerAdapter;
import com.jude.rollviewpager.hintview.ColorPointHintView;
import com.xc.designer.R;
import com.xc.designer.activity.VideoOpActivity;
import com.xc.designer.adapter.VideoAdapter;
import com.xc.designer.bean.Document;
import com.xc.designer.bean.Exam;
import com.xc.designer.bean.Video;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/21.
 */

public class MainFragment extends Fragment implements View.OnClickListener{
    private ProgressDialog progressDialog;
    private RollPagerView rollPagerView;
    private List<Integer> imgIdList;

    private LinearLayout introduceLayout;
    private ImageButton introduceBtn;
    private CardView introduceContent;

    private LinearLayout noticeLayout;
    private ImageButton noticeBtn;
    private CardView noticeContent;
    private TextView noticeContentText;

    private SwipeRefreshLayout refreshLayout;
    private TextView baidu;

    private Video lastVideo=new Video();
    private Document lastDocument=new Document();

    private String notice;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main,container,false);
        String address="http://192.168.191.1:8080/Designer/down_findLastFile";
        queryLastFile(address);
        initImgIdList();
        initView(view);
        initEvent();
        return view;
    }

    private void initView(View view){
        rollPagerView=(RollPagerView)view.findViewById(R.id.introduce_photo);
        rollPagerView.setPlayDelay(2000);
        rollPagerView.setAnimationDurtion(500);
        RollAdapter adapter=new RollAdapter(imgIdList);
        rollPagerView.setAdapter(adapter);

        refreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.refresh);
        baidu=(TextView)view.findViewById(R.id.baidu);

        introduceLayout=(LinearLayout)view.findViewById(R.id.introduce_content_layout);
        introduceBtn=(ImageButton) view.findViewById(R.id.introduce_content_btn);
        introduceContent=(CardView)view.findViewById(R.id.introduce_content);

        noticeLayout=(LinearLayout)view.findViewById(R.id.notice_content_layout);
        noticeBtn=(ImageButton) view.findViewById(R.id.notice_content_btn);
        noticeContent=(CardView)view.findViewById(R.id.notice_content);
        noticeContentText=(TextView)view.findViewById(R.id.notice_content_text);

       /* notice="最新视频："+lastVideo.getName()+"\n"+"最新文档："+lastDocument.getName();
        noticeContentText.setText(notice);*/
    }

    private void initEvent(){
        introduceLayout.setOnClickListener(this);
        noticeLayout.setOnClickListener(this);
        baidu.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String address="http://192.168.191.1:8080/Designer/down_findLastFile";
                queryLastFile(address);
                notice="最新视频："+lastVideo.getName()+"\n"+"最新文档："+lastDocument.getName();
                noticeContentText.setText(notice);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.introduce_content_layout:
                if (introduceContent.getVisibility()==View.GONE){
                    introduceContent.setVisibility(View.VISIBLE);
                    introduceBtn.setImageResource(R.drawable.ic_keyboard_arrow_down_black_18dp);
                }else{
                    introduceContent.setVisibility(View.GONE);
                    introduceBtn.setImageResource(R.drawable.ic_keyboard_arrow_right_black_18dp);
                }
                break;
            case R.id.notice_content_layout:
                if (noticeContent.getVisibility()==View.GONE){
                    noticeContent.setVisibility(View.VISIBLE);
                    noticeBtn.setImageResource(R.drawable.ic_keyboard_arrow_down_black_18dp);
                }else{
                    noticeContent.setVisibility(View.GONE);
                    noticeBtn.setImageResource(R.drawable.ic_keyboard_arrow_right_black_18dp);
                }
                break;
            case R.id.baidu:
                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.baidu.com"));
                startActivity(intent);
            default:
        }
    }

    public class RollAdapter extends StaticPagerAdapter{

        private List<Integer> idList;
        public RollAdapter(List<Integer> idList){
            this.idList=idList;
        }
        @Override
        public View getView(ViewGroup container, int position) {
            ImageView view = new ImageView(container.getContext());
            Integer imgId=idList.get(position);
            Glide.with(container.getContext()).load(imgId)
                    .error(R.drawable.photo12).into(view);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return view;
        }

        @Override
        public int getCount() {
            return idList.size();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String address="http://192.168.191.1:8080/Designer/down_findLastFile";
        queryLastFile(address);
        notice="最新视频："+lastVideo.getName()+"\n"+"最新文档："+lastDocument.getName();
        noticeContentText.setText(notice);
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

    private void initImgIdList(){
        imgIdList=new ArrayList<Integer>();
        imgIdList.add(R.drawable.yangda1);
        imgIdList.add(R.drawable.yangda2);
        imgIdList.add(R.drawable.yangda3);
        imgIdList.add(R.drawable.yangda4);
    }


    private void queryLastFile(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if (!TextUtils.isEmpty(responseText)){
                    try{
                        JSONObject resultObject=new JSONObject(responseText);
                        JSONObject videoJson=resultObject.getJSONObject("lastVideo");
                        JSONObject docJson=resultObject.getJSONObject("lastDoc");
                        int code=resultObject.getInt("code");

                        lastVideo.setPath(videoJson.getString("path"));
                        lastVideo.setDescr(videoJson.getString("descr"));
                        lastVideo.setName(videoJson.getString("name"));
                        lastVideo.setVid(videoJson.getInt("id"));

                        lastDocument.setPath(docJson.getString("path"));
                        lastDocument.setName(docJson.getString("name"));
                        lastDocument.setDid(docJson.getInt("id"));
                        lastDocument.setDescr(docJson.getString("descr"));

                        result=true;
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
}
