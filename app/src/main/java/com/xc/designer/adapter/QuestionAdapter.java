package com.xc.designer.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xc.designer.R;
import com.xc.designer.bean.Question;

import java.util.List;

/**
 * Created by Administrator on 2017/4/3.
 */

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private Context mContext;
    private List<Question> questionList;

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

    public QuestionAdapter(List<Question> questionList){
        this.questionList=questionList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }
}
