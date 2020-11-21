package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class GetOverdueSubscriptionProcedure extends BaseSqlProcedure {
    public GetOverdueSubscriptionProcedure(int seconds) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("CALL `DiscordBot`.`get_overdue_subscription`('%d');", seconds);
        sqlQuery = sbuf.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        List<String> result = new ArrayList<>();
        try {
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
