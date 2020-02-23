package com.rifkifi.insta.Model;

public class Notification {
    private String  userid,
                    text,
                    postid;
    private Boolean ispost;

    public Notification(String userid, String text, String postid, Boolean ispost) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.ispost = ispost;
    }

    public Notification() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public Boolean getIspost() {
        return ispost;
    }

    public void setIspost(Boolean ispost) {
        this.ispost = ispost;
    }
}
