package com.tmv.ingest;

import java.net.Socket;

public interface RequestHandler extends Runnable {
    public void setSocket(Socket clientSocket);
}
