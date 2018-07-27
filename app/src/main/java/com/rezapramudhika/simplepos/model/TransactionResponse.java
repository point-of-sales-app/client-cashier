package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

public class TransactionResponse {
    @SerializedName("msg")
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
