package com.example.karimt.belladatidongleapp;

import java.io.Serializable;

/**
 * Created by KarimT on 14.02.2017.
 */

public class LimitData implements Serializable {

    public double getLimitBreaking() {
        return limitBreaking;
    }

    public void setLimitBreaking(double limitBreaking) {
        this.limitBreaking = limitBreaking;
    }

    private double limitBreaking = 0D;
    private double limitCornering = 0D;

    public double getLimitSpeedUp() {
        return limitSpeedUp;
    }

    public void setLimitSpeedUp(double limitSpeedUp) {
        this.limitSpeedUp = limitSpeedUp;
    }

    public double getLimitCornering() {
        return limitCornering;
    }

    public void setLimitCornering(double limitCornering) {
        this.limitCornering = limitCornering;
    }

    private double limitSpeedUp = 0D;
    private static final long serialVersionUID = 465489764;


}
