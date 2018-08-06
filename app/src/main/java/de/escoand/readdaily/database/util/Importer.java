/*
 * Copyright (c) 2018 escoand.
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

package de.escoand.readdaily.database.util;

import android.arch.persistence.room.Dao;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.escoand.readdaily.LogHandler;
import de.escoand.readdaily.database.TextDatabase;
import de.escoand.readdaily.database.dao.SubscriptionDao;
import de.escoand.readdaily.database.dao.TextDao;
import de.escoand.readdaily.database.entity.Subscription;
import de.escoand.readdaily.database.entity.Text;
import de.escoand.readdaily.database.entity.TextType;

@Dao
public abstract class Importer {
    private TextDatabase db;
    private SubscriptionDao subscriptionDao;
    private TextDao textDao;

    Importer(TextDatabase db) {
        this.db = db;
        subscriptionDao = db.getSubscriptionDao();
        textDao = db.getTextDao();
    }

    public void importXML(String subscription, InputStream stream) throws IOException, XmlPullParserException {
        Random rand = new Random();
        XmlPullParser parser = Xml.newPullParser();
        Text text_entry = new Text();
        Text text_item = new Text();
        String name = "";

        db.beginTransaction();

        // subscription
        long id = subscriptionDao.insert(new Subscription(subscription, 0));
        text_item.setSubscription(id);

        try {
            parser.setInput(stream, null);
            while (parser.next() != XmlPullParser.END_DOCUMENT) {

                // start tag
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    name = parser.getName();

                    switch (name) {

                        case "entry":
                            if (parser.getAttributeValue(null, "date") != null) {
                                Calendar tmp = Converters.intToCalendar(Integer.valueOf(parser.getAttributeValue(null, "date").replaceAll("-", "")));
                                text_entry.setDate(tmp);
                                text_item.setDate(tmp);
                            } else {
                                GregorianCalendar tmp = new GregorianCalendar();
                                tmp.set(rand.nextInt(7000) + 2100, rand.nextInt(12), rand.nextInt(31));
                                text_entry.setDate(tmp);
                                text_item.setDate(tmp);
                            }
                            text_entry.setType(TextType.TYPE_INTRO);
                            if (parser.getAttributeValue(null, "sourceId") != null)
                                text_entry.setGroup(Integer.valueOf(parser.getAttributeValue(null, "sourceId")));
                            text_entry.setTitle(parser.getAttributeValue(null, "title"));
                            text_entry.setText(null);
                            text_entry.setSource(null);
                            break;

                        case "exegesis":
                            text_item.setType(TextType.TYPE_EXEGESIS);
                            text_item.setGroup(
                                    Float.valueOf(parser.getAttributeValue(null, "sourceId")) +
                                            Float.valueOf(parser.getAttributeValue(null, "sourceChapter")) / 1000
                            );
                            text_item.setTitle(parser.getAttributeValue(null, "title"));
                            text_item.setText(null);
                            if (parser.getAttributeValue(null, "source") != null &&
                                    parser.getAttributeValue(null, "sourceChapter") != null &&
                                    parser.getAttributeValue(null, "sourceChapter") != null)
                                text_item.setSource(
                                        parser.getAttributeValue(null, "source") +
                                                (!parser.getAttributeValue(null, "sourceChapter").isEmpty() ? " " + parser.getAttributeValue(null, "sourceChapter") : "") +
                                                (!parser.getAttributeValue(null, "sourceVerse").isEmpty() ? "," + parser.getAttributeValue(null, "sourceVerse") : "")
                                );
                            break;

                        case "verse_of_the_day":
                            text_item.setType(TextType.TYPE_DAY);
                            text_item.setGroup(0);
                            text_item.setTitle(null);
                            text_item.setText(null);
                            text_item.setSource(parser.getAttributeValue(null, "source"));
                            break;

                        case "verse_of_the_week":
                            text_item.setType(TextType.TYPE_WEEK);
                            text_item.setGroup(0);
                            text_item.setTitle(null);
                            text_item.setText(null);
                            text_item.setSource(parser.getAttributeValue(null, "source"));
                            break;

                        case "verse_of_the_month":
                            text_item.setType(TextType.TYPE_MONTH);
                            text_item.setGroup(0);
                            text_item.setTitle(null);
                            text_item.setText(null);
                            text_item.setSource(parser.getAttributeValue(null, "source"));
                            break;

                        case "verse_of_the_year":
                            text_item.setType(TextType.TYPE_YEAR);
                            text_item.setGroup(0);
                            text_item.setTitle(null);
                            text_item.setText(null);
                            text_item.setSource(parser.getAttributeValue(null, "source"));
                            break;

                        case "thoughts_on_bible_quote_year":
                            text_item.setType(TextType.TYPE_YEAR);
                            text_item.setGroup(0);
                            text_item.setTitle(parser.getAttributeValue(null, "title"));
                            text_item.setText(null);
                            text_item.setSource(parser.getAttributeValue(null, "source"));
                            break;

                        default: // do nothing
                            break;
                    }
                }

                // text element
                else if (parser.getEventType() == XmlPullParser.TEXT && !parser.getText().isEmpty()) {

                    switch (name) {

                        case "entry":
                            text_entry.setText(parser.getText());
                            break;

                        case "exegesis":
                        case "verse_of_the_day":
                        case "verse_of_the_week":
                        case "verse_of_the_month":
                        case "verse_of_the_year":
                        case "thoughts_on_bible_quote_year":
                            text_item.setText(parser.getText());
                            break;

                        default: // do nothing
                            break;
                    }
                }

                // end tag
                else if (parser.getEventType() == XmlPullParser.END_TAG) {

                    switch (name) {

                        case "entry":
                            if (text_entry.getText() != null) {
                                LogHandler.d(text_entry.toString());
                                textDao.insert(text_entry);
                            }
                            break;

                        case "exegesis":
                        case "verse_of_the_day":
                        case "verse_of_the_week":
                        case "verse_of_the_month":
                        case "verse_of_the_year":
                        case "thoughts_on_bible_quote_year":
                            if (text_item.getDate() != null && text_item.getText() != null) {
                                LogHandler.d(text_item.toString());
                                textDao.insert(text_item);
                                text_item.setText(null);
                            }
                            break;

                        default: // do nothing
                            break;
                    }
                }
            }

            db.setTransactionSuccessful();
        }

        // commit or rollback
        finally {
            db.endTransaction();
        }
    }

    public void importZIP(String subscription, InputStream stream, File dir) throws IOException {
        Text text = new Text();
        ZipInputStream zip = new ZipInputStream(stream);
        ZipEntry entry;
        byte[] buffer = new byte[16 * 1024];
        int len;

        db.beginTransaction();

        // subscription
        long id = subscriptionDao.insert(new Subscription(subscription, 0));
        text.setSubscription(id);
        text.setType(TextType.TYPE_MEDIA);
        text.setGroup(0);
        text.setTitle(null);
        text.setText(null);

        File outdir = new File(dir, subscription);
        if (!outdir.exists())
            outdir.mkdir();

        // read entries
        try {
            while ((entry = zip.getNextEntry()) != null) {
                String filename = entry.getName();
                File outfile = new File(outdir, filename);
                FileOutputStream outstrm = new FileOutputStream(outfile);
                int date = Integer.valueOf(outfile.getName().substring(0, filename.lastIndexOf(".")));

                LogHandler.i("file " + outfile.getAbsolutePath());
                LogHandler.i("date " + date);

                // save file
                while ((len = zip.read(buffer)) != -1)
                    outstrm.write(buffer, 0, len);
                outstrm.close();
                zip.closeEntry();

                // save path
                text.setDate(Converters.intToCalendar(date));
                text.setSource(outfile.getAbsolutePath());
                textDao.insert(text);
            }

            zip.close();
            db.setTransactionSuccessful();
        }

        // rollback filesystem
        catch (IOException e) {
            File[] files = outdir.listFiles();
            for (int i = 0; i < files.length; i++)
                files[i].delete();
            outdir.delete();

            // throw to the caller
            throw e;
        }

        // commit or rollback
        finally {
            db.endTransaction();
        }
    }
}
