package com.kakao;

import org.json.JSONObject;

public class Call {

    int id;
    int start;
    int end;
    int timestamp;

    public Call(int id, int start, int end, int timestamp) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Call{" +
            "id=" + id +
            ", 탑승 층=" + start +
            ", 목적 층=" + end +
            ", 버튼 누른 시간=" + timestamp +
            '}';
    }


    public Call(JSONObject json) {
        this.start = json.getInt("start");
        this.end = json.getInt("end");
        this.id = json.getInt("id");
        this.timestamp = json.getInt("timestamp");
    }

}
