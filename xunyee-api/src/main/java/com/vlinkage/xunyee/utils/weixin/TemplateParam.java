package com.vlinkage.xunyee.utils.weixin;

public class TemplateParam {

    private String key;
    private String Value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public TemplateParam(String key, String value) {
        this.key = key;
        Value = value;
    }
}
