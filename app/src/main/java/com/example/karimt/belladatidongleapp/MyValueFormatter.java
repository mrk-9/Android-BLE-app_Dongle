package com.example.karimt.belladatidongleapp;

/**
 * Created by KarimT on 26.09.2016.
 * my value formatter for stacked bar chart
 */
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class MyValueFormatter implements ValueFormatter
{

    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        if (value==0.0)
        {
            return "";
        }
        else
        {
            return mFormat.format(value);
        }
    }
}
