package com.jonathandilks.dilksysfit;

import android.net.Uri;

public class RunDBContract {
    public static final String AUTHORITY = "com.jonathandilks.dilksysfit.RunDBProvider";

    public static final Uri POINT_DATA_URI_LAST_ENTRY = Uri.parse("content://" + AUTHORITY + "/point_data/lastEntry");
    public static final Uri POINT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/point_data");
    public static final Uri RUN_SUMMARIES_URI = Uri.parse("content://" + AUTHORITY + "/run_summaries");
    public static final Uri ALL_URI = Uri.parse("content://" + AUTHORITY + "/");

    public static final String POINT_DATA_ID = "_id";
    public static final String POINT_DATA_RUNID = "run_id";
    public static final String POINT_DATA_TIMESTAMP = "timestamp";
    public static final String POINT_DATA_LATITUDE = "lat";
    public static final String POINT_DATA_LONGITUDE = "lng";
    public static final String POINT_DATA_ALTITUDE = "alt";
    public static final String POINT_DATA_SPEED = "speed";

    public static final String[] allColsPointData = {
            POINT_DATA_ID,
            POINT_DATA_RUNID,
            POINT_DATA_TIMESTAMP,
            POINT_DATA_LATITUDE,
            POINT_DATA_LONGITUDE,
            POINT_DATA_ALTITUDE,
            POINT_DATA_SPEED
    };

    public static final String RUN_SUMMARIES_ID = "_id";
    public static final String RUN_SUMMARIES_NAME = "name";
    public static final String RUN_SUMMARIES_FINISH_TIMESTAMP = "finish_timestamp";
    public static final String RUN_SUMMARIES_FINISH_LOCATION_NAME = "finish_location_name";
    public static final String RUN_SUMMARIES_TOTAL_TIME = "total_time";
    public static final String RUN_SUMMARIES_ID_TOTAL_DISTANCE = "total_distance";

    public static final String[] allColsRunSummary = {
            RUN_SUMMARIES_ID,
            RUN_SUMMARIES_NAME,
            RUN_SUMMARIES_FINISH_TIMESTAMP,
            RUN_SUMMARIES_FINISH_LOCATION_NAME,
            RUN_SUMMARIES_TOTAL_TIME,
            RUN_SUMMARIES_ID_TOTAL_DISTANCE
    };

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/RunDBProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/RunDBProvider.data.text";
}
