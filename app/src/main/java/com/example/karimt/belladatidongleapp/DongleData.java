package com.example.karimt.belladatidongleapp;

import java.io.Serializable;

/**
 * Created by KarimT on 16.02.2017.
 */

public class DongleData implements Serializable {


    public String getDongleName() {
        return dongleName;
    }

    public void setDongleName(String dongleName) {
        this.dongleName = dongleName;
    }

    private String dongleName = "";

    private static final long serialVersionUID = 465489764;


}
