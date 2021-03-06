package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.context.MessageEventContext;
import reactor.core.publisher.Mono;

public class InviteLink extends BaseCommand {
    public InviteLink() {
        commandNames = new String[]{"invite"};
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        if (context.isPrivateChannel()) {
            return context.getChannel().flatMap(messageChannel -> messageChannel.createMessage("Follow the link for invite me on your server: https://discord.com/oauth2/authorize?client_id=490771771977498626&scope=bot&permissions=3078"))
                    .then();
        }
        return Mono.empty();
    }
}
