package org.messanger.snsproject.Listener;

import android.view.View;

import org.messanger.snsproject.structure.Pair;

import java.util.ArrayList;
import java.util.Map;

public interface OnCommentListener {
    void onSave(String postId, String uid, String comment, ArrayList<Pair> commentList, int pos);
    void onDelete(String postId, int idx, ArrayList<Pair> commentList,int pos);
}
