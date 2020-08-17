package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.MessageEventContext;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class Weather extends BaseCommand {
    private final static String baseScheme = "http";
    private final static String baseHost = "wttr.in";

    Weather() {
        commandNames = new String[]{"weather", "w"};
        shortDescription = "Прогноз погоды по городам";
        description = "Возращает прогноз погоды для указанного города. Пользуется ресурсом: https://github.com/chubin/wttr.in";
        example = "!w userName#0000";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        HttpGet httpGet = new HttpGet(getRequestUrl(context.getArg()));
        try {
            HttpResponse response = httpClientBuilder.build().execute(httpGet);
            Header[] headers = response.getAllHeaders();
            boolean isImage = false;
            for (Header header : headers) {
                if (header.getName().equals("Content-Type") && header.getValue().contains("image/png")) {
                    isImage = true;
                }
            }
            if (isImage) {
                return context.getChannel().flatMap(messageChannel -> messageChannel.createMessage(x -> {
                    try {
                        x.addFile(context.getArg(), response.getEntity().getContent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })).then();
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            String resultString = sb.toString();
            if (resultString.length() >= 2000) {
                resultString = "Что-то пошло не так";
            }
            String finalResultString = resultString;
            return context.getChannel().flatMap(messageChannel -> messageChannel.createMessage(finalResultString)).then();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Mono.empty();
    }

    private URI getRequestUrl(String additionalPath) {
        URIBuilder builder = new URIBuilder()
                .setScheme(baseScheme)
                .setHost(baseHost)
                .setPath(additionalPath);
        builder.addParameter("format", "3");

        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
