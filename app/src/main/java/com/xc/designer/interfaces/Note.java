package com.xc.designer.interfaces;

/**
 * Created by Administrator on 2017/4/13.
 */

public interface Note {
    public Integer getNid();
    public void setNid(Integer nid);

    public String getContents();
    public void setContents(String contents);

    public String getFdate();
    public void setFdate(String fdate);

    public Integer getUserid();
    public void setUserid(Integer userid);

    public String getUsername();
    public void setUsername(String username);

    public String getTitle();
    public void setTitle(String title);
}
