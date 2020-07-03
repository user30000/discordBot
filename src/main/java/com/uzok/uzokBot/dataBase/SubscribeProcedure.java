package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.util.Formatter;

public class SubscribeProcedure extends BaseSqlProcedure {
    public SubscribeProcedure(String streamerTag, long subSnowflake, String subTag){
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("CALL `DiscordBot`.`subscribe`('%s', %d, '%s');", streamerTag, subSnowflake, subTag);
        sqlQuery = sbuf.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        return null;
    }
}
