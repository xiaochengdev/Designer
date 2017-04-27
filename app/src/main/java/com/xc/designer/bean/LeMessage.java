package com.xc.designer.bean;

import com.xc.designer.interfaces.Note;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/4/11.
 */

public class LeMessage extends DataSupport implements Serializable,Note{
    private Integer lid;
    private String contents;
    private String fdate;
    private Integer userid;
    private String username;
    private String title;

    public Integer getNid() {
        return lid;
    }

    public void setNid(Integer nid) {
        this.lid = nid;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getFdate() {
        return fdate;
    }

    public void setFdate(String fdate) {
        this.fdate = fdate;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
