package com.uzok.uzokBot.discord.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uzok.uzokBot.utils.weather.dtos.WeatherForecastDto;
import com.uzok.uzokBot.utils.context.MessageEventContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
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
        example = "!w Moscow\n!w -i New-York\n!weather -f Novosibirsk";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        Options options = new Options();
        options.addOption("f", "forecast", false, "Return weather forecast");
        options.addOption("i", "image", false, "Return result as image");

        CommandLine commandLine = context.getCommandLine(options);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        HttpGet httpGet = new HttpGet(getRequestUrl(commandLine));

        try {
            HttpResponse response = httpClientBuilder.build().execute(httpGet);

            if (commandLine.hasOption("i") && !commandLine.hasOption("f")) {
                return image(context, response);
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            if (commandLine.hasOption("f")) {
                return forecast(context, sb.toString());
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

    private Mono<Void> image(MessageEventContext context, HttpResponse response){
        Header[] headers = response.getAllHeaders();
        boolean isImage = false;
        for (Header header : headers) {
            if (header.getName().equals("Content-Type") && header.getValue().contains("image/png")) {
                isImage = true;
                break;
            }
        }
        if (isImage) {
            return context.getChannel().flatMap(messageChannel -> messageChannel.createMessage(x -> {
                try {
                    x.addFile("forecast.png", response.getEntity().getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                    x.setContent("Что-то пошло не так");
                }
            })).then();
        }

        return Mono.empty();
    }

    private Mono<Void> forecast(MessageEventContext context, String in){
        JSONObject json = new JSONObject(in);
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        WeatherForecastDto weatherForecast = null;
        try {
            weatherForecast = jsonMapper.readValue(json.toString(), WeatherForecastDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.empty();
        }

        String resultString = String.format("Сейчас: %+d°C Ощущается как: %+d°C\nЗавтра: %+d°C .. %+d°C\nПослезавтра: %+d°C .. %+d°C",
                weatherForecast.current_condition.get(0).temp_C,
                weatherForecast.current_condition.get(0).FeelsLikeC,
                weatherForecast.weather.get(1).mintempC,
                weatherForecast.weather.get(1).maxtempC,
                weatherForecast.weather.get(2).mintempC,
                weatherForecast.weather.get(2).maxtempC);

        return context.getChannel().flatMap(messageChannel -> messageChannel.createMessage(resultString)).then();
    }

    private URI getRequestUrl(CommandLine commandLine) {
        String additionalPath = String.join(" ", commandLine.getArgs());
        if (commandLine.hasOption("i") && !commandLine.hasOption("f")) {
            additionalPath += ".png";
        }
        URIBuilder builder = new URIBuilder()
                .setScheme(baseScheme)
                .setHost(baseHost)
                .setPath(additionalPath.trim());
        if (commandLine.hasOption("f")) {
            builder.addParameter("format", "j1");
        } else {
            builder.addParameter("format", "3");
        }

        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
