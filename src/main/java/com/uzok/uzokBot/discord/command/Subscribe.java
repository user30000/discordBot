package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.SubscribeProcedure;
import com.uzok.uzokBot.utils.MessageEventContext;
import reactor.core.publisher.Mono;

public class Subscribe extends BaseCommand {

    Subscribe() {
        commandName = "sub";
        shortDescription = "Подписка на стримы пользователя";
        description = "Принимает на вход тэг пользователя.\nЕсли этот пользователь начинает трансляцию на Twitch.tv то бот отправит сообщение в личный чат об этом событии.";
        example = "!sub userName#0000";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        String streamerName = context.getArg();
        if (streamerName == null || streamerName.isEmpty() || !streamerName.matches("\\S+#\\d{4}")) {
            return Mono.empty();
        }
        try {
            new JavaToMySQL().executeCall(new SubscribeProcedure(context.getArg(), context.getAuthor().getId().asLong(), context.getAuthor().getTag()));
        } catch (Exception e) {
            return Mono.empty();
        }
        return context.getChannel().flatMap(channel -> channel.createMessage("Ты подписался на " + context.getArg()))
                .then();
    }
}
