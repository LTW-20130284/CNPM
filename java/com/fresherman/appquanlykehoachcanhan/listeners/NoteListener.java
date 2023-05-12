package com.fresherman.appquanlykehoachcanhan.listeners;

import com.fresherman.appquanlykehoachcanhan.entities.Note;

public interface NoteListener {
    void onNoteClicked(Note note, int position);

}
