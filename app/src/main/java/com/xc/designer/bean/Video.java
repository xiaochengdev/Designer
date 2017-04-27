package com.xc.designer.bean;

import com.xc.designer.interfaces.CustomFile;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/20.
 */

public class Video extends DataSupport implements Serializable,CustomFile{
    private Integer vid;
    private String name;
    private String path;
    private String descr;

    public Integer getVid() {
        return vid;
    }

    public void setVid(Integer vid) {
        this.vid = vid;
    }

    @Override
    public Integer getFileId() {
        return vid;
    }

    @Override
    public void setFileId(Integer fileId) {
        this.vid=fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    @Override
    public String getKind() {
        return "video";
    }
}
