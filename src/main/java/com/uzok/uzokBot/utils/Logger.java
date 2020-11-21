package com.uzok.uzokBot.utils;

import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.LogProcedure;

public class Logger {
    private static Logger instance;

    public static void write(String msg) {
        if(instance == null){
            instance = new Logger();
        }
        if(msg.length() > Prop.getInt("logLength")) {
            JavaToMySQL.getInstance().executeCall(new LogProcedure(msg.substring(0, Prop.getInt("logLength"))));
            write(msg.substring(Prop.getInt("logLength")));
            return;
        }
        JavaToMySQL.getInstance().executeCall(new LogProcedure(msg));
    }
}
