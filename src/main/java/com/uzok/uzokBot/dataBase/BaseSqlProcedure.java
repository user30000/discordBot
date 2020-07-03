package com.uzok.uzokBot.dataBase;

import java.sql.ResultSet;

public abstract class BaseSqlProcedure {
    public String sqlQuery;
    public abstract Object execute(ResultSet resultSet);
}