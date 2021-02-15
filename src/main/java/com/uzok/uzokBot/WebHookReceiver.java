package com.uzok.uzokBot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.uzok.uzokBot.dataBase.GetSubscribersByUserTag;
import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.ResetSubscriptionTimeProcedure;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.dtos.Stream;
import com.uzok.uzokBot.twitch.responses.GamesResponse;
import com.uzok.uzokBot.twitch.responses.UsersResponse;
import com.uzok.uzokBot.twitch.responses.WebHookStreamResponse;
import com.uzok.uzokBot.utils.Logger;
import com.uzok.uzokBot.utils.Prop;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

class WebHookReceiver {

    private static WebHookReceiver instance;

    static void start() {
        if (instance == null) {
            try {
                instance = new WebHookReceiver();
            } catch (Exception e) {
                Logger.write(e.getMessage());
            }
        }
    }

    private WebHookReceiver() throws IOException {
        String serverAddress = Prop.getProp("webHookAddress");
        int serverPort = Prop.getInt("webHookPort");
        HttpServer server = HttpServer.create(new InetSocketAddress(serverAddress, serverPort), 0);
        server.createContext("/streamHook", new WebHookHttpHandler());
        server.createContext("/pingHook", new PingWebHookHttpHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
    }

    private static class WebHookHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Logger.write("WebHookHandler receive message");
            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    Map<String, String> query_pairs = new LinkedHashMap<>();
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

                    String streamerId = query_pairs.get("hub.topic").replaceAll("[^\\d]", "");
                    UsersResponse streamer = Client.getInstance().getUserInfo(Integer.parseInt(streamerId));
                    JavaToMySQL.getInstance().executeCall(new ResetSubscriptionTimeProcedure(streamer.data.get(0).display_name));

                    Logger.write("Response on twitch subscription challenge request\n" + httpExchange.getRequestURI().getQuery());
                    break;

                case "POST":
                    Logger.write("Twitch send post request with channel changing data");
                    httpExchange.sendResponseHeaders(200, 0);
                    String result = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()))
                            .lines().collect(Collectors.joining("\n"));

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    WebHookStreamResponse streamResponse = objectMapper.readValue((new JSONObject(result)).toString(), WebHookStreamResponse.class);

                    if (streamResponse.data.isEmpty()) {
                        Logger.write("Stream data is empty");
                        return;
                    }

                    Stream stream = streamResponse.data.get(0);
                    String gameId = stream.game_id;
                    GamesResponse gamesResponse = Client.getInstance().getGameInfo(gameId);

                    List<?> subs = (List<?>) (JavaToMySQL.getInstance().executeQuery(new GetSubscribersByUserTag(stream.user_name)));

                    List<Long> guildsIds = new LinkedList<>();
                    List<Long> channelsIds = new LinkedList<>();
                    List<Long> everyoneChannelsIds = new LinkedList<>();

                    subs.forEach(subscriber -> {
                        GetSubscribersByUserTag.subscriber castedSub = (GetSubscribersByUserTag.subscriber) subscriber;
                        guildsIds.add(castedSub.guidSnowflake);
                        channelsIds.add(castedSub.channelSnowflake);
                        if (castedSub.isEveryone) {
                            everyoneChannelsIds.add(castedSub.channelSnowflake);
                        }
                    });

                    DiscordBot.getDiscordClient()
                            .getGuilds()
                            .filter(guild -> guildsIds.contains(guild.getId().asLong()))
                            .flatMap(Guild::getChannels)
                            .filter(channel -> channelsIds.contains(channel.getId().asLong()))
                            .flatMap(channel ->
                                    ((MessageChannel) channel).createMessage(message -> {
                                        if (everyoneChannelsIds.contains(channel.getId().asLong())) {
                                            message.setContent("@Everyone");
                                        }
                                        message.setEmbed(spec -> spec.setColor(Color.of(255, 0, 0))
                                                .setAuthor(stream.user_name, null, null)
                                                .setImage(stream.thumbnail_url
                                                        .replace("{width}x{height}", "440x248")
                                                        .concat("?r=")
                                                        .concat(String.valueOf(System.currentTimeMillis())))
                                                .setTitle(stream.title)
                                                .setUrl("https://www.twitch.tv/" + stream.user_name)
                                                .addField("Стримит", stream.game_name, true)
                                                .setThumbnail(gamesResponse.data.get(0).box_art_url.replace("{width}x{height}", "285x380"))
                                                .setTimestamp(Instant.now()));
                                    })
                            ).blockLast();
                    Logger.write("Send " + subs.size() + " messages about stream starting; Username = " + stream.user_name);
                    break;
            }
        }
    }

    private static class PingWebHookHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if ("GET".equals(httpExchange.getRequestMethod())) {
                Map<String, String> query_pairs = new LinkedHashMap<>();
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
                Logger.write("Get request by Twitch ping WebHook\n" + httpExchange.getRequestURI().getQuery());
            }
        }
    }
}
