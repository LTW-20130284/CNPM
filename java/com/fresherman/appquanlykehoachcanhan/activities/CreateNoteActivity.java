package com.fresherman.appquanlykehoachcanhan.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fresherman.appquanlykehoachcanhan.R;
import com.fresherman.appquanlykehoachcanhan.database.NoteDatabase;
import com.fresherman.appquanlykehoachcanhan.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {
    // Khai báo biến
    private EditText inputNoteTitle , inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private View viewSubtitleIndictor;
    private ImageView imageNote;
    private TextView textWebUrl;
    private LinearLayout layoutWebUrl;
    private Note alreadyAvailableNote;
    private String selectedNoteColor;
    private String selectedImagePath;
    private static final int REQUEST_CODE_STORAGE=1;
    private static final int REQUEST_CODE_SELECT_IMAGE=2;

    private AlertDialog dialogAddURl;
    private AlertDialog dialogDeleteNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        // Khởi tạo các thành phần giao diện và thiết lập sự kiện cho nút "Back"
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // Liên kết các thành phần giao diện
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle= findViewById(R.id.inputNoteSubtitle);
        inputNoteText=findViewById(R.id.inputNote);
        textDateTime= findViewById(R.id.textDateTime);
        viewSubtitleIndictor= findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebUrl= findViewById(R.id.layoutWebUrl);
        // Thiết lập ngày giờ hiện tại cho ghi chú và thiết lập sự kiện cho nút "Save"
        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );
        ImageView imageView = findViewById(R.id.imageSave);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
        // Thiết lập mặc định cho màu ghi chú và đường dẫn ảnh đã chọn
        selectedNoteColor="#333333";
        selectedImagePath="";

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailableNote =(Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        findViewById(R.id.imageRemoveWebUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebUrl.setText(null);
                layoutWebUrl.setVisibility(View.GONE);

            }
        });
        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath="";
            }
        });
        // Kiểm tra xem trang này có được mở từ hành động QuickActions ko
        if(getIntent().getBooleanExtra("isFromQuickActions", false)){
            // Lấy loại hành động QuickAction được truyền qua intent
            String type= getIntent().getStringExtra("quickActionType");
            if(type!=null){
                // Nếu là hành động QuickAction "image", hiển thị ảnh và lưu đường dẫn vào biến
                if(type.equals("image")){
                    selectedImagePath= getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                }
                // Nếu là hành động QuickAction "URL", hiển thị đường dẫn và lưu vào trường textWebUrl
                else if(type.equals("URL")){
                    textWebUrl.setText(getIntent().getStringExtra("URL"));
                    layoutWebUrl.setVisibility(View.VISIBLE);
                }
            }
        }
        // Khởi tạo các thành phần giao diện phụ
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }
    // Thiết lập các giá trị cho phần xem hoặc cập nhật ghi chú
    private void setViewOrUpdateNote(){
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());
        // Nếu có đường dẫn ảnh, hiển thị ảnh và phần xóa ảnh
        if(alreadyAvailableNote.getImagePath()!=null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

            selectedImagePath = alreadyAvailableNote.getImagePath();
        }
        // Nếu có đường dẫn liên kết web, hiển thị đường dẫn và phần liên kết web
        if(alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            textWebUrl.setText(alreadyAvailableNote.getWebLink());
            layoutWebUrl.setVisibility(View.VISIBLE);
        }
    }
    // Lưu ghi chú
    private void saveNote(){
        // Nếu tiêu đề ghi chú trống, hiển thị thông báo lỗi
        if(inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note title can't be empty", Toast.LENGTH_SHORT).show();
        }
        // Nếu phụ đề và nội dung đều trống, hiển thị thông báo lỗi
        else if(inputNoteSubtitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()
        ){
            Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show();
            return ;
        }
        // Tạo đối tượng ghi chú mới với các giá trị đã nhập
        final Note note= new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setImagePath(selectedImagePath);
        note.setColor(selectedNoteColor);
        // Nếu có đường dẫn liên kết web, thiết lập giá trị đường dẫn
        if(layoutWebUrl.getVisibility()==View.VISIBLE){
            note.setWebLink(textWebUrl.getText().toString());
        }
        // Nếu ghi chú đã tồn tại, thiết lập ID của nó cho đối tượng ghi chú mới
        if(alreadyAvailableNote !=null){
            note.setId(alreadyAvailableNote.getId());
        }
        // Thực hiện lưu ghi chú bằng cách sử dụng AsyncTask
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void , Void , Void>{
            // Thực hiện tác vụ lưu ghi chú vào csdl trong phương thức doInBackground
            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                // Tạo một Intent và đặt kết quả là RESULT_OK để thông báo cho Activity rằng tác vụ đã được hoàn thành thành công
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }
    // Lấy reference đến layout Miscellaneous và BottomSheetBehavior tương ứng
    private void initMiscellaneous(){
        // Thêm sự kiện click cho nút "Miscellaneous" để mở/collapse bottom sheet
        final LinearLayout layoutMiscellanous= findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior bottomSheetBehavior=BottomSheetBehavior.from(layoutMiscellanous);
        layoutMiscellanous.findViewById(R.id.textMiscelaneous).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(bottomSheetBehavior.getState() !=BottomSheetBehavior.STATE_EXPANDED){
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        else{
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }
                }
        );
        // Lấy reference đến tất cả các ImageView để chọn màu cho note
        final ImageView imageColor1=layoutMiscellanous.findViewById(R.id.imageColor1);
        final ImageView imageColor2=layoutMiscellanous.findViewById(R.id.imageColor2);
        final ImageView imageColor3=layoutMiscellanous.findViewById(R.id.imageColor3);
        final ImageView imageColor4=layoutMiscellanous.findViewById(R.id.imageColor4);
        final ImageView imageColor5=layoutMiscellanous.findViewById(R.id.imageColor5);
        // Thêm sự kiện click cho từng màu để đổi màu note và chọn indicator tương ứng
        layoutMiscellanous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor= "#3333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellanous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor= "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellanous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor= "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellanous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor= "#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellanous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor= "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            }
        });
        // Kiểm tra xem ghi chú đã tồn tại, nếu có thì chọn màu nền tương ứng với ghi chú đó
        if(alreadyAvailableNote!= null && alreadyAvailableNote.getColor()!=null
                && !alreadyAvailableNote.getColor().trim().isEmpty()){
            // Sử dụng switch case để xác định màu nền của ghi chú và chọn viewColor tương ứng
                switch (alreadyAvailableNote.getColor()){
                    case "#FDBE3B":
                        layoutMiscellanous.findViewById(R.id.viewColor2).performClick();
                        break;
                    case "#FF4842":
                        layoutMiscellanous.findViewById(R.id.viewColor3).performClick();
                        break;
                    case "#3A52FC":
                        layoutMiscellanous.findViewById(R.id.viewColor4).performClick();
                        break;
                    case "#000000":
                        layoutMiscellanous.findViewById(R.id.viewColor5).performClick();
                        break;

                }
        }
        // Thêm sự kiện click vào layoutAddImage để chọn hình ảnh cho ghi chú
        layoutMiscellanous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                // Kiểm tra xem ứng dụng có quyền truy cập vào bộ nhớ để đọc file ảnh hay không
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    // Nếu chưa được cấp quyền thì yêu cầu cấp quyền
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                }
                else{
                    // Nếu đã được cấp quyền thì hiển thị giao diện chọn ảnh
                    selectImage();
                }
            }
        });
        // Thêm sự kiện click vào layoutAddUri để thêm đường dẫn cho ghi chú
        layoutMiscellanous.findViewById(R.id.layoutAddUri).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialog();
            }
        });
        // Nếu ghi chú đã tồn tại, hiển thị layout để xóa ghi chú
        if(alreadyAvailableNote !=null){
            layoutMiscellanous.findViewById(R.id.layoutDelecteNote).setVisibility(View.VISIBLE);
            // Thêm sự kiện click vào layoutDelecteNote để xóa ghi chú và ẩn bottom sheet.
            layoutMiscellanous.findViewById(R.id.layoutDelecteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                  showDeleteNoteDialog();
                }
            });
        }

        }
    // Hiển thị hộp thoại xóa ghi chú
    private void showDeleteNoteDialog(){
        if(dialogDeleteNote==null){
            // Tạo hộp thoại xóa ghi chú bằng AlertDialog.Builder
            AlertDialog.Builder builder= new AlertDialog.Builder(CreateNoteActivity.this);
            // Tạo view từ layout layout_delet_note.xml
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delet_note,(ViewGroup) findViewById(R.id.layoutDeleteNoteConteiner));
            builder.setView(view);
            // Khởi tạo dialogDeleteNote
            dialogDeleteNote = builder.create();
            if(dialogDeleteNote.getWindow() !=null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            // Thêm sự kiện click vào nút Xóa ghi chú
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Tạo một lớp AsyncTask để xóa ghi chú
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void>{
                        // Thực hiện xóa ghi chú trong doInBackground
                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }
                        // Sau khi xóa ghi chú, gửi trả kết quả cho activity trước đó và kết thúc CreateNoteActivity
                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent= new Intent();
                            intent.putExtra("isNoteDelected", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    // Thực thi lớp AsyncTask
                    new DeleteNoteTask().execute();
                }
            });
            // Thêm sự kiện click vào nút Delecte
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ẩn hộp thoại
                    dialogDeleteNote.dismiss();
                }
            });
        }
        // Hiển thị hộp thoại xóa ghi chú
        dialogDeleteNote.show();
    }
    // Thiết lập màu cho viewSubtitleIndictor
    private void setSubtitleIndicatorColor(){

        GradientDrawable gradientDrawable= (GradientDrawable) viewSubtitleIndictor.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));

    }
    // Hàm này được gọi khi người dùng nhấn vào layoutAddImage để chọn ảnh
    private void selectImage(){

        Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }
    // Hàm được gọi khi người dùng cho phép hoặc từ chối quyền truy cập bộ nhớ
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORAGE && grantResults.length>0){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else{
                Toast.makeText(this, "Permisson", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Hàm được gọi khi kết quả trả về từ việc chọn ảnh cho ghi chú
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Nếu kết quả trả về từ việc chọn ảnh và thành công
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data!= null){
                // Lấy đường dẫn của ảnh được chọn
                Uri selectedImageUri =data.getData();
                if(selectedImageUri != null){
                    try {
                        // Mở một luồng đọc dữ liệu từ đường dẫn ảnh và chuyển đổi thành bitmap
                        InputStream inputStream= getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        // Hiển thị ảnh đã chọn lên ImageView
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                        // Lưu đường dẫn ảnh đã chọn
                        selectedImagePath =getPathFronUri(selectedImageUri);
                        // Hiển thị thông báo lỗi nếu xảy ra lỗi khi xử lý ảnh
                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // Phương thức này dùng để lấy đường dẫn thực tế của file được chọn từ Uri của file
    private String getPathFronUri(Uri contentUri){
        String filePath;
        // Tạo cursor để truy vấn bộ nhớ và trả về các thông tin liên quan đến Uri
        Cursor cursor= getContentResolver()
                .query(contentUri, null, null, null, null);
        // Tạo cursor để truy vấn bộ nhớ và trả về các thông tin liên quan đến Uri
        if(cursor==null){
            filePath = contentUri.getPath();
        }
        // Nếu cursor trả về dữ liệu, di chuyển con trỏ cursor đến hàng đầu tiên và lấy thông tin đường dẫn của file từ cột "_data"
        else{
            cursor.moveToFirst();
            int index= cursor.getColumnIndex("_data");
           filePath= cursor.getString(index);
           cursor.close();
        }
        return filePath;
    }
    // Phương thức này được gọi khi người dùng chọn chức năng thêm URL
    private  void showAddUrlDialog(){

        if(dialogAddURl==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                        Toast.makeText(CreateNoteActivity.this,"Enter URL", Toast.LENGTH_SHORT).show();
                    }
                    else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(CreateNoteActivity.this,"Enter valid Url", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        textWebUrl.setText(inputURL.getText().toString());
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        dialogAddURl.dismiss();
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
