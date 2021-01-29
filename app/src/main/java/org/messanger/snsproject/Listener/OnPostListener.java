package org.messanger.snsproject.Listener;

import java.util.ArrayList;

public interface OnPostListener {
    void onDelete(String postId, ArrayList<String> mediaFiles);
}
