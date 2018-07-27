package com.rezapramudhika.simplepos.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MoneyFormat {

    public static String idr(double angka) {
        DecimalFormat mataUangIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

        formatRp.setCurrencySymbol("Rp.");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        mataUangIndonesia.setDecimalFormatSymbols(formatRp);
        String output = String.valueOf(mataUangIndonesia.format(angka));
        return output;
    }

}
