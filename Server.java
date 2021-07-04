package com.javarush.task.task30.task3008;

import com.javarush.task.task30.task3008.client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends  Thread{

    private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            
            while(true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();

                if(message.getType()!= MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("Получено сообщение от " + socket.getRemoteSocketAddress() + ". Тип сообщения не соответствует протоколу.");
                    continue;
                }
                String userName = message.getData();

                if(userName.isEmpty()) {
                    ConsoleHelper.writeMessage("Попытка подключения к серверу с пустым именем от " + socket.getRemoteSocketAddress());
                    continue;
                }

                    if(connectionMap.containsKey(userName)){
                        ConsoleHelper.writeMessage("Попытка подключения к серверу с уже используемым именем от " + socket.getRemoteSocketAddress());
                        continue;
                    }
                connectionMap.put(message.getData(),connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return userName;
                }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{

            for (Map.Entry<String, Connection> map: connectionMap.entrySet()){
                String nameOtherUser = map.getKey();
                if(userName.equals(nameOtherUser)) continue;
                connection.send(new Message(MessageType.USER_ADDED, nameOtherUser));
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true) {
                Message messageSet = connection.receive();
                if (messageSet.getType() != MessageType.TEXT) {
                    ConsoleHelper.writeMessage("Cообщение "+ connection.getRemoteSocketAddress() +" не является текстом");
                    continue;
                }
                Message messageGet = new Message(MessageType.TEXT, userName + ": " + messageSet.getData());
                sendBroadcastMessage(messageGet);
            }
        }

        @Override
        public void run() {
            System.out.println("Установленно новое соединение с " + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)){
                userName =  serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection,userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Сбой соеднинения с" + socket.getRemoteSocketAddress());
                }
            if(userName != null){
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));
            }



        }
    }
    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> map : connectionMap.entrySet()){
            try {
                map.getValue().send(message);
            } catch (IOException e) {
               ConsoleHelper.writeMessage("Не смогли отправить сообщение " + map.getValue().getRemoteSocketAddress());
            }
        }
    }


    public static void main(String[] args) throws IOException {

        int port = ConsoleHelper.readInt();


        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Cервер запущен");

            while(true){
                new Handler(serverSocket.accept()).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
