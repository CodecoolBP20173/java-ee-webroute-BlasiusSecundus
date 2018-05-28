package com.codecool.webroute;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Router {

    private static String sanitizeInputString(String input) throws UnsupportedEncodingException {
        String sanitized = URLDecoder.decode(input, "UTF8");

        sanitized = sanitized.replaceAll("&", "&amp;");

        Map<String, String> htmlCharMap = new HashMap<>();
        htmlCharMap.put("<", "&lt;");
        htmlCharMap.put(">", "&gt;");
        htmlCharMap.put("\"", "&quot;");
        htmlCharMap.put("'", "&#039;");

        for(Map.Entry<String, String> entry :htmlCharMap.entrySet()){
            sanitized = sanitized.replaceAll(entry.getKey(), entry.getValue());
        }

        return sanitized;
    }

    private static Map<String, String> getFormData(HttpExchange exchange) throws IOException {
        Map<String, String> retval = new HashMap<>();

        Stream.of(new String(exchange.getRequestBody().readAllBytes(), UTF_8).split("&")).forEach( line -> {
            String[] splitLine = line.split("=");
            try {
                retval.put(
                        sanitizeInputString(splitLine[0]),
                        sanitizeInputString(splitLine[1])
                );
            } catch (UnsupportedEncodingException ignored) {

            }
        });

        return retval;
    }

    private static void sendResponse(String response, HttpExchange exchange, int code, String contentType) throws IOException {
        exchange.getResponseHeaders().add("Content-Type",contentType);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream responseStream = exchange.getResponseBody();
        responseStream.write(response.getBytes());
        responseStream.close();
    }

    private static void sendResponse(String response, HttpExchange exchange) throws IOException {
        sendResponse(response, exchange, 200, "text/html;charset=utf-8");
    }

    @WebRoute(methods = {"post"})
    public static void postTest(HttpExchange exchange) throws IOException {
        Map<String, String> formData = getFormData(exchange);

        String message = formData.get("name")+" posted this: "+formData.get("message");

        sendResponse(message, exchange);
    }

    @WebRoute
    public static void homepage(HttpExchange exchange) throws IOException {
        sendResponse("<form method=\"post\" action=\"/\">\n" +
                "    <fieldset>\n" +
                "        <legend>Send your message</legend>\n" +
                "        <div>\n" +
                "            <label for=\"name\">Name:</label>\n" +
                "            <input id=\"name\" name=\"name\" placeholder=\"Your name\">\n" +
                "        </div>\n" +
                "        <div>\n" +
                "            <label for=\"message\">Message:</label>\n" +
                "            <textarea id=\"message\" name=\"message\" placeholder=\"Your message\"></textarea>\n" +
                "        </div>\n" +
                "        <button type=\"submit\">Submit</button>\n" +
                "    </fieldset>\n" +
                "</form>", exchange);
    }

    @WebRoute(path="/something")
    public static void something(HttpExchange exchange) throws IOException {
        sendResponse("Something returned", exchange);
    }

    @WebRoute(path="/parametrized/<parameterName>")
    public static void paremtrizedRoute(HttpExchange exchange, String parameterName) throws IOException {
        sendResponse("Something parametrized:"+sanitizeInputString(parameterName), exchange);
    }

}
