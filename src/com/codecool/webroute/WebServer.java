package com.codecool.webroute;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.stream.Stream;

public class WebServer {

    private int port;
    private Class router;
    private HttpServer server;


    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            try {
                //finding route method
                Optional<Method> routeMethod = Stream.of(router.getMethods()).filter(method -> {
                    WebRoute route = method.getAnnotation(WebRoute.class);
                    if(route == null) //not a route method
                        return false;

                    //not set to handle this type of request
                    if(Stream.of(route.methods()).map(String::toLowerCase).noneMatch(httpMethod -> httpMethod.equals(exchange.getRequestMethod().toLowerCase())))
                        return false;

                    //path does not match
                    if(!exchange.getRequestURI().getPath().toLowerCase().equals(route.path()))
                        return false;

                    //else we found the route
                    return true;
                }).findFirst();

                if(routeMethod.isPresent())
                    routeMethod.get().invoke(null, exchange);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public WebServer(int port, Class router){
        this.port = port;
        this.router = router;
    }
}
