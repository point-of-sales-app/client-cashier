package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetLastIdResponse {
    @SerializedName("msg")
    private String msg;
    @SerializedName("id")
    private int id;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
