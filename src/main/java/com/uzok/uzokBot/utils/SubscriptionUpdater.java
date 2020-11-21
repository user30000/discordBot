package com.uzok.uzokBot.utils;

import com.uzok.uzokBot.dataBase.GetOverdueSubscriptionProcedure;
import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.responses.UsersResponse;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public class SubscriptionUpdater extends TimerTask {
    @Override
    public void run() {
        try {
            List<String> streamers = (List<String>) JavaToMySQL.getInstance().
                    executeQuery(new GetOverdueSubscriptionProcedure(Prop.getInt("sub_time")));
            for (String streamer :
                    streamers) {
                UsersResponse x = Client.getInstance().getUserInfo(streamer);
                Client.getInstance().postUnsubOnStreamChange(x.data.get(0).id);
                Client.getInstance().postSubOnStreamChange(x.data.get(0).id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
