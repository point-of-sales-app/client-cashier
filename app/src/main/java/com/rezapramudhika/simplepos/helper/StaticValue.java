package com.rezapramudhika.simplepos.helper;

import java.util.ArrayList;

/**
 * Created by shohrab.uddin on 29.12.2015.
 */
public class StaticValue {
    public static boolean  isPrinterConnected=false;
//    public static ArrayList<BillModel> billModelArrayList = new ArrayList<BillModel>();
//    public static ArrayList<BillModel> check = new ArrayList<BillModel>();
    public static final String CURRENCY = "Rp.";
    //public static final double VAT = 10.00;
    public static final String VAT_REGISTRATION_NUMBER ="8877BD9877";
    public static final String BRANCH_ADDRESS ="70188, Stuttgart, Germany";

    public static String nameLeftValueRightJustify(String param1, String param2,
                                                   int cpl) {
        if(param1 == null)
            param1 = "";
        if(param2 == null)
            param2 = "";
        int len = param1.length();
        return param1.trim()+rightJustify(param2, (cpl - len));
    }

    public static String rightJustify(String item, int digits) {
        StringBuffer buf = null;
        if(digits < 0)
        {
            buf = new StringBuffer();
        }
        else
        {
            buf = new StringBuffer(digits);
        }
        for (int i = 0; i < digits - item.length(); i++) {
            buf.append(" ");
        }
        buf.append(item);
        return buf.toString();
    }
}
