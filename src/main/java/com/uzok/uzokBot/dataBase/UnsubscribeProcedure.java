package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.util.Formatter;

public class UnsubscribeProcedure extends BaseSqlProcedure {
    public UnsubscribeProcedure(String streamerTag, long guildSnowflake, long channelSnowflake){
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("CALL `DiscordBot`.`unsubscribe`('%s', %d, %d);", streamerTag, guildSnowflake, channelSnowflake);
        sqlQuery = sbuf.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        return null;
    }
}
