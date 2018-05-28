package com.codecool.webroute;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WebServer {

    private int port;
    private Class router;
    private HttpServer server;



    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        List<Object> routeMethodInvokeParams = new ArrayList<>();

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

                    //extracting parameters, if any
                    Pattern parameterPattern = Pattern.compile("<([a-zA-Z_][a-zA-Z0-9_]+?)>");
                    Matcher parameterMatcher = parameterPattern.matcher(route.path());


                    List<String> parameterNames = new ArrayList<>();

                    String finalUrlPattern = route.path();

                    while(parameterMatcher.find()){
                        String parameterName = parameterMatcher.group(1);
                        parameterNames.add(parameterName);

                        finalUrlPattern = finalUrlPattern.replaceAll("<("+parameterName+")>", "(?<$1>.*)");

                    }

                    Pattern pattern = Pattern.compile(finalUrlPattern);

                    Matcher matcher = pattern.matcher(exchange.getRequestURI().getPath());

                    if(!matcher.matches())//path does not match
                        return false;

                    routeMethodInvokeParams.clear();
                    routeMethodInvokeParams.add(exchange);

                    for(String parameterName : parameterNames){
                        routeMethodInvokeParams.add(matcher.group(parameterName));
                    }
                    //we found the route
                    return true;
                }).findFirst();

                if(routeMethod.isPresent())
                    routeMethod.get().invoke(null, routeMethodInvokeParams.toArray());

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
