package org.messanger.snsproject.Data;

import org.messanger.snsproject.structure.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ProcessedPostData { //실제 게시물 리스트에 필요한 정보들로 가공한 데이터
    private String name; // 이름
    private String title; // 제목
    private String contents; // 내부 텍스트 내용
    private ArrayList<String> mediaBox; //이미지 또는 비디오(첨부)
    private String writerProfImage; //유저의 프로필 이미지
    private String myUid; //로그인 유저의 아이디
    private String postId; //게시물 고유아이디
    private String wDate; //작성 날짜
    private ArrayList<String> formats;
    private ArrayList<Pair> comments;

    public ProcessedPostData(String name,String title ,String contents, ArrayList<String> mediaBox, String writerProfImage,String uid,String pid,String date,ArrayList<String> formats,ArrayList<Pair> comments) {
        this.name = name;
        this.title = title;
        this.contents = contents;
        this.mediaBox = mediaBox;
        this.writerProfImage = writerProfImage;
        this.myUid=uid;
        this.postId=pid;
        this.wDate=date;
        this.formats=formats;
        this.comments=comments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ArrayList<String> getMediaBox() {
        return mediaBox;
    }

    public void setMediaBox(ArrayList<String> mediaBox) {
        this.mediaBox = mediaBox;
    }

    public String getWriterProfImage() {
        return writerProfImage;
    }

    public void setWriterProfImage(String writerProfImage) {
        this.writerProfImage = writerProfImage;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getwDate() {
        return wDate;
    }

    public void setwDate(String wDate) {
        this.wDate = wDate;
    }

    public ArrayList<String> getFormats() {
        return formats;
    }

    public void setFormats(ArrayList<String> formats) {
        this.formats = formats;
    }

    public ArrayList<Pair> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Pair> comments) {
        this.comments = comments;
    }
}
