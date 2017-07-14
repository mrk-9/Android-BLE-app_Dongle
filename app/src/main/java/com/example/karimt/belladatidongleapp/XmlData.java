package com.example.karimt.belladatidongleapp;

/**
 * Created by KarimT on 29.09.2016.
 * xmlData of Mac Address of Dongle
 */
import java.io.Serializable;

public class XmlData implements Serializable {

    private String macAddress = "";
    private static final long serialVersionUID = 465489764;

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

}