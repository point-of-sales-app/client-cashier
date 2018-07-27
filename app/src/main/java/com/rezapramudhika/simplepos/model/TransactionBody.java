package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionBody {
    @SerializedName("total")
    private int total;
    @SerializedName("paymentmethodid")
    private int paymentmethodid;
    @SerializedName("userid")
    private int userid;
    @SerializedName("transactionMenus")
    private List<TransactionMenu> transactionMenus;

    public TransactionBody(int total, int paymentmethodid, int userid, List<TransactionMenu> transactionMenus) {
        this.total = total;
        this.paymentmethodid = paymentmethodid;
        this.userid = userid;
        this.transactionMenus = transactionMenus;
    }
}
