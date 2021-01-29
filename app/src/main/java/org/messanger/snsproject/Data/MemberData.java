package org.messanger.snsproject.Data;

public class MemberData {
    private String myname; //이름
    private String myphone; //폰번호
    private String mybirth; //생일
    private String myaddress; //집주소
    private String prof_path; //파이어베이스에 저장된 프로필 이미지 경로

    public MemberData(String n,String p, String b, String a,String pr){
        this.myname=n;
        this.myphone=p;
        this.mybirth=b;
        this.myaddress=a;
        this.prof_path=pr;
    }
    public MemberData(){}
    public String getProf_path() {
        return prof_path;
    }

    public String getMyname() {
        return myname;
    }

    public String getMyphone() {
        return myphone;
    }

    public String getMybirth() {
        return mybirth;
    }

    public String getMyaddress() {
        return myaddress;
    }
    public void setProf_path(String prof_path) {
        this.prof_path = prof_path;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }

    public void setMyphone(String myphone) {
        this.myphone = myphone;
    }

    public void setMybirth(String mybirth) {
        this.mybirth = mybirth;
    }

    public void setMyaddress(String myaddress) {
        this.myaddress = myaddress;
    }
}
