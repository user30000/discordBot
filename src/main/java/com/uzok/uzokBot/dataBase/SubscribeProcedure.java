package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.util.Formatter;

public class SubscribeProcedure extends BaseSqlProcedure {
    public SubscribeProcedure(String streamerTag, long guildSnowflake, long channelSnowflake, boolean isEveryone) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("CALL `DiscordBot`.`subscribeChannel`('%s', %d, %d, '%d');", streamerTag, guildSnowflake, channelSnowflake, isEveryone ? 1 : 0);
        sqlQuery = sbuf.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        return null;
    }
}
