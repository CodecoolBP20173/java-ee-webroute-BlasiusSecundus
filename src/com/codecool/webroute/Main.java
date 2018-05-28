package com.codecool.webroute;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        WebServer server = new WebServer(8000, Router.class);
        server.start();
    }
}
