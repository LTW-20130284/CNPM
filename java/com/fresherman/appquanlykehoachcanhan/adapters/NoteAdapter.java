package com.fresherman.appquanlykehoachcanhan.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fresherman.appquanlykehoachcanhan.R;
import com.fresherman.appquanlykehoachcanhan.entities.Note;
import com.fresherman.appquanlykehoachcanhan.listeners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{
    // Danh sách các Note hiển thị trên RecyclerView
    private List<Note> notes;
    private NoteListener notesListener;
    // // Timer để thực hiện tìm kiếm theo keyword được nhập vào từ phía người dùng
    private Timer timer;
    private List<Note> notesSource;
    public NoteAdapter(List<Note> notes, NoteListener notesListener){

        this.notes= notes;
        this.notesListener= notesListener;
        notesSource=notes;
    }
    // Tạo ViewHolder cho RecyclerView, tạo View từ file XML item_container_note
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_note,parent,false));
    }
    // Thiết lập các giá trị hiển thị của ViewHolder tại vị trí position
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position), position);
            }
        });
    }
    // Trả về số lượng item hiển thị trên RecyclerView
    @Override
    public int getItemCount() {
        return notes.size();
    }
    // Trả về viewType tương ứng với vị trí position
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    // ViewHolder của Adapter
    static class  NoteViewHolder extends RecyclerView.ViewHolder{

        TextView textTitle, textSubtitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        // Constructor của ViewHolder, tìm View và ánh xạ các thuộc tính của Note tương ứng với View
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle= itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime= itemView.findViewById(R.id.textDateTime);
            layoutNote= itemView.findViewById(R.id.layoutNote);
            imageNote= itemView.findViewById(R.id.imageNote);

        }
        //Hiển thị các thông tin của ghi chú lên View.
        void setNote(Note note){
            textTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }
            else{
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());


            GradientDrawable gradientDrawable=(GradientDrawable) layoutNote.getBackground();
            if(note.getColor() !=null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if(note.getImagePath()!=null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }
            else{
                imageNote.setVisibility(View.GONE);

            }
        }
    }
    //Tìm kiếm các ghi chú theo từ khóa tìm kiếm.
    public void searchNotes(final String searchKeyword){
        timer= new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    notes = notesSource;
                }
                else{
                    ArrayList<Note> temp= new ArrayList<>();
                    for(Note note: notesSource){
                        if(note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())||
                        note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    notes= temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }
    public void cancelTimer(){
     if(timer!=null){
         timer.cancel();
     }
    }
}
