package com.uzok.uzokBot.utils;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.apache.commons.cli.*;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class MessageEventContext {
    private final MessageCreateEvent event;
    private final String commandName;
    @Nullable
    private final String arg;

    public MessageEventContext(MessageCreateEvent event) {
        this.event = event;
        final String[] splittedContent = event.getMessage().getContent().split(" ", 2);
        commandName = splittedContent[0].substring(1).toLowerCase();
        arg = splittedContent.length > 1 ? splittedContent[1] : "";
    }

    public MessageCreateEvent getEvent() {
        return this.event;
    }

    public Mono<Guild> getGuild() {
        return this.event.getGuild();
    }

    public Optional<Snowflake> getGuildId() {
        return this.event.getGuildId();
    }

    public Mono<MessageChannel> getChannel() {
        return this.getMessage().getChannel();
    }

    public Snowflake getChannelId() {
        return this.getMessage().getChannelId();
    }

    public boolean isPrivateChannel() {
        return !getGuildId().isPresent();
    }

    public Message getMessage() {
        return this.event.getMessage();
    }

    public Optional<Member> getMember() {
        return this.event.getMember();
    }

    public User getAuthor() {
        return this.getMessage().getAuthor().orElseThrow(NullPointerException::new);
    }

    public boolean isOwner() {
        return getMember().equals(getGuild().flatMap(Guild::getOwner).blockOptional());
    }

    public String getUsername() {
        return this.getAuthor().getUsername();
    }

    public String getAvatarUrl() {
        return this.getAuthor().getAvatarUrl();
    }

    public String getCommandName() {
        return commandName;
    }

    public String getArg() {
        return arg;
    }

    public CommandLine getCommandLine() {
        Options options = new Options();
        return getCommandLine(options);
    }

    public CommandLine getCommandLine(Options options) {
        CommandLineParser parser = new DefaultParser();
        try {
            if (options == null) {
                return getCommandLine();
            }
            return parser.parse(options, arg.split(" "), true);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
