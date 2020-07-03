package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class GetSubscribersByUserTag extends BaseSqlProcedure {
    public GetSubscribersByUserTag(String userName) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("SELECT subscriberTag FROM DiscordBot.subscriptions WHERE streamerTag = '%s';", userName);
        this.sqlQuery = sbuf.toString();
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
