package com.kakao;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Elevator {

    int id;
    int floor;
    List<Call> passengers;
    String status;

    public Elevator(int id, int floor, List<Call> passengers, String status) {
        this.id = id;
        this.floor = floor;
        this.passengers = passengers;
        this.status = status;
    }


    public Elevator(JSONObject json) {
        this.id = json.getInt("id");
        this.floor = json.getInt("floor");
        this.status = json.getString("status");
        JSONArray passJson = json.getJSONArray("passengers");
        this.passengers = new ArrayList<>();
        for (Object passenger : passJson) {
            this.passengers.add(new Call((JSONObject) passenger));
        }
    }

    @Override
    public String toString() {
        return "Elevator{" +
            "엘리베이터 id=" + id +
            ", 층 수=" + floor +
            ", 승객 명단=" + passengers +
            ", 엘리베이터 상태 ='" + status + '\'' +
            '}';
    }
}
