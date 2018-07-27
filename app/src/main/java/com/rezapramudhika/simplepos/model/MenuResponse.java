package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MenuResponse {
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private List<Menu> menus;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }
}
