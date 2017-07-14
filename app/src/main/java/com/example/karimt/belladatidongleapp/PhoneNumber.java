package com.example.karimt.belladatidongleapp;

import java.io.Serializable;

/**
 * Created by KarimT on 14.12.2016.
 * class for emergency phone number xml file
 */
public class PhoneNumber implements Serializable {

    private String phoneNumber = "";

    public String getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(String serviceNumber) {
        this.serviceNumber = serviceNumber;
    }

    public String getFirstAidNumber() {
        return firstAidNumber;
    }

    public void setFirstAidNumber(String firstAidNumber) {
        this.firstAidNumber = firstAidNumber;
    }

    private String serviceNumber = "";
    private String firstAidNumber = "";

    private static final long serialVersionUID = 465489764;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}

