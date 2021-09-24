package com.kakao;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    static List<Call> calls = new ArrayList<>();
    static List<Elevator> elevators = new ArrayList<>();
    static boolean[] downCalls, upCalls;
    static boolean isEnd;
    static int top, bot;
    static String[] prevStatus;

    public static void main(String[] args) {
        String user_key = "tester2";
        int problemId = 0;
        int number_of_elevators = 4;

        start(user_key, problemId, number_of_elevators);
        init(problemId, number_of_elevators);
        while (true) {
            onCalls();
            if (isEnd) {
                break;
            }
            checkCalls();
            List<Command> commands = makeCommand();
            doAction(commands);
        }

    }

    private static void doAction(List<Command> commands) {
        Gson gson = new Gson();
        JsonObject params = new JsonObject();
        params.add("commands", gson.toJsonTree(commands));
        JSONObject response = HttpUtil.getInstance()
            .callApi(Constants.POST_ACTION, params, "POST", false);
    }

    private static void init(int problemId, int number_of_elevators) {
        if (problemId == 0) {
            downCalls = new boolean[6];
            upCalls = new boolean[6];
        } else {
            downCalls = new boolean[26];
            upCalls = new boolean[26];
        }
        prevStatus = new String[number_of_elevators];
        Arrays.fill(prevStatus, "STOPPED");
    }

    private static List<Command> makeCommand() {
        List<Elevator> empty = new ArrayList<>();
        List<Elevator> working = new ArrayList<>();
        Elevator Up = null;
        Elevator Down = null;
        for (Elevator e : elevators) {
            if (e.passengers.isEmpty() && e.status.equals("UPWARD")) {
                Down = e;
            } else if (e.passengers.isEmpty() && e.status.equals("DOWNWARD")) {
                Up = e;
            } else if (e.passengers.isEmpty()) {
                empty.add(e);
            } else {
                working.add(e);
            }
        }
        List<Command> commands = sendEmptyElevator(empty, Up, Down);
        commands.addAll(sendWorkingElevator(working));
        return commands;

    }

    private static List<Command> sendWorkingElevator(List<Elevator> working) {
        List<Command> commands = new ArrayList<>();
        for (Elevator e : working) {
            boolean stop = false;
            switch (e.status) {
                case "STOPPED":
                    if (prevStatus[e.id].equals("OPENED")) {
                        Call pass = e.passengers.get(0);
                        if (pass.start < pass.end) {
                            commands.add(new Command(e.id, Constants.UP));
                        } else {
                            commands.add(new Command(e.id, Constants.DOWN));
                        }
                    } else {
                        commands.add(new Command(e.id, Constants.OPEN));
                    }
                    break;
                case "UPWARD":
                    for (Call c : e.passengers) {
                        if (c.end == e.floor) {
                            stop = true;
                            break;
                        }
                    }
                    if (stop || upCalls[e.floor]) {
                        commands.add(new Command(e.id, Constants.STOP));
                    } else {
                        commands.add(new Command(e.id, Constants.UP));

                    }
                    break;
                case "DOWNWARD":
                    for (Call c : e.passengers) {
                        if (c.end == e.floor) {
                            stop = true;
                            break;
                        }
                    }
                    if (stop || downCalls[e.floor]) {
                        commands.add(new Command(e.id, Constants.STOP));
                    } else {
                        commands.add(new Command(e.id, Constants.DOWN));

                    }
                    break;
                case "OPENED":
                    List<Integer> exits = new ArrayList<>();
                    for (Call c : e.passengers) {
                        if (c.end == e.floor) {
                            exits.add(c.id);
                        }
                    }
                    if (exits.isEmpty()) {
                        boolean up = true;
                        Call p = e.passengers.get(0);
                        if (p.start > p.end) {
                            up = false;
                        }

                        List<Integer> enters = new ArrayList<>();
                        for (Call c : calls) {
                            if (up) {
                                if (c.start == e.floor && c.start < c.end) {
                                    enters.add(c.id);
                                }
                            } else {
                                if (c.start == e.floor && c.start > c.end) {
                                    enters.add(c.id);
                                }
                            }
                        }
                        Command comm;
                        if (enters.isEmpty()) {
                            comm = new Command(e.id, Constants.CLOSE);
                        } else {
                            comm = new Command(e.id, Constants.ENTER);
                            comm.call_ids = enters;
                        }
                        commands.add(comm);
                    } else {
                        Command comm = new Command(e.id, Constants.EXIT);
                        comm.call_ids = exits;
                        commands.add(comm);
                    }
                    break;
            }
        }
        return commands;
    }


    private static List<Command> sendEmptyElevator(List<Elevator> empty, Elevator Up,
        Elevator Down) {
        if (Up == null && bot != 26 && !empty.isEmpty()) {
            Up = empty.get(0);
            int diff = Math.abs(Up.floor - bot);
            findNearElevator(empty, Up, diff, bot);
        }
        if (Down == null && top != 0 && !empty.isEmpty()) {
            Down = empty.get(0);
            int diff = Math.abs(Down.floor - top);
            findNearElevator(empty, Down, diff, top);
        }
        List<Command> commands = new ArrayList<>();
        if (Up != null) {
            if (Up.floor < bot) {
                commands.add(new Command(Up.id, Constants.UP));
            } else if (Up.floor > bot) {
                commands.add(new Command(Up.id, Constants.DOWN));
            } else {
                switch (Up.status) {
                    case "STOPPED":
                        commands.add(new Command(Up.id, Constants.OPEN));
                        break;
                    case "UPWARD":
                    case "DOWNWARD":
                        commands.add(new Command(Up.id, Constants.STOP));
                        break;
                    case "OPENED":
                        List<Integer> ids = new ArrayList<>();
                        for (Call c : calls) {
                            if (c.start == Up.floor) {
                                if (c.start < c.end) {
                                    ids.add(c.id);
                                    if (ids.size() == 8) {
                                        break;
                                    }
                                }
                            }
                        }
                        Command comm = new Command(Up.id, Constants.ENTER);
                        comm.call_ids = ids;
                        commands.add(comm);
                        break;
                }
            }
        }
        if (Down != null) {
            if (Down.floor < top) {
                commands.add(new Command(Down.id, Constants.UP));
            } else if (Down.floor > top) {
                commands.add(new Command(Down.id, Constants.DOWN));
            } else {
                switch (Down.status) {
                    case "STOPPED":
                        commands.add(new Command(Down.id, Constants.OPEN));
                        break;
                    case "UPWARD":
                    case "DOWNWARD":
                        commands.add(new Command(Down.id, Constants.STOP));
                        break;
                    case "OPENED":
                        List<Integer> ids = new ArrayList<>();
                        for (Call c : calls) {
                            if (c.start == Down.floor) {
                                if (c.start > c.end) {
                                    ids.add(c.id);
                                }
                            }
                        }
                        Command comm = new Command(Down.id, Constants.ENTER);
                        comm.call_ids = ids;
                        commands.add(comm);
                        break;
                }
            }
        }
        for (Elevator e : empty) {
            Command comm;
            if (prevStatus[e.id].equals("OPENED")) {
                comm = new Command(e.id, Constants.CLOSE);
            } else {
                comm = new Command(e.id, Constants.STOP);
            }
            commands.add(comm);
        }
        return commands;
    }

    private static void findNearElevator(List<Elevator> empty, Elevator target, int diff,
        int floor) {
        for (int i = 1; i < empty.size(); i++) {
            Elevator e = empty.get(i);
            int nDiff = Math.abs(e.floor - floor);
            if (diff > nDiff) {
                target = e;
                diff = nDiff;
            }
        }
        empty.remove(target);
    }

    private static void checkCalls() { // 내려가는 가장 높은 층의 호출과 올라가는 가장 낮은 층의 호출을 확인
        top = 0;
        bot = 26;
        Arrays.fill(downCalls, false);
        Arrays.fill(upCalls, false);
        for (Call c : calls) {
            if (c.start > c.end) {
                top = Math.max(top, c.start);
                downCalls[c.start] = true;
            } else {
                bot = Math.min(bot, c.start);
                upCalls[c.start] = true;
            }
        }
    }

    private static void onCalls() {
        JSONObject response = HttpUtil.getInstance()
            .callApi(Constants.GET_ONCALLS, new JsonObject(), "GET", false);
        calls = parseCallList(response.getJSONArray("calls"));
        if (!elevators.isEmpty()) {
            for (Elevator e : elevators) {
                prevStatus[e.id] = e.status;
            }
        }
        elevators = parseElevatorList(response.getJSONArray("elevators"));
        isEnd = response.getBoolean("is_end");
    }

    private static List<Elevator> parseElevatorList(JSONArray elevators) {
        List<Elevator> tmp = new ArrayList<>();
        for (Object elevator : elevators) {
            tmp.add(new Elevator((JSONObject) elevator));
        }
        return tmp;
    }

    private static List<Call> parseCallList(JSONArray callArray) {
        List<Call> tmp = new ArrayList<>();
        for (Object call : callArray) {
            tmp.add(new Call((JSONObject) call));
        }
        return tmp;
    }


    private static void start(String user_key, int problemId, int number_of_elevators) {
        TokenManager.getInstance().createToken(user_key, problemId, number_of_elevators);
    }
}
