package com.xc.designer.interfaces;

/**
 * Created by Administrator on 2017/4/20.
 */

public interface CustomFile {
    public Integer getFileId();
    public void setFileId(Integer fileId);

    public String getName();
    public void setName(String name);

    public String getPath();
    public void setPath(String path);

    public String getDescr();
    public void setDescr(String descr);

    public String getKind();
}
