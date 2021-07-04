package com.javarush.task.task30.task3008.client;

public class ClientGuiController extends Client{

    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    @Override
    public void run() {
        getSocketThread().run();
    }

    public String getServerAddress(){
        return view.getServerAddress();
    }

    public int getServerPort(){
        return view.getServerPort();
    }

    public String getUserName(){
        return view.getUserName();
    }

    public ClientGuiModel getModel(){
        return model;
    }

    // Методы в GuiSocketThread не переопределенны

    public class GuiSocketThread extends SocketThread{

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
        }

        @Override
        protected void informAboutAddingNewUser(String userName) {
            super.informAboutAddingNewUser(userName);
        }

        @Override
        protected void informAboutDeletingNewUser(String userName) {
            super.informAboutDeletingNewUser(userName);
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            super.notifyConnectionStatusChanged(clientConnected);
        }
    }
}
