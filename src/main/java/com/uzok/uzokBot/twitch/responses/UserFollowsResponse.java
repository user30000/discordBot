package com.uzok.uzokBot.twitch.responses;

import com.uzok.uzokBot.twitch.dtos.Cursor;
import com.uzok.uzokBot.twitch.dtos.UserFollow;

import java.util.ArrayList;

public class UserFollowsResponse {
    public int total;
    public ArrayList<UserFollow> data;
    public Cursor pagination;
}
