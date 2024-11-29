package com.tmv.inbound;

import java.net.Socket;

public interface RequestHandler extends Runnable {
    public void setSocket(Socket clientSocket);
}
