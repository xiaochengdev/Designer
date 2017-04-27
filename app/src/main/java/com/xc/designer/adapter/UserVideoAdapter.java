package com.xc.designer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.xc.designer.R;
import com.xc.designer.bean.Video;

import java.util.List;

/**
 * Created by Administrator on 2017/4/18.
 */

public class UserVideoAdapter extends ArrayAdapter<Video> {
    private int resourceId;
    private List<Video> videoList;

    public UserVideoAdapter(Context context, int resource, List<Video> objects) {
        super(context, resource, objects);
        resourceId=resource;
        videoList=objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Video video=getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder=new ViewHolder();
            viewHolder.videoImg=(ImageView)view.findViewById(R.id.video_img);
            viewHolder.videoName=(TextView)view.findViewById(R.id.video_name);
            viewHolder.videoDescr=(TextView)view.findViewById(R.id.video_descr);
            view.setTag(viewHolder);
            //viewHolder.choice
        }else {
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.videoImg.setImageResource(R.drawable.ic_action_movie_blue);
        viewHolder.videoName.setText(video.getName());
        viewHolder.videoDescr.setText(video.getDescr());
        return view;
    }

    class ViewHolder{
        ImageView videoImg;
        TextView videoName;
        TextView videoDescr;
        //CheckBox choice;
    }
}
