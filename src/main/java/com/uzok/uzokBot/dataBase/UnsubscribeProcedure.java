package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.util.Formatter;

public class UnsubscribeProcedure extends BaseSqlProcedure {
    public UnsubscribeProcedure(String streamerTag, String subTag){
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("CALL `DiscordBot`.`unsubscribe`('%s', '%s');", streamerTag, subTag);
        sqlQuery = sbuf.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        return null;
    }
}
