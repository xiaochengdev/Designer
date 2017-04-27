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

import java.util.List;

/**
 * Created by Administrator on 2017/4/19.
 */

public class UserDocAdapter  extends ArrayAdapter<Document> {
    private int resourceId;
    private List<Document> docList;

    public UserDocAdapter(Context context, int resource, List<Document> objects) {
        super(context, resource, objects);
        resourceId=resource;
        docList=objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Document document=getItem(position);
        View view;
        UserDocAdapter.ViewHolder viewHolder;
        if (convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder=new UserDocAdapter.ViewHolder();
            viewHolder.docImg=(ImageView)view.findViewById(R.id.doc_img);
            viewHolder.docName=(TextView)view.findViewById(R.id.doc_name);
            viewHolder.docDescr=(TextView)view.findViewById(R.id.doc_descr);
            view.setTag(viewHolder);
            //viewHolder.choice
        }else {
            view=convertView;
            viewHolder=(UserDocAdapter.ViewHolder)view.getTag();
        }
        viewHolder.docImg.setImageResource(R.drawable.ic_action_document_blue);
        viewHolder.docName.setText(document.getName());
        viewHolder.docDescr.setText(document.getDescr());
        return view;
    }

    class ViewHolder{
        ImageView docImg;
        TextView docName;
        TextView docDescr;
        //CheckBox choice;
    }
}
