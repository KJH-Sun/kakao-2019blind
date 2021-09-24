package com.kakao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpUtil {

    private static HttpUtil instance = null;

    private HttpUtil() {
    }

    public static HttpUtil getInstance() {
        return instance == null ? new HttpUtil() : instance;
    }

    public JSONObject callApi(String reqUrl, JsonObject params, String type, boolean init) {

        HttpURLConnection conn = null;
        JSONObject responseJson = null;

        try {
            //URL 설정
            URL url = new URL(Constants.HOST_URL + reqUrl);

            conn = (HttpURLConnection) url.openConnection();

            // type의 경우 POST, GET, PUT, DELETE 가능
            conn.setRequestMethod(type);
            conn.setRequestProperty("Content-Type", "application/json");
            if (!init) {
                conn.setRequestProperty("X-Auth-Token", TokenManager.getInstance().getToken());
            }
            conn.setDoOutput(true);

            if (!type.equals("GET")) {
                BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(conn.getOutputStream()));
                // JSON 형식의 데이터 셋팅

                // 데이터를 STRING으로 변경
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                String jsonOutput = gson.toJson(params);
//                System.out.println("send JSON : " + jsonOutput);

                bw.write(params.toString());
                bw.flush();
                bw.close();
            }

            // 보내고 결과값 받기
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                responseJson = new JSONObject(sb.toString());
                // 응답 데이터
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonOutput = gson.toJson(responseJson);
                System.out.println("response :: " + jsonOutput);
            } else {
                System.out.println("responseCode : " + responseCode);
                Map<String, String> failRequest = new HashMap<>();
                failRequest.put("responseCode", Integer.toString(responseCode));
                responseJson = new JSONObject(failRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("not JSON Format response");
            e.printStackTrace();
        }
        return responseJson;
    }


    public String start(String user_key, int problemId, int number_of_elevators) {
        String URL =
            Constants.POST_START + "/" + user_key + "/" + problemId + "/" + number_of_elevators;
        JSONObject response = callApi(URL, new JsonObject(), "POST", true);
        if (response.has("responseCode")) {
            return response.getString("responseCode");
        } else {
            return response.getString("token");
        }
    }
}
