package com.xc.designer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.*;
import android.provider.*;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.xc.designer.R;
import com.xc.designer.activity.VideoOpActivity;
import com.xc.designer.bean.Video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.bitmap;
import static android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT;

/**
 * Created by Administrator on 2017/3/20.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder>{
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;
    //private static final String bitmapUrl="/bitmap/";
    private Context mContext;
    private List<Video> mVideoList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView videoImage;
        TextView videoName;
        public ViewHolder(View itemView) {
            super(itemView);
            cardView=(CardView)itemView;
            videoImage=(ImageView)itemView.findViewById(R.id.video_image);
            videoName=(TextView)itemView.findViewById(R.id.video_name);
        }
    }

    public VideoAdapter(List<Video> videoList){
        mVideoList=videoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.video_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                Video video=mVideoList.get(position);

                Intent intent=new Intent(v.getContext(), VideoOpActivity.class);
                intent.putExtra("video",video);
                v.getContext().startActivity(intent);
            }
        });
        holder.videoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                Video video=mVideoList.get(position);
                Intent intent=new Intent(v.getContext(), VideoOpActivity.class);
                intent.putExtra("video",video);
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Video video=mVideoList.get(position);
        holder.videoName.setText(video.getName());
        String name=video.getName();
        String videoPath="http://192.168.191.1:8080/Designer/video/"+name;
        String imgPath="http://192.168.191.1:8080/Designer/video/bitmap/"
                +name.substring(0,name.lastIndexOf("."))+".jpg";
        Glide.with(mContext).load(Uri.parse(imgPath))
                .error(R.drawable.ic_action_emo_shame_black).
                into(holder.videoImage);
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

}
