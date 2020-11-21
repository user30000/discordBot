package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class CheckStreamIntoDB extends BaseSqlProcedure {
    public CheckStreamIntoDB(String userName) {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("SELECT id FROM DiscordBot.subscriptions WHERE streamerTag = '%s' limit 1;", userName);
        this.sqlQuery = sb.toString();
    }

    @Override
    public Object execute(ResultSet resultSet) {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
