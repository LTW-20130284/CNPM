package com.fresherman.appquanlykehoachcanhan.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fresherman.appquanlykehoachcanhan.dao.NoteDao;
import com.fresherman.appquanlykehoachcanhan.entities.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase noteDatabase;

    public static synchronized NoteDatabase getDatabase(Context context){
        // Nếu noteDatabase chưa được khởi tạo
        if(noteDatabase==null){
            // Khởi tạo noteDatabase sử dụng Room.databaseBuilder để tạo cơ sở dữ liệu
            // context: context của ứng dụng
            // NoteDatabase.class: class của cơ sở dữ liệu
            // "notes_db": tên của cơ sở dữ liệu
            noteDatabase= Room.databaseBuilder(
                    context, NoteDatabase.class,
                    "notes_db"
            ).build();
        }
        return noteDatabase;
    }
    // Khai báo abstract method để lấy đối tượng DAO, được sử dụng để truy vấn dữ liệu
    public abstract NoteDao noteDao();
}
