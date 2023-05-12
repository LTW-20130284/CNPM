package com.fresherman.appquanlykehoachcanhan.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fresherman.appquanlykehoachcanhan.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {
    //lấy tất cả các ghi chú và sắp xếp theo thứ tự giảm dần của id.
    @Query("SELECT*FROM notes ORDER BY id DESC")
    //Phương thức truy vấn và trả về danh sách các ghi chú.
    List<Note> getAllNotes();
    //Chèn một ghi chú vào csdl, hoặc thay thế nếu đã tồn tại.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);
    //Xóa một ghi chú khỏi csdl
    @Delete
    void deleteNote(Note  note);
}
