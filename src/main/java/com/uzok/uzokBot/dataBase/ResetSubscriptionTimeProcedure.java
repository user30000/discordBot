package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.util.Formatter;

public class ResetSubscriptionTimeProcedure extends BaseSqlProcedure {
    public ResetSubscriptionTimeProcedure(String login) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("CALL `DiscordBot`.`set_subscription_time`('%s');", login);
        sqlQuery = sbuf.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        return null;
    }
}
