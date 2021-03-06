package com.uzok.uzokBot.twitch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uzok.uzokBot.twitch.responses.GamesResponse;
import com.uzok.uzokBot.twitch.responses.StreamsResponse;
import com.uzok.uzokBot.twitch.responses.UserFollowsResponse;
import com.uzok.uzokBot.twitch.responses.UsersResponse;
import com.uzok.uzokBot.utils.Logger;
import com.uzok.uzokBot.utils.Prop;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Client {
    private static Client instance;

    private final static String baseScheme = "https";
    private final static String baseHost = "api.twitch.tv";
    private final static String basePath = "/helix/";

    private Token tkn;
    private String client_id;
    private HttpClientBuilder httpClientBuilder;

    private static ObjectMapper objectMapper;
    final private String grant_type;

    private Client() {
        grant_type = "client_credentials";
        httpClientBuilder = HttpClientBuilder.create();

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void build(String clientId, String clientSecret) throws IOException {
        this.client_id = clientId;

        HttpPost post = new HttpPost("https://id.twitch.tv/oauth2/token");
        List<BasicNameValuePair> nameValuePairs = new ArrayList<>(3);
        nameValuePairs.add(new BasicNameValuePair("client_id", client_id));
        nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
        nameValuePairs.add(new BasicNameValuePair("grant_type", grant_type));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpClientBuilder.build().execute(post);

        JSONObject json = parseResponse(response);
        this.tkn = objectMapper.readValue(json.toString(), Token.class);
    }

    public UsersResponse getUserInfo(String userName) throws IOException {
        List<NameValuePair> logins = new ArrayList<>();

        logins.add(new BasicNameValuePair("login", userName));
        URI requestUri = getRequestUrl("users", logins);

        JSONObject json = executeGetRequest(requestUri);
        return objectMapper.readValue(json.toString(), UsersResponse.class);
    }

    public UsersResponse getUserInfo(int userId) throws IOException {
        List<NameValuePair> ids = new ArrayList<>();

        ids.add(new BasicNameValuePair("id", String.valueOf(userId)));
        URI requestUri = getRequestUrl("users", ids);

        JSONObject json = executeGetRequest(requestUri);
        return objectMapper.readValue(json.toString(), UsersResponse.class);
    }

    public UserFollowsResponse getUserFollowers(String toId) throws IOException {
        List<NameValuePair> userIds = new ArrayList<>();

        userIds.add(new BasicNameValuePair("to_id", toId));
        URI requestUri = getRequestUrl("users/follows", userIds);

        JSONObject json = executeGetRequest(requestUri);
        return objectMapper.readValue(json.toString(), UserFollowsResponse.class);
    }

    public StreamsResponse getStreamInfo(String userName) throws IOException {
        List<NameValuePair> logins = new ArrayList<>();

        logins.add(new BasicNameValuePair("user_login", userName));
        URI requestUri = getRequestUrl("streams", logins);

        JSONObject json = executeGetRequest(requestUri);
        return objectMapper.readValue(json.toString(), StreamsResponse.class);
    }

    public GamesResponse getGameInfo(String gameId) throws IOException {
        List<NameValuePair> gameIds = new ArrayList<>();

        gameIds.add(new BasicNameValuePair("id", gameId));
        URI requestUri = getRequestUrl("games", gameIds);

        JSONObject json = executeGetRequest(requestUri);
        return objectMapper.readValue(json.toString(), GamesResponse.class);
    }

    public void postSubOnStreamChange(String userId) throws IOException {
        postSubOnStreamChange(userId, Prop.getProp("sub_time"));
    }

    public void postSubOnStreamChange(String userId, String time_s) throws IOException {
        HttpPost post = new HttpPost("https://api.twitch.tv/helix/webhooks/hub");
        List<BasicNameValuePair> nameValuePairs = new ArrayList<>(4);
        nameValuePairs.add(new BasicNameValuePair("hub.callback", "http://37.193.12.82:" + Prop.getProp("webHookPort") + "/streamHook"));
        nameValuePairs.add(new BasicNameValuePair("hub.mode", "subscribe"));
        nameValuePairs.add(new BasicNameValuePair("hub.topic", "https://api.twitch.tv/helix/streams?user_id=" + userId));
        nameValuePairs.add(new BasicNameValuePair("hub.lease_seconds", time_s));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        prepareRequest(post);
        httpClientBuilder.build().execute(post);
        Logger.write("Initiate subscribe on twitch channel update. UserId = " + userId);
    }

    public void postUnsubOnStreamChange(String userId) throws IOException {
        HttpPost post = new HttpPost("https://api.twitch.tv/helix/webhooks/hub");
        List<BasicNameValuePair> nameValuePairs = new ArrayList<>(4);
        nameValuePairs.add(new BasicNameValuePair("hub.callback", "http://37.193.12.82:" + Prop.getProp("webHookPort") + "/streamHook"));
        nameValuePairs.add(new BasicNameValuePair("hub.mode", "unsubscribe"));
        nameValuePairs.add(new BasicNameValuePair("hub.topic", "https://api.twitch.tv/helix/streams?user_id=" + userId));
        nameValuePairs.add(new BasicNameValuePair("hub.lease_seconds", Prop.getProp("sub_time")));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        prepareRequest(post);
        httpClientBuilder.build().execute(post);
        Logger.write("Initiate subscribe on twitch channel update. UserId = " + userId);
    }

    public void postPingSubOnStreamChange() throws IOException {
        HttpPost post = new HttpPost("https://api.twitch.tv/helix/webhooks/hub");
        List<BasicNameValuePair> nameValuePairs = new ArrayList<>(4);
        nameValuePairs.add(new BasicNameValuePair("hub.callback", "http://37.193.12.82:" + Prop.getProp("webHookPort") + "/pingHook"));
        nameValuePairs.add(new BasicNameValuePair("hub.mode", "subscribe"));
        nameValuePairs.add(new BasicNameValuePair("hub.topic", "https://api.twitch.tv/helix/streams?user_id=0"));
        nameValuePairs.add(new BasicNameValuePair("hub.lease_seconds", "0"));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        prepareRequest(post);
        httpClientBuilder.build().execute(post);

        Logger.write("Initiate fake subscribe on twitch channel update for ping");
    }

    private JSONObject executeGetRequest(URI requestUri) throws IOException {
        HttpGet getRequest = new HttpGet(requestUri);
        prepareRequest(getRequest);
        HttpResponse response = httpClientBuilder.build().execute(getRequest);

        return parseResponse(response);
    }

    private void prepareRequest(HttpPost postRequest) {
        postRequest.addHeader("Authorization", new StringJoiner(" ").add("Bearer").add(tkn.access_token).toString());
        postRequest.addHeader("Client-ID", this.client_id);
    }

    private void prepareRequest(HttpGet getRequest) {
        getRequest.addHeader("Authorization", new StringJoiner(" ").add("Bearer").add(tkn.access_token).toString());
        getRequest.addHeader("Client-ID", this.client_id);
    }

    private JSONObject parseResponse(HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return new JSONObject(sb.toString());
    }

    private URI getRequestUrl(String additionalPath, List<NameValuePair> params) {
        URIBuilder builder = new URIBuilder()
                .setScheme(baseScheme)
                .setHost(baseHost)
                .setPath(basePath.concat(additionalPath));

        if (params != null && !params.isEmpty()) {
            builder.addParameters(params);
        }

        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
