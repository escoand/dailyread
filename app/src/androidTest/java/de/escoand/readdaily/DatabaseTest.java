/*
 * Copyright (c) 2017 escoand.
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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private final static String TEST_DOWNLAOD = "-test-download-";
    private final static String TEST_CSV = "-test-csv-";
    private final static String TEST_XML = "-test-xml-";

    private final Context context;
    private final Database db;

    public DatabaseTest() {
        super();
        context = InstrumentationRegistry.getTargetContext();
        db = Database.getInstance(context);
    }

    @Before
    public void initTest() {
        db.removeData(TEST_CSV);
        db.removeData(TEST_XML);
    }

    @After
    public void cleanTest() {
        db.removeData(TEST_CSV);
        db.removeData(TEST_XML);
    }

    // test upgrade procedure for every possible upgrade
    @Test
    public void test_01_Upgrade() {
        for (int i = 0; i < Database.DATABASE_VERSION; i++)
            for (int j = 1; j <= Database.DATABASE_VERSION; j++)
                db.onUpgrade(db.getWritableDatabase(), i, j);
    }

    // test download handling
    @Test
    public void test_02_Downloads() {
        Assert.assertEquals(0, db.getDownloads().getCount());

        db.addDownload(TEST_DOWNLAOD, 123456789);

        Assert.assertEquals(1, db.getDownloads().getCount());

        db.removeDownload(123456789);

        Assert.assertEquals(0, db.getDownloads().getCount());
    }

    // test csv file handling
    @Test
    public void test_11_CSV() throws Exception {
        String data = "01.01\t20001101\tAuthor 1\tScripture 1\tScr 1\tTitle 1\tExegesis 1\tVerse Week 1\tScripture Week 1\tVerse Month 1\tScripture Month 1\n" +
                "02.02\t20001102\tAuthor 2\tScripture 2\tScr 2\tTitle 2\tExegesis 2\tVerse Week 1\tScripture Week 1\t\t\n" +
                "03.03\t20001103\tAuthor 3\tScripture 3\tScr 3\tTitle 3\tExegesis 3\t\t\tVerse Month 3\tScripture Month 3\n" +
                "04.04\t20001104\tAuthor 4\tScripture 4\tScr 4\tTitle 4\tExegesis 4\t\t\t\t";

        db.importCSV(TEST_CSV, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue(db.isInstalled(TEST_CSV));

        Assert.assertEquals(4, db.getDay(new GregorianCalendar(2000, 11 - 1, 1).getTime()).getCount());
        Assert.assertEquals(3, db.getDay(new GregorianCalendar(2000, 11 - 1, 2).getTime()).getCount());
        Assert.assertEquals(2, db.getDay(new GregorianCalendar(2000, 11 - 1, 3).getTime()).getCount());
        Assert.assertEquals(2, db.getDay(new GregorianCalendar(2000, 11 - 1, 4).getTime()).getCount());
        Assert.assertEquals(4, db.getCalendar().getCount());
        Assert.assertEquals(4 + 3 + 2 + 2, db.getList().getCount());
        Assert.assertEquals(2, db.getSearch("Exegesis").getCount());

        db.removeData(TEST_CSV);
        Assert.assertFalse(db.isInstalled(TEST_CSV));
    }

    // test xml file handling
    @Test
    public void test_12_XML() throws Exception {
        String data = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<collection creationDate=\"01.01.1970 01:02:03\">" +
                "<entry sourceId=\"123\" source=\"Name of the Book 1\" title=\"Title of the Book 1\" subtitle=\"Subtitle of the Book 1\">Description of the Book 1</entry>" +
                "<entry sourceId=\"567\" source=\"Name of the Book 2\" title=\"Title of the Book 2\" subtitle=\"Subtitle of the Book 2\">Description of the Book 2</entry>" +
                "<entry date=\"2000-01-01\" description=\"Name of the Day 1\">" +
                "<exegesis sourceId=\"123\" source=\"Name of the Book\" sourceChapter=\"234\" sourceVerse=\"345\" title=\"Title of the Day\" subtitle=\"Subtitle of the Day\">Text of the Day</exegesis>" +
                "<verse_of_the_day source=\"Scripture of the Day\">Verse of the Day</verse_of_the_day>" +
                "<verse_of_the_week source=\"Scripture of the Week\">Verse of the Week</verse_of_the_week>" +
                "<verse_of_the_month source=\"Scripture of the Month\">Verse of the Month</verse_of_the_month>" +
                "<verse_of_the_year source=\"Scripture of the Year\">Verse of the Year</verse_of_the_year>" +
                "</entry>" +
                "<entry date=\"2000-01-02\" description=\"Name of the Day 2\">" +
                "<exegesis sourceId=\"123\" source=\"Name of the Book\" sourceChapter=\"234\" sourceVerse=\"345\" title=\"Title of the Day\" subtitle=\"Subtitle of the Day\">Text of the Day</exegesis>" +
                "<verse_of_the_day source=\"Scripture of the Day\">Verse of the Day</verse_of_the_day>" +
                "<verse_of_the_week source=\"Scripture of the Week\">Verse of the Week</verse_of_the_week>" +
                "<verse_of_the_month source=\"Scripture of the Month\">Verse of the Month</verse_of_the_month>" +
                "<verse_of_the_year source=\"Scripture of the Year\">Verse of the Year</verse_of_the_year>" +
                "</entry>" +
                "</collection>";

        db.importXML(TEST_XML, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue(db.isInstalled(TEST_XML));

        Assert.assertEquals(5, db.getDay(new GregorianCalendar(2000, 1 - 1, 1).getTime()).getCount());
        Assert.assertEquals(5, db.getDay(new GregorianCalendar(2000, 1 - 1, 2).getTime()).getCount());
        Assert.assertEquals(2, db.getCalendar().getCount());
        Assert.assertEquals(5 + 5, db.getList().getCount());
        Assert.assertEquals(2, db.getSearch("Text").getCount());

        db.removeData(TEST_XML);
        Assert.assertFalse(db.isInstalled(TEST_XML));
    }
}
