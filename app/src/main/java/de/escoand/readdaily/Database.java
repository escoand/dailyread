/*
 * Copyright (c) 2016 escoand.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.escoand.readdaily;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class Database extends SQLiteOpenHelper {
    public static final String COLUMN_SUBSCRIPTION = "subscription";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_GROUP = "groups";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_SOURCE = "author";
    public static final String COLUMN_READ = "read";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_REVISION = "revision";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_HASONLINE = "hasonline";
    public static final String TYPE_YEAR = "voty";
    public static final String TYPE_MONTH = "votm";
    public static final String TYPE_WEEK = "votw";
    public static final String TYPE_DAY = "votd";
    public static final String TYPE_EXEGESIS = "exeg";
    public static final String TYPE_INTRO = "intr";
    private static final String TABLE_TEXTS = "texts";
    private static final String TABLE_SETS = "sets";
    private static final String TABLE_TYPES = "types";
    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 1;
    private static final int PRIORITY_YEAR = 50;
    private static final int PRIORITY_MONTH = 40;
    private static final int PRIORITY_WEEK = 30;
    private static final int PRIORITY_DAY = 20;
    private static final int PRIORITY_EXEGESIS = 10;
    private static final int PRIORITY_INTRO = 25;

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static int getIntFromDate(Date date) {
        return Integer.valueOf(dateFormat.format(date));
    }

    @Nullable
    public static Date getDateFromInt(int date) {
        try {
            return dateFormat.parse(String.valueOf(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SETS + " (" +
                COLUMN_NAME + " TEXT PRIMARY KEY ON CONFLICT REPLACE, " +
                COLUMN_REVISION + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_TYPES + " (" +
                COLUMN_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_PRIORITY + " INTEGER NOT NULL)");
        db.execSQL("CREATE VIRTUAL TABLE " + TABLE_TEXTS + " USING fts3(" +
                COLUMN_SUBSCRIPTION + " TEXT NOT NULL REFERENCES " + TABLE_SETS + "(" + COLUMN_NAME + "), " +
                COLUMN_TYPE + " TEXT NOT NULL REFERENCES " + TABLE_TYPES + "(" + COLUMN_NAME + "), " +
                COLUMN_DATE + " INTEGER NOT NULL, " +
                COLUMN_GROUP + " REAL, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_TEXT + " TEXT, " +
                COLUMN_SOURCE + " TEXT, " +
                COLUMN_READ + " BOOLEAN, " +
                "UNIQUE (" + COLUMN_SUBSCRIPTION + "," + COLUMN_TYPE + "," + COLUMN_DATE + ") ON CONFLICT REPLACE)");

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, TYPE_YEAR);
        values.put(COLUMN_PRIORITY, PRIORITY_YEAR);
        db.insert(TABLE_TYPES, null, values);
        values.put(COLUMN_NAME, TYPE_MONTH);
        values.put(COLUMN_PRIORITY, PRIORITY_MONTH);
        db.insert(TABLE_TYPES, null, values);
        values.put(COLUMN_NAME, TYPE_WEEK);
        values.put(COLUMN_PRIORITY, PRIORITY_WEEK);
        db.insert(TABLE_TYPES, null, values);
        values.put(COLUMN_NAME, TYPE_DAY);
        values.put(COLUMN_PRIORITY, PRIORITY_DAY);
        db.insert(TABLE_TYPES, null, values);
        values.put(COLUMN_NAME, TYPE_EXEGESIS);
        values.put(COLUMN_PRIORITY, PRIORITY_EXEGESIS);
        db.insert(TABLE_TYPES, null, values);
        values.put(COLUMN_NAME, TYPE_INTRO);
        values.put(COLUMN_PRIORITY, PRIORITY_INTRO);
        db.insert(TABLE_TYPES, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_SETS);
        db.execSQL("DROP TABLE " + TABLE_TYPES);
        db.execSQL("DROP TABLE " + TABLE_TEXTS);
        onCreate(db);
    }

    public boolean loadDataCSV(String subscription, int revision, File file) throws IOException {
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            ContentValues values = new ContentValues();

            while ((line = reader.readLine()) != null) {
                String[] cols = line.split("\t");

                if (cols.length < 6)
                    continue;

                // subscription
                values.clear();
                values.put(COLUMN_NAME, subscription);
                values.put(COLUMN_REVISION, revision);
                db.insertOrThrow(TABLE_SETS, null, values);

                // verse of the day
                values.clear();
                values.put(COLUMN_SUBSCRIPTION, subscription);
                values.put(COLUMN_TYPE, TYPE_DAY);
                values.put(COLUMN_DATE, cols[1].trim());
                values.put(COLUMN_TEXT, cols[3].trim());
                db.insertOrThrow(TABLE_TEXTS, null, values);

                // description of the day
                values.clear();
                values.put(COLUMN_SUBSCRIPTION, subscription);
                values.put(COLUMN_TYPE, TYPE_EXEGESIS);
                values.put(COLUMN_DATE, cols[1].trim());
                values.put(COLUMN_TITLE, cols[5].trim());
                values.put(COLUMN_TEXT, cols[4].trim());
                values.put(COLUMN_SOURCE, cols[2].trim());
                db.insertOrThrow(TABLE_TEXTS, null, values);

                // verse of the week
                if (cols.length >= 8 && !cols[6].equals("")
                        && !cols[7].equals("")) {
                    values.clear();
                    values.put(COLUMN_SUBSCRIPTION, subscription);
                    values.put(COLUMN_TYPE, TYPE_WEEK);
                    values.put(COLUMN_DATE, cols[1].trim());
                    values.put(COLUMN_TEXT, cols[6].trim());
                    values.put(COLUMN_SOURCE, cols[7].trim());
                    db.insertOrThrow(TABLE_TEXTS, null, values);
                }

                // verse of the month
                if (cols.length >= 10 && !cols[8].equals("")
                        && !cols[9].equals("")) {
                    values.clear();
                    values.put(COLUMN_SUBSCRIPTION, subscription);
                    values.put(COLUMN_TYPE, TYPE_MONTH);
                    values.put(COLUMN_DATE, cols[1].trim());
                    values.put(COLUMN_TEXT, cols[8].trim());
                    values.put(COLUMN_SOURCE, cols[9].trim());
                    db.insertOrThrow(TABLE_TEXTS, null, values);
                }
            }

            reader.close();
            db.setTransactionSuccessful();
            result = true;
        } finally {
            db.endTransaction();
            db.close();
        }

        return result;
    }

    public boolean loadDataXML(String subscription, int revision, File file) throws IOException, XmlPullParserException {
        Random rand = new Random();
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            XmlPullParser parser = Xml.newPullParser();
            ContentValues values_exeg = new ContentValues();
            ContentValues values_day = new ContentValues();
            ContentValues values_week = new ContentValues();
            ContentValues values_month = new ContentValues();
            ContentValues values_year = new ContentValues();
            ContentValues values_intro = new ContentValues();
            String name = "";
            String date = "";

            // subscription
            values_day.put(COLUMN_NAME, subscription);
            values_day.put(COLUMN_REVISION, revision);
            db.insertOrThrow(TABLE_SETS, null, values_day);
            values_day.clear();

            parser.setInput(new FileInputStream(file), null);
            while (parser.next() != XmlPullParser.END_DOCUMENT) {

                // start tag
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    name = parser.getName();

                    switch (name) {

                        case "entry":
                            if (parser.getAttributeValue(null, "date") != null)
                                date = parser.getAttributeValue(null, "date").replaceAll("-", "");
                            else {
                                GregorianCalendar tmp = new GregorianCalendar();
                                tmp.set(rand.nextInt(7000) + 2100, rand.nextInt(12), rand.nextInt(31));
                                date = String.valueOf(getIntFromDate(tmp.getTime()));
                            }
                            // description - ignore
                            values_intro.put(COLUMN_SUBSCRIPTION, subscription);
                            values_intro.put(COLUMN_TYPE, TYPE_INTRO);
                            values_intro.put(COLUMN_DATE, date);
                            if (parser.getAttributeValue(null, "sourceId") != null)
                                values_intro.put(COLUMN_GROUP, Integer.valueOf(parser.getAttributeValue(null, "sourceId")));
                            // source - ignore
                            values_intro.put(COLUMN_TITLE, parser.getAttributeValue(null, "title"));
                            // subtitle - ignore
                            break;

                        case "exegesis":
                            values_exeg.put(COLUMN_SUBSCRIPTION, subscription);
                            values_exeg.put(COLUMN_TYPE, TYPE_EXEGESIS);
                            values_exeg.put(COLUMN_DATE, date);
                            // source - ignore
                            values_exeg.put(COLUMN_GROUP,
                                    Float.valueOf(parser.getAttributeValue(null, "sourceId")) +
                                            Float.valueOf(parser.getAttributeValue(null, "sourceChapter")) / 1000
                            );
                            // sourceVerse - ignore
                            values_exeg.put(COLUMN_TITLE, parser.getAttributeValue(null, "title"));
                            // subtitle - ignore
                            break;

                        case "verse_of_the_day":
                            values_day.put(COLUMN_SUBSCRIPTION, subscription);
                            values_day.put(COLUMN_TYPE, TYPE_DAY);
                            values_day.put(COLUMN_DATE, date);
                            values_day.put(COLUMN_SOURCE, parser.getAttributeValue(null, "source"));
                            break;

                        case "verse_of_the_week":
                            values_week.put(COLUMN_SUBSCRIPTION, subscription);
                            values_week.put(COLUMN_TYPE, TYPE_WEEK);
                            values_week.put(COLUMN_DATE, date);
                            values_week.put(COLUMN_SOURCE, parser.getAttributeValue(null, "source"));
                            break;

                        case "verse_of_the_month":
                            values_month.put(COLUMN_SUBSCRIPTION, subscription);
                            values_month.put(COLUMN_TYPE, TYPE_MONTH);
                            values_month.put(COLUMN_DATE, date);
                            values_month.put(COLUMN_SOURCE, parser.getAttributeValue(null, "source"));
                            break;

                        case "thoughts_on_bible_quote_year":
                            values_year.put(COLUMN_SUBSCRIPTION, subscription);
                            values_year.put(COLUMN_TYPE, TYPE_YEAR);
                            values_year.put(COLUMN_DATE, date);
                            values_year.put(COLUMN_SOURCE, parser.getAttributeValue(null, "source"));
                            // source - verse 
                            // sourceVerse - verse content
                            // author
                            values_year.put(COLUMN_TITLE, parser.getAttributeValue(null, "title"));
                            break;

                    }
                }

                // text element
                else if (parser.getEventType() == XmlPullParser.TEXT && !parser.getText().isEmpty()) {

                    switch (name) {

                        case "entry":
                            values_intro.put(COLUMN_TEXT, parser.getText());
                            break;

                        case "exegesis":
                            values_exeg.put(COLUMN_TEXT, parser.getText());
                            break;

                        case "verse_of_the_day":
                            values_day.put(COLUMN_TEXT, parser.getText());
                            break;

                        case "verse_of_the_week":
                            values_week.put(COLUMN_TEXT, parser.getText());
                            break;

                        case "verse_of_the_month":
                            values_month.put(COLUMN_TEXT, parser.getText());
                            break;

                        case "thoughts_on_bible_quote_year":
                            values_year.put(COLUMN_TEXT, parser.getText());
                            break;

                    }
                }

                // end tag
                else if (parser.getEventType() == XmlPullParser.END_TAG) {

                    switch (name) {

                        case "entry":
                            if (values_intro.containsKey(COLUMN_TEXT))
                                db.insertOrThrow(TABLE_TEXTS, null, values_intro);
                            values_intro.clear();
                            break;

                        case "exegesis":
                            if (values_exeg.containsKey(COLUMN_DATE) && values_exeg.containsKey(COLUMN_TEXT))
                                db.insertOrThrow(TABLE_TEXTS, null, values_exeg);
                            values_exeg.clear();
                            break;

                        case "verse_of_the_day":
                            if (values_day.containsKey(COLUMN_DATE) && values_day.containsKey(COLUMN_TEXT))
                                db.insertOrThrow(TABLE_TEXTS, null, values_day);
                            values_day.clear();
                            break;

                        case "verse_of_the_week":
                            if (values_week.containsKey(COLUMN_DATE) && values_week.containsKey(COLUMN_TEXT))
                                db.insertOrThrow(TABLE_TEXTS, null, values_week);
                            values_week.clear();
                            break;

                        case "verse_of_the_month":
                            if (values_month.containsKey(COLUMN_DATE) && values_month.containsKey(COLUMN_TEXT))
                                db.insertOrThrow(TABLE_TEXTS, null, values_month);
                            values_month.clear();
                            break;

                        case "thoughts_on_bible_quote_year":
                            if (values_year.containsKey(COLUMN_DATE) && values_year.containsKey(COLUMN_TEXT))
                                db.insertOrThrow(TABLE_TEXTS, null, values_year);
                            values_year.clear();
                            break;

                    }
                }
            }

            db.setTransactionSuccessful();
            result = true;
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public boolean isInstalled(String set) {
        boolean result = false;
        Cursor c = getReadableDatabase().query(
                TABLE_SETS,
                new String[]{"COUNT(*)"},
                COLUMN_NAME + "=?",
                new String[]{set},
                null, null, null
        );
        if (c != null) {
            c.moveToFirst();
            if (c.getInt(0) > 0)
                result = true;
            c.close();
        }
        return result;
    }

    public Cursor getDay(Date date, String condition, String[] values) {
        String w = "(" + COLUMN_DATE + "=? OR " + COLUMN_TYPE + "=? AND " + COLUMN_GROUP + " IN (SELECT CAST(" + COLUMN_GROUP + " AS INT) FROM " + TABLE_TEXTS + " WHERE " + COLUMN_DATE + "=?))";
        String[] v = new String[]{String.valueOf(getIntFromDate(date)), TYPE_INTRO, String.valueOf(getIntFromDate(date))};

        if (condition != null && !condition.isEmpty() && values != null && values.length > 0) {
            w += " AND (" + condition + ")";
            List<String> list = new ArrayList<>(Arrays.asList(v));
            list.addAll(Arrays.asList(values));
            v = list.toArray(new String[list.size()]);
        }

        Cursor c = getReadableDatabase().query(
                TABLE_TEXTS + " JOIN " + TABLE_TYPES + " ON " + TABLE_TEXTS + "." + COLUMN_TYPE + "=" + TABLE_TYPES + "." + COLUMN_NAME,
                new String[]{TABLE_TEXTS + ".rowid _id", COLUMN_TYPE, COLUMN_TITLE, COLUMN_TEXT, COLUMN_SOURCE, COLUMN_READ, COLUMN_DATE},
                w, v,
                null, null,
                TABLE_TYPES + "." + COLUMN_PRIORITY + " DESC");
        if (c != null)
            c.moveToFirst();
        return c;
    }

    public Cursor getDay(Date date) {
        return getDay(date, null, null);
    }

    public Cursor getCalendar() {
        Cursor c = getReadableDatabase().query(
                TABLE_TEXTS,
                new String[]{"rowid _id", COLUMN_SOURCE, COLUMN_DATE, COLUMN_READ},
                COLUMN_DATE + ">='20000000' AND " + COLUMN_DATE + "<'21000000'",
                null, null, null, null);
        if (c != null)
            c.moveToFirst();
        return c;
    }

    public Cursor getList(String condition, String[] values) {
        Cursor c = getReadableDatabase().query(
                TABLE_TEXTS,
                new String[]{"rowid _id", COLUMN_TITLE, COLUMN_TEXT, COLUMN_SOURCE, COLUMN_DATE, COLUMN_GROUP, COLUMN_READ},
                condition, values,
                null, null,
                COLUMN_GROUP);
        if (c != null)
            c.moveToFirst();
        return c;
    }

    public Cursor getList() {
        return getList(null, null);
    }

    public int markAsRead(Date date) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_READ, true);
        return getWritableDatabase().update(TABLE_TEXTS, values, COLUMN_DATE + "=?", new String[]{String.valueOf(getIntFromDate(date))});
    }

    public void removeData(String subscription) {
        SQLiteDatabase db = getWritableDatabase();
        int result = 0;

        result += db.delete(TABLE_TEXTS, COLUMN_SUBSCRIPTION + "=?", new String[]{subscription});
        result += db.delete(TABLE_SETS, COLUMN_NAME + "=?", new String[]{subscription});
        db.close();

        Log.w("removeData", subscription + " removed " + result);
    }
}
