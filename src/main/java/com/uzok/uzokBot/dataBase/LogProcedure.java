package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.util.Formatter;

public class LogProcedure extends BaseSqlProcedure {
    public LogProcedure(String logMsg) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("CALL `DiscordBot`.`add_log`('%s');", logMsg);
        sqlQuery = sbuf.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        return null;
    }
}
