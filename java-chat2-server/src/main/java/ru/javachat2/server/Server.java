package ru.javachat2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {   // подключение клиента

    private List<ClientHandler> clients;

    public Server() {
        try {
            this.clients = new ArrayList<>();
            ServerSocket serverSocket = new ServerSocket(8189);
            System.out.println("Сервер запущен. Ожидаем подключение клиентов...");
            while (true) {
                Socket socket = serverSocket.accept();  // ждём подключения клиентов
                System.out.println("Подключился новый клиент");
                new ClientHandler(this, socket); // создаём обработчик клиентов
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler c) {  // подписка на рассылку(добавили клиента)
        broadcastMessage("В чат зашел пользователь " + c.getUsername());
        clients.add(c);
        broadcastClientList();


    }

    public synchronized void unsubscribe(ClientHandler c) { // удалили клиента из списка (рассылки)
        clients.remove(c);
        broadcastMessage("Из чата вышел пользователь " + c.getUsername());
        broadcastClientList();
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void broadcastClientList() {
        StringBuilder builder = new StringBuilder(clients.size() * 10);
        builder.append("/clients_list ");
        for (ClientHandler c : clients) {
            builder.append(c.getUsername()).append(" ");
        }
        String clientsListStr = builder.toString();
        broadcastMessage(clientsListStr);
    }

    public synchronized boolean isUsernameUsed(String username) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equalsIgnoreCase(username)) {
                return true;
            }

        }
        return false;
    }


    public synchronized void sendPersonalMessage(ClientHandler sender, String receiverUsername, String message) {
        if(sender.getUsername().equalsIgnoreCase(receiverUsername)) {
            sender.sendMessage("Нельзя отправлять личные сообщения самому себе");
            return;
        }
        for (ClientHandler c : clients) {
            if(c.getUsername().equalsIgnoreCase(receiverUsername)) {
                c.sendMessage("от " + sender.getUsername() + ": " + message);
                sender.sendMessage("пользователю " + receiverUsername + ": " + message);
                return;
            }
        }
        sender.sendMessage("Пользователь " + receiverUsername + " не в сети");
    }

}
