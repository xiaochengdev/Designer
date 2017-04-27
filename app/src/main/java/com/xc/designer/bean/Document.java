package com.xc.designer.bean;

import com.xc.designer.interfaces.CustomFile;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/23.
 */

public class Document extends DataSupport implements Serializable,CustomFile {
    private Integer did;
    private String name;
    private String path;
    private String descr;

    public Integer getDid() {
        return did;
    }

    public void setDid(Integer did) {
        this.did = did;
    }

    @Override
    public Integer getFileId() {
        return did;
    }

    @Override
    public void setFileId(Integer fileId) {
        this.did=fileId;
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
        return "document";
    }
}
