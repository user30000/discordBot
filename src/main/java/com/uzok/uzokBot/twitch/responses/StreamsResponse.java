package com.uzok.uzokBot.twitch.responses;

import com.uzok.uzokBot.twitch.dtos.Cursor;
import com.uzok.uzokBot.twitch.dtos.Stream;

import java.util.ArrayList;

public class StreamsResponse {
    public ArrayList<Stream> data;
    public Cursor pagination;
}
