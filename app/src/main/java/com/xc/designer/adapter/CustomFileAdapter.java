package com.xc.designer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xc.designer.R;
import com.xc.designer.bean.Document;
import com.xc.designer.bean.Video;
import com.xc.designer.interfaces.CustomFile;

import java.util.List;

/**
 * Created by Administrator on 2017/4/20.
 */

public class CustomFileAdapter extends ArrayAdapter<CustomFile>{
    private int resourceId;
    private List<CustomFile> docList;

    public CustomFileAdapter(Context context, int resource, List<CustomFile> objects) {
        super(context, resource, objects);
        resourceId=resource;
        docList=objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomFile file=getItem(position);
        View view;
        CustomFileAdapter.ViewHolder viewHolder;
        if (convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder=new CustomFileAdapter.ViewHolder();
            viewHolder.fileImg=(ImageView)view.findViewById(R.id.file_img);
            viewHolder.fileName=(TextView)view.findViewById(R.id.file_name);
            viewHolder.fileDescr=(TextView)view.findViewById(R.id.file_descr);
            view.setTag(viewHolder);
            //viewHolder.choice
        }else {
            view=convertView;
            viewHolder=(CustomFileAdapter.ViewHolder)view.getTag();
        }
        if (file instanceof Document) {
            viewHolder.fileImg.setImageResource(R.drawable.ic_action_document_blue);
        }else if (file instanceof Video){
            viewHolder.fileImg.setImageResource(R.drawable.ic_action_movie_blue);
        }
        viewHolder.fileName.setText(file.getName());
        viewHolder.fileDescr.setText(file.getDescr());
        return view;
    }

    class ViewHolder{
        ImageView fileImg;
        TextView fileName;
        TextView fileDescr;
        //CheckBox choice;
    }
}
