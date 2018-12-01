package com.chat.thread;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatServerProcessThread implements Runnable{

    private String nickname = null;
    private Socket socket = null;
    List<PrintWriter> listWirter = null;

    public ChatServerProcessThread(Socket socket, List<PrintWriter> listWirter) {
        this.socket = socket;
        this.listWirter = listWirter;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8) );
            PrintWriter printWriter = new PrintWriter( new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            while(true) {
                String request = bufferedReader.readLine();

                if( request == null ) {
                    consoleLog("클라이언트로부터 연결 끊김");
                    doQuit(printWriter);
                    break;
                }

                String[] tokens = request.split(":");
                if("join".equals(tokens[0]))
                    doJoin(tokens[1], printWriter);
                else if("message".equals(tokens[0]))
                    doMessage(tokens[1]);
                else if("quit".equals(tokens[0])) {
                    doQuit(printWriter);
                }
            }
        } catch (IOException e) {
            consoleLog(this.nickname + " 님이 채팅방을 나갔습니다.");
        }
    }

    private void doQuit(PrintWriter writer) {
        removeWriter(writer);

        String data = this.nickname + " 님이 퇴장했습니다.";
        broadCast(data);


    }

    private void removeWriter(PrintWriter writer) {
        synchronized (listWirter) {
            listWirter.remove(writer);
        }
    }

    private void doMessage(String data) {
        if (data.isEmpty()) data = "";
        broadCast(this.nickname + " : " + data);
    }

    private void doJoin(String nickname, PrintWriter writer) {
        this.nickname = nickname;

        String data = nickname + " 님이 입장하였습니다.";
        broadCast(data);

        addWriter(writer);
    }

    private void addWriter(PrintWriter writer) {
        synchronized (listWirter) {
            listWirter.add(writer);
        }
    }

    private void broadCast(String data) {
        synchronized (listWirter) {
            for(PrintWriter writer : listWirter) {
                writer.println(data);
                writer.flush();
            }
        }
    }

    private void consoleLog(String log) {
        System.out.println(log);
    }
}
