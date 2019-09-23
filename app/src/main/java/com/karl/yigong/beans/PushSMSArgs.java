package com.karl.yigong.beans;

import java.io.Serializable;

/**
 * 推送的信息的数据
 */
public class PushSMSArgs implements Serializable {
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
