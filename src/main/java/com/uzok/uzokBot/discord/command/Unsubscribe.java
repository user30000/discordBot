package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.UnsubscribeProcedure;
import reactor.core.publisher.Mono;
import com.uzok.uzokBot.utils.MessageEventContext;

public class Unsubscribe extends BaseCommand {

    public Unsubscribe() {
        commandName = "unsub";
        shortDescription = "Отписка от стримов пользователя";
        description = "Принимает на вход тэг пользователя.\nОтписка от личных сообщений о начале стрима данного пользователя";
        example = "!unsub userName#0000";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        String streamerName = context.getArg();
        if (streamerName == null || streamerName.isEmpty() || !streamerName.matches("\\S+#\\d{4}")) {
            return Mono.empty();
        }
        try {
            new JavaToMySQL().executeCall(new UnsubscribeProcedure(context.getArg(), context.getAuthor().getTag()));
        }catch (Exception e){
            return Mono.empty();
        }
        return context.getChannel().flatMap(channel -> channel.createMessage("Ты отписался от " + context.getArg()))
                .then();
    }
}
