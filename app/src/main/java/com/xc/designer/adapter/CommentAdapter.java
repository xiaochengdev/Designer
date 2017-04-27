package com.xc.designer.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xc.designer.R;
import com.xc.designer.activity.LeaveOpActivity;
import com.xc.designer.bean.AnMessage;
import com.xc.designer.bean.LeMessage;
import com.xc.designer.interfaces.Note;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Note> noteList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView noteCard;
        private TextView noteId;
        private TextView title;
        private TextView contents;
        private TextView fdate;
        private TextView username;
        private TextView userid;

        public ViewHolder(View itemView) {
            super(itemView);
            noteCard = (CardView) itemView.findViewById(R.id.note_card);
            noteId=(TextView)itemView.findViewById(R.id.note_id);
            title=(TextView)itemView.findViewById(R.id.note_title);
            contents = (TextView) itemView.findViewById(R.id.note_content);
            fdate = (TextView) itemView.findViewById(R.id.note_date);
            username = (TextView) itemView.findViewById(R.id.note_username);
            userid=(TextView)itemView.findViewById(R.id.note_userid);
        }

    }

    public CommentAdapter(List<Note> noteList) {
        this.noteList = noteList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.answer_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.noteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                Note note=noteList.get(position);
                if (note instanceof LeMessage){
                    Intent intent=new Intent(v.getContext(), LeaveOpActivity.class);
                    intent.putExtra("leMsg",(LeMessage)note);
                    v.getContext().startActivity(intent);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(CommentAdapter.ViewHolder holder, int position) {
        Note note=noteList.get(position);
        holder.noteId.setText(note.getNid()+"");
        holder.title.setText(note.getTitle());
        holder.contents.setText(note.getContents());
        holder.fdate.setText(note.getFdate());
        holder.username.setText(note.getUsername());
        holder.userid.setText(note.getUserid()+"");
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
