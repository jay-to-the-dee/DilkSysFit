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
    private static final String TABLE_NAME = "runs";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "lastEntry", 1);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "#", 2);
        uriMatcher.addURI(RunDBContract.AUTHORITY, "*", 3);
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new DBHelper(this.getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case 1:
                String projectionCols[] = new String[]
                        {
                                RunDBContract._POINTID,
                                RunDBContract.RUN_RUNID
                        };
                return db.query(TABLE_NAME, projectionCols, null, null, null, null, RunDBContract._POINTID + " DESC", "1");
            case 2:
                selection = RunDBContract.RUN_RUNID + " = " + uri.getLastPathSegment();
            case 3:
                return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
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
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        Uri newUri = ContentUris.withAppendedId(uri, id);

        getContext().getContentResolver().notifyChange(newUri, null); //This is running

        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        selection = "_POINTID = " + uri.getLastPathSegment();
        return db.update(TABLE_NAME, values, selection, selectionArgs);
    }
}
