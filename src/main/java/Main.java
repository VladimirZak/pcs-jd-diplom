import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        //System.out.println(engine.search("бизнес"));

        // здесь создайте сервер, который отвечал бы на нужные запросы
        // слушать он должен порт 8989
        // отвечать на запросы /{word} -> возвращённое значение метода search(word) в JSON-формате
        int port = 8989;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept(); // ждем подключения
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                ) {
                    String word = in.readLine();
                    if (word != null && !word.isEmpty()) {
                        out.println(listToJson(engine.search(word)));
                    } else {
                        out.printf("Поступило неверное значение");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу запустить сервер");
            e.printStackTrace();
        }
    }

    private static String listToJson(List<PageEntry> list) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Type listType = new TypeToken<List<PageEntry>>() {
        }.getType();
        return gson.toJson(list, listType);
    }
}