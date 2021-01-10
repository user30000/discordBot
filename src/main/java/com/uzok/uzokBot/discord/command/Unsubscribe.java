package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.UnsubscribeProcedure;
import reactor.core.publisher.Mono;
import com.uzok.uzokBot.utils.MessageEventContext;

public class Unsubscribe extends BaseCommand {

    Unsubscribe() {
        commandNames = new String[]{"unsub"};
        shortDescription = "Отписка от стримов пользователя";
        description = "Принимает на вход название канала.\nОтписка от сообщений о начале стрима на канале";
        example = "!unsub twitchuser";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        String twitchChannel = context.getArg();
        if (context.isPrivateChannel() || !context.isOwner() || twitchChannel == null || twitchChannel.isEmpty()) {
            return Mono.empty();
        }
        try {
            JavaToMySQL.getInstance().executeCall(new UnsubscribeProcedure(context.getArg(), context.getGuildId().get().asLong(), context.getChannelId().asLong()));
        }catch (Exception e){
            return Mono.empty();
        }
        return context.getChannel().flatMap(channel -> channel.createMessage("Ты отписался от " + context.getArg()))
                .then();
    }
}
