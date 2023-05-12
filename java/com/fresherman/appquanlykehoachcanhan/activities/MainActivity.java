package com.fresherman.appquanlykehoachcanhan.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fresherman.appquanlykehoachcanhan.R;
import com.fresherman.appquanlykehoachcanhan.adapters.NoteAdapter;
import com.fresherman.appquanlykehoachcanhan.database.NoteDatabase;
import com.fresherman.appquanlykehoachcanhan.entities.Note;
import com.fresherman.appquanlykehoachcanhan.listeners.NoteListener;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {
    public static final int REQUEST_CODE_AND_NOTE=1;
    public static final int REQUEST_CODE_UPDATE_NOTE=2;
    public static  final int REQUEST_CODE_SHOW_NOTES=3;
    public static final int REQUEST_CODE_SELECT_IMAGE=4;
    public static final int REQUEST_CODE_STORGE_PERMISSION=5;


    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NoteAdapter noteAdapter;

    private int noteClickedPosition=-1;
    private AlertDialog dialogAddURl;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain= findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_AND_NOTE);
            }
        });

        notesRecyclerView= findViewById(R.id.notesRecylerView);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        noteAdapter= new NoteAdapter(noteList, this);
        notesRecyclerView.setAdapter(noteAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size()!=0){
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });
        //nút thêm note
        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_AND_NOTE);
            }
        });
        //nut thêm ảnh
        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORGE_PERMISSION);
                }
                else{
                    selectImage();
                }
            }
        });
        // nút thêm đường dẫn web
        findViewById(R.id.imageAddWebLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUrlDialog();
            }
        });
    }
    // Phương thức này được sử dụng để chọn một ảnh từ bộ nhớ ngoài của thiết bị
    private void selectImage(){
        Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }
    // Phương thức được gọi khi yêu cầu quyền truy cập bộ nhớ được cho phép
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORGE_PERMISSION && grantResults.length>0){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else{
                Toast.makeText(this, "Permisson", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Phương thức này nhận đầu vào là một Uri của một tài nguyên trên thiết bị và trả về đường dẫn thực tế của tệp.
    private String getPathFronUri(Uri contentUri){
        String filePath;
        Cursor cursor= getContentResolver()
                .query(contentUri, null, null, null, null);
        if(cursor==null){
            filePath = contentUri.getPath();
        }
        else{
            cursor.moveToFirst();
            int index= cursor.getColumnIndex("_data");
            filePath= cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    // Phương thức này được gọi khi người dùng nhấn vào một ghi chú trên danh sách.
    @Override
    public void onNoteClicked(Note note, int position) {
        // Gán vị trí ghi chú được chọn bằng vị trí hiện tại.
        noteClickedPosition= position;
        // Tạo một Intent để chuyển đến CreateNoteActivity và truyền dữ liệu về ghi chú được chọn.
        Intent intent= new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note", note);
        // Gọi startActivityForResult để hiển thị CreateNoteActivity và chờ kết quả trả về.
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted){
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                // Nếu requestCode bằng REQUEST_CODE_SHOW_NOTES
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    // Thêm danh sách ghi chú mới vào danh sách hiện tại và cập nhật giao diện
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();

                }
                // Nếu requestCode bằng REQUEST_CODE_AND_NOTE
                else if (requestCode == REQUEST_CODE_AND_NOTE) {
                    //Thêm ghi chú mới vào đầu danh sách hiện tại và cuộn màn hình lên đầu danh sách
                    noteList.add(0, notes.get(0));
                    noteAdapter.notifyItemChanged(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }
                // Nếu requestCode bằng REQUEST_CODE_UPDATE_NOTE
               else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                    // Xóa ghi chú cũ khỏi danh sách hiện tại và cập nhật lại danh sách
                    noteList.remove(noteClickedPosition);
                   if(isNoteDeleted){
                       noteAdapter.notifyItemRemoved(noteClickedPosition);
                   }
                   // Nếu ghi chú không bị xóa, thêm ghi chú mới vào danh sách và cập nhật lại danh sách
                   else{
                       noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                       noteAdapter.notifyItemChanged(noteClickedPosition);
                   }
               }
            }
        }
 //       else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
//                    if (noteClickedPosition < noteList.size()) {
//                        noteList.remove(noteClickedPosition);
//                        if (isNoteDeleted) {
//                            noteAdapter.notifyItemRemoved(noteClickedPosition);
//                        } else {
//                            noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
//                            noteAdapter.notifyItemChanged(noteClickedPosition);
//                        }
//                    }
//                }
        GetNoteTask task = new GetNoteTask();
        task.execute();
    }
    // Xử lý kết quả trả về từ các hoạt động đã gọi trước đó
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Nếu hoạt động tạo mới ghi chú hoàn tất và kết quả trả về là OK
        if(requestCode== REQUEST_CODE_AND_NOTE && resultCode == RESULT_OK){
            // Gọi phương thức getNotes để lấy danh sách ghi chú mới nhất
            getNotes(REQUEST_CODE_AND_NOTE,false);
        }// Nếu hoạt động cập nhật ghi chú hoàn tất và kết quả trả về là OK
        else if(requestCode== REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK){
            // Kiểm tra xem có dữ liệu trả về từ hoạt động hay không
            if(data !=null){
                // Lấy giá trị boolean isNoteDeleted từ intent
                // Gọi phương thức getNotes để lấy danh sách ghi chú mới nhất và truyền giá trị isNoteDeleted
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
        // Nếu hoạt động chọn ảnh hoàn tất và kết quả trả về là OK
        else if(requestCode== REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data!=null){
                // Lấy URI của ảnh được chọn
                Uri selectedImageUri =data.getData();
                if(selectedImageUri!=null){
                    try{
                        // Lấy đường dẫn tuyệt đối của ảnh từ URI
                        String selectedImagePath= getPathFronUri(selectedImageUri);
                        // Tạo intent để mở màn hình tạo mới ghi chú với các thông tin về ảnh
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        // Gọi startActivityForResult để mở màn hình tạo mới ghi chú và chờ kết quả trả về
                        startActivityForResult(intent, REQUEST_CODE_AND_NOTE);
                    }
                    catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    // phương thưc hiển URl cho ghi tại màn hình chính
    private  void showAddUrlDialog(){
        if(dialogAddURl==null){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);
            dialogAddURl= builder.create();
            if(dialogAddURl.getWindow() != null){
                dialogAddURl.getWindow().setBackgroundDrawable(new ColorDrawable(0));


            }
            final EditText inputURL= view.findViewById(R.id.inputUrl);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputURL.getText().toString().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this,"Enter URL", Toast.LENGTH_SHORT).show();
                    }
                    else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(MainActivity.this,"Enter valid Url", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        dialogAddURl.dismiss();
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("URL", inputURL.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_AND_NOTE);
                    }
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURl.dismiss();
                }
            });
        }
        dialogAddURl.show();

    }

}