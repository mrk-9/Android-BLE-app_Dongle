package com.example.karimt.belladatidongleapp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by KarimT on 20.01.2017.
 */

public class CalibrationData implements Serializable {

    public byte[] getStoredData() {
        return storedData;
    }

    public void setStoredData(byte[] storedData) {
        this.storedData = storedData;
    }

    private byte[] storedData;
    private static final long serialVersionUID = 465489764;

}
