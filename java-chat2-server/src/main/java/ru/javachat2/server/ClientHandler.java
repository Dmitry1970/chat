package ru.javachat2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {    // вся логика общения сервера с клиентом

    private Server server; // ссылка на сервер, на котором работает клиент
    private Socket socket; //запоминание сокета(общение с конкретным клиентом)
    private String username;
    private DataOutputStream out;
    private DataInputStream in;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {  // поток общения с клиентом
                logic();
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {   //отправка сообщения клиенту, с которым общаемся
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void logic() {
        try {
            while (!consumeAuthorizeMessage(in.readUTF())) ;
            while (consumeRegularMessage(in.readUTF())) ;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Клиент " + username + " отключился");
            server.unsubscribe(this);
            closeConnection();
        }
    }

    private boolean consumeRegularMessage(String inputMessage) {
        if (inputMessage.startsWith("/")) {
            if (inputMessage.equals("/exit")) { // команда от клиента на отключение
                sendMessage("/exit");  // ответ клиенту
                return false;
            }
            if (inputMessage.startsWith("/w ")) {   // /w bob hello world!
                String[] tokens = inputMessage.split("\\s+", 3);
                server.sendPersonalMessage(this, tokens[1], tokens[2]);
            }
            return true;
        }
        server.broadcastMessage(username + ": " + inputMessage);
        return true;
    }


    public boolean consumeAuthorizeMessage(String message) {
        if (message.startsWith("/auth ")) {    // auth Bob
            String[] tokens = message.split("\\s+");
            if (tokens.length == 1) {
                sendMessage("SERVER: Вы не указали имя пользователя");
                return false;
            }
            if (tokens.length > 2) {
                sendMessage("SERVER: имя пользователя не может состоять из нескольких слов");
                return false;
            }
            String selectedUsername = tokens[1];
            if (server.isUsernameUsed(selectedUsername)) {
                // если имя клиента уже используется
                sendMessage("SERVER:  Данное имя пользователя уже занято");
                return false;
            }
            username = selectedUsername;
            sendMessage("/authok");
            server.subscribe(this);
            return true;
        } else {
            sendMessage("SERVER: Вам необходимо авторизоваться");
            return false;
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
