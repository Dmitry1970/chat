package ru.javachat2;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;


public class Controller {


    @FXML
    TextArea chatArea;  //многострочное текстовое поле для сообщений чата

    @FXML
    TextField messageField, usernameField;// поле отправки сообщений

    @FXML
    HBox authPanel, msgPanel;

    @FXML
    ListView<String> clientsListView;

    private Socket socket;  // соединение с сервером
    private DataInputStream in;
    private DataOutputStream out;




    public void setAuthorized(boolean authorized) {  // переключение панелей чата
        msgPanel.setVisible(authorized);
        msgPanel.setManaged(authorized);
        authPanel.setVisible(!authorized);
        authPanel.setManaged(!authorized);
        clientsListView.setVisible(authorized);
        clientsListView.setManaged(authorized);

    }

    public void sendMessage() {   // нажимаем на кнопку для отправки сообщения на сервер
        try {
            out.writeUTF(messageField.getText());  // отправка сообщения серверу
            messageField.clear(); // очищаем поле сообщений
            messageField.requestFocus(); // установка курсора на поле отправки сообщения
        } catch (IOException e) {
            showError("Невозможно отправить сообщение на сервер");  // если невозможно отправить запрос на сервер
        }
    }

    public void sendCloseRequest() {   // запрос на сервер на отключение соединения
        try {
            if (out != null && !socket.isClosed()) {
                out.writeUTF("/exit");  // отправка сообщения серверу на отключение
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {   // нажимаем на кнопку для отправки сообщения на сервер
        connect();
        try {
            out.writeUTF("/auth " + usernameField.getText());
            usernameField.clear();
        } catch (IOException e) {
            showError("Невозможно отправить запрос авторизации на сервер");
        }
    }

    public void connect() {
        if (socket != null && !socket.isClosed()) { // проверка есть ли активное соединение
            return;
        }
        try {
            socket = new Socket("localhost", 8189); // подключение к серверу
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> mainClientLogic()).start();
        } catch (IOException e) {
            showError("Невозможно подключиться к серверу");
        }
    }


    private void mainClientLogic() {
        try {
            while (true) {  //цикл ожидания сообщений от сервера
                String inputMessage = in.readUTF(); // блокир. операция - ждём сообщение от сервера
                if (inputMessage.equals("/exit")) {
                    closeConnection();
                }
                if (inputMessage.equals("/authok")) {
                    setAuthorized(true);
                    break;
                }
                chatArea.appendText(inputMessage + "\n");
            }
            while (true) {   // читаем сообщения от сервера
                String inputMessage = in.readUTF();
                if (inputMessage.startsWith("/")) {  // служебное сообщение от сервера
                    if (inputMessage.equals("/exit")) {
                        break;
                    }
                    if (inputMessage.startsWith("/clients_list ")) {
                        Platform.runLater(() -> {    // thread javaFX
                            String[] tokens = inputMessage.split("\\s+");
                            clientsListView.getItems().clear();
                            for (int i = 1; i < tokens.length; i++) {
                                clientsListView.getItems().add(tokens[i]);
                            }
                        });
                    }
                    continue;
                }
                chatArea.appendText(inputMessage + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        setAuthorized(false);
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

    public void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }


    public void clientsListDoubleClick(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2) {
            String selectedUser = clientsListView.getSelectionModel().getSelectedItem();
            messageField.setText("/w " + selectedUser + " ");
            messageField.requestFocus();  //выделяет весь текст
            messageField.selectEnd(); //курсор на конец текста
        }

    }
}
