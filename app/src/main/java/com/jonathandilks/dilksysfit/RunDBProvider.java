package com.jonathandilks.dilksysfit;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class RunDBProvider extends ContentProvider {
    private DBHelper dbHelper = null;
    private static final UriMatcher uriMatcher;
    private static final String POINT_TABLE_NAME = "point_data";
    private static final String SUMMARIES_TABLE_NAME = "run_summaries";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "point_data/lastEntry", 1);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "point_data/#", 2);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "point_data", 4);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "run_summaries/#", 100);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "run_summaries", 101);
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new DBHelper(this.getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (uri.getLastPathSegment() == null) {
            return RunDBContract.CONTENT_TYPE_MULTIPLE;
        } else {
            return RunDBContract.CONTENT_TYPE_SINGLE;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case 1:
                return db.query(POINT_TABLE_NAME, RunDBContract.allColsPointData, null, null, null, null, RunDBContract.POINT_DATA_ID + " DESC", "1");
            case 2:
                selection = RunDBContract.POINT_DATA_RUNID + " = " + uri.getLastPathSegment();
            case 4:
                return db.query(POINT_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case 100:
            case 101:
                return db.query(SUMMARIES_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id;
        switch (uriMatcher.match(uri)) {
            case 2:
            case 4:
                id = db.insert(POINT_TABLE_NAME, null, values);
                break;
            case 100:
            case 101:
                id = db.insert(SUMMARIES_TABLE_NAME, null, values);
                break;
            default:
                return null;
        }
        db.close();
        Uri newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null); //This is running
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (uriMatcher.match(uri) != 100)  throw new UnsupportedOperationException("Illegal RunDBProvider operation"); //Can only delete indivudal run entries

        SQLiteDatabase db = dbHelper.getWritableDatabase();


        db.delete(POINT_TABLE_NAME, RunDBContract.POINT_DATA_RUNID+ "=?", new String[]{uri.getLastPathSegment()});
        int returnValue = db.delete(SUMMARIES_TABLE_NAME, RunDBContract.RUN_SUMMARIES_ID+ "=?", selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return returnValue;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (uriMatcher.match(uri) != 100)  throw new UnsupportedOperationException("Illegal RunDBProvider operation"); //Can only update summaries - makes no sense to do otherwise

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        selection = "_id = " + uri.getLastPathSegment();
        int returnValue = db.update(SUMMARIES_TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return returnValue;
    }
}
