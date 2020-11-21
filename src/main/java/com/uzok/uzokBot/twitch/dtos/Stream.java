package com.uzok.uzokBot.twitch.dtos;

import java.util.ArrayList;

public class Stream {
    public long id;
    public String user_id;
    public String user_name;
    public String game_id;
    public String game_name;
    public String type;
    public String title;
    public String started_at;
    public String language;
    public String thumbnail_url;
    public ArrayList<String> tag_ids;
    public int viewer_count;
}
