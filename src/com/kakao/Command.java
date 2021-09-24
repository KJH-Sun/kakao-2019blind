package com.kakao;

import java.util.ArrayList;
import java.util.List;

public class Command {

    int elevator_id;
    String command;
    List<Integer> call_ids;

    public Command(int elevator_id, String command) {
        this.elevator_id = elevator_id;
        this.command = command;
    }
}
