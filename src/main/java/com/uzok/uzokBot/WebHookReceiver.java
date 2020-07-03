package com.uzok.uzokBot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.uzok.uzokBot.discord.activity.UserStartStreamEvent;
import com.uzok.uzokBot.dataBase.GetSubscribersByUserTag;
import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.dtos.Stream;
import com.uzok.uzokBot.twitch.responses.GamesResponse;
import com.uzok.uzokBot.twitch.responses.WebHookStreamResponse;
import com.uzok.uzokBot.utils.Prop;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;
import org.apache.tomcat.jni.Proc;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WebHookReceiver {

    private static WebHookReceiver instance;

    public static void start() {
        if (instance == null) {
            try {
                instance = new WebHookReceiver();
            } catch (Exception e) {
                //
            }
        }
    }

    private WebHookReceiver() throws IOException {
        String serverAddress = Prop.getProp("webHookAddress");
        int serverPort = Prop.getInt("webHookPort");
        HttpServer server = HttpServer.create(new InetSocketAddress(serverAddress, serverPort), 0);
        server.createContext("/streamHook", new WebHookHttpHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
    }

    private static class WebHookHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    Map<String, String> query_pairs = new LinkedHashMap<>();
                    System.out.println(httpExchange.getRequestURI().getQuery());
                    String[] pairs = httpExchange.getRequestURI().getQuery().split("&");
                    for (String pair : pairs) {
                        int ind = pair.indexOf("=");
                        query_pairs.put(pair.substring(0, ind), pair.substring(ind + 1));
                    }
                    OutputStream response = httpExchange.getResponseBody();
                    String challenge = query_pairs.get("hub.challenge");
                    httpExchange.sendResponseHeaders(200, challenge.length());
                    response.write(challenge.getBytes());
                    response.flush();
                    response.close();
                    break;

                case "POST":
                    httpExchange.sendResponseHeaders(200, 0);
                    String result = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()))
                            .lines().collect(Collectors.joining("\n"));

                    WebHookStreamResponse streamResponse = new ObjectMapper().readValue((new JSONObject(result)).toString(), WebHookStreamResponse.class);

                    if (streamResponse.data.isEmpty()) {
                        return;
                    }

                    Stream stream = streamResponse.data.get(0);
                    String gameId = stream.game_id;
                    GamesResponse gamesResponse = Client.getInstance().getGameInfo(gameId);

                    String userTag = UserStartStreamEvent.getDiscordTagByTwitchName(stream.user_name);
                    if (userTag == null) {
                        return;
                    }

                    List<String> subs = (List<String>) (new JavaToMySQL().executeQuery(new GetSubscribersByUserTag(userTag)));

                    DiscordBot.getDiscordClient().getUsers().filter(User -> subs.contains(User.getTag())).flatMap(User::getPrivateChannel)
                            .flatMap(channel -> channel.createEmbed(
                                    spec -> spec.setColor(new Color(255, 0, 0))
                                            .setAuthor(stream.user_name, null, null)//, ANY_URL, IMAGE_URL)
                                            .setImage(stream.thumbnail_url.replace("{width}x{height}", "440x248"))
                                            .setTitle(stream.title)
                                            .setUrl("https://www.twitch.tv/" + stream.user_name)
                                            .addField("Стримит", gamesResponse.data.get(0).name, true)
                                            .setThumbnail(gamesResponse.data.get(0).box_art_url.replace("{width}x{height}", "285x380"))
                                            .setFooter("Для отписки напиши !unsub " + userTag, null)
                                            .setTimestamp(Instant.now()))).blockLast();

                    break;
            }
        }
    }
}
