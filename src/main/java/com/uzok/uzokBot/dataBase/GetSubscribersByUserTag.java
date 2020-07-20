package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class GetSubscribersByUserTag extends BaseSqlProcedure {
    public GetSubscribersByUserTag(String userName) {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("SELECT subscriberTag, guildSnowflake, channelSnowflake, isEveryone FROM DiscordBot.subscriptions WHERE streamerTag = '%s';", userName);
        this.sqlQuery = sb.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        List<subscriber> result = new ArrayList<>();
        try {
            while (resultSet.next()) {
                result.add(
                        new subscriber(resultSet.getString(1),
                                resultSet.getLong(2),
                                resultSet.getLong(3),
                                resultSet.getBoolean(4)
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class subscriber {
        subscriber(String s, long g, long c, boolean e) {
            this.subTag = s;
            this.guidSnowflake = g;
            this.channelSnowflake = c;
            this.isEveryone = e;
        }

        public String subTag;
        public long guidSnowflake;
        public long channelSnowflake;
        public boolean isEveryone;
    }
}
