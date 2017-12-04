package com.demo.evolving.net.lib;

/**
 * Created by CodingCodersCode on 2017/10/17.
 */

public class TestRespObj {

    //{"content":"请先登录","statusCode":222}
    private String content;
    private int statusCode;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "TestRespObj{" +
                "content='" + content + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
