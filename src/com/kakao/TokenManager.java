package com.kakao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class TokenManager {

    private static TokenManager instance = null;
    private String token = "";


    public static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public String getToken() {
        return this.token;
    }

    public synchronized String createToken(int problemId) {
        String response = HttpUtil.getInstance().start(problemId);
        switch (response) {
            case "400":
                System.out.println("400:: problem_id 잘못됨");
                break;
            case "401":
                System.out.println("401:: X-Auth-Token Header가 잘못됨");
                break;
            case "403":
                System.out.println("403:: user_key가 잘못되었거나 10초 이내에 생성한 토큰이 존재");
                token = loadTokenFile();
                break;
            case "500":
                System.out.println("500:: 서버 에러, 문의 필요");
                break;
            default:
                saveTokenFile(response/* token */);
                token = response;
                response = "200";
                break;
        }
        return response;
    }

    private void saveTokenFile(String token) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./res/token")));
            bw.write(token);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadTokenFile() {
        String token = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("./res/token")));
            token = br.readLine();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }
}
