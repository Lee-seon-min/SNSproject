package org.messanger.snsproject.structure;

public class Pair {
    private String uid;
    private String comment;
    public Pair(String a, String b){
        uid=a;
        comment=b;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
