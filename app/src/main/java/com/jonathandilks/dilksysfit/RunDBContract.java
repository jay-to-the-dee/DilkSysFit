package com.jonathandilks.dilksysfit;

import android.net.Uri;

public class RunDBContract {
    public static final String AUTHORITY = "com.jonathandilks.dilksysfit.RunDBProvider";

    public static final Uri URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri ALL_URI = Uri.parse("content://" + AUTHORITY + "/*");

    public static final String _ID = "_id";
    public static final String RUN_TIMESTAMP = "timestamp";
    public static final String RUN_LATITUDE = "lat";
    public static final String RUN_LONGITUDE = "lng";
    public static final String RUN_ALTITUDE = "alt";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/RunDBProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/RunDBProvider.data.text";
}
