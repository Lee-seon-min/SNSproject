package org.messanger.snsproject.Data;

import org.messanger.snsproject.structure.Pair;

import java.util.ArrayList;
import java.util.Date;

public class PostData { //일반적 게시물의 정보들
    private String title;
    private String contents;
    private String uid;
    private ArrayList<String> mediaValues;
    private Date postDate;
    private ArrayList<String> mediaFormats;
    private ArrayList<Pair> comments;
    public PostData(String t,String c,String u,ArrayList<String> m,Date p,ArrayList<String> formats,ArrayList<Pair> comments){
        title=t;
        contents=c;
        uid=u;
        mediaValues=m;
        postDate=p;
        mediaFormats=formats;
        this.comments=comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getMediaValues() {
        return mediaValues;
    }

    public void setMediaValues(ArrayList<String> mediaValues) {
        this.mediaValues = mediaValues;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public ArrayList<String> getMediaFormats() {
        return mediaFormats;
    }

    public void setMediaFormats(ArrayList<String> mediaFormats) {
        this.mediaFormats = mediaFormats;
    }

    public ArrayList<Pair> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Pair> comments) {
        this.comments = comments;
    }
}
