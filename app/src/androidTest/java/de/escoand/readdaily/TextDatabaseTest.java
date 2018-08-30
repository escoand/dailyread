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

package de.escoand.readdaily;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import de.escoand.readdaily.database.TextDatabase;
import de.escoand.readdaily.database.dao.DownloadDao;
import de.escoand.readdaily.database.dao.SubscriptionDao;
import de.escoand.readdaily.database.dao.TextDao;
import de.escoand.readdaily.database.dao.TextTypeDao;
import de.escoand.readdaily.database.entity.Download;
import de.escoand.readdaily.database.entity.Subscription;
import de.escoand.readdaily.database.entity.Text;
import de.escoand.readdaily.database.entity.TextInfo;
import de.escoand.readdaily.database.entity.TextType;
import de.escoand.readdaily.database.util.Converters;
import de.escoand.readdaily.database.util.Importer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class TextDatabaseTest {
    private TextDatabase db;
    private DownloadDao downloadDao;
    private SubscriptionDao subscriptionDao;
    private TextTypeDao textTypeDao;
    private TextDao textDao;
    private Importer importer;

    @Before
    public void createDb() {
        final Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, TextDatabase.class).build();
        downloadDao = db.getDownloadDao();
        subscriptionDao = db.getSubscriptionDao();
        textTypeDao = db.getTextTypeDao();
        textDao = db.getTextDao();
        importer = db.getImporter();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testConverters() {

        // date to int
        assertEquals(
                20171201,
                Converters.calendarToInt(new GregorianCalendar(2017, 12 - 1, 1))
        );
        assertEquals(
                20170101,
                Converters.calendarToInt(new GregorianCalendar(2017, 1 - 1, 1))
        );

        // int to date
        assertEquals(
                new GregorianCalendar(2017, 12 - 1, 1),
                Converters.intToCalendar(20171201)
        );
        assertEquals(
                new GregorianCalendar(2017, 1 - 1, 1),
                Converters.intToCalendar(20170101)
        );
    }

    @Test
    public void testDownload() {
        Download obj1, obj2;
        obj1 = new Download("test", 1, null);

        // insert
        downloadDao.insert(obj1);
        obj2 = downloadDao.findBySubscription(obj1.getSubscription());
        assertEquals(obj1, obj2);

        // update 1
        obj2.setSubscription("test2");
        downloadDao.update(obj2);
        obj1 = downloadDao.findBySubscription(obj2.getSubscription());
        assertEquals(obj1, obj2);

        // update 2
        obj1.setDownloadId(2);
        downloadDao.update(obj1);
        obj2 = downloadDao.findByDownloadId(obj1.getDownloadId());
        downloadDao.update(obj2);
        assertEquals(obj1, obj2);

        // delete
        downloadDao.delete(obj2);
        obj1 = downloadDao.findBySubscription(obj2.getSubscription());
        assertNull(obj1);
    }

    @Test
    public void testSubscription() {
        Subscription obj1, obj2;
        obj1 = new Subscription("test", 1);

        // insert
        subscriptionDao.insert(obj1);
        obj2 = subscriptionDao.findByName(obj1.getName());
        assertEquals(obj1, obj2);

        // delete
        subscriptionDao.delete(obj2);
        obj1 = subscriptionDao.findByName(obj2.getName());
        assertNull(obj1);
    }

    @Test
    public void testText() {
        Text obj1, obj2;
        long sub = subscriptionDao.insert(new Subscription("text", 99));
        long typ = textTypeDao.insert(new TextType(99, "text"));
        obj1 = new Text(sub, typ, new GregorianCalendar(), 99.0f, "tit99", "txt99", "src99");

        // insert
        obj2 = textDao.findById(textDao.insert(obj1)).text;
        assertEquals(obj1, obj2);
    }

    @Test
    public void testSubscriptionWithTexts() {
        final int YEAR = 2222;
        final int MONTH = 2;

        Subscription sub1, sub2;
        TextType typ1, typ2;

        // insert subscriptions
        subscriptionDao.insert(
                new Subscription("complex1", 100),
                new Subscription("complex2", 200)
        );
        sub1 = subscriptionDao.findByName("complex1");
        sub2 = subscriptionDao.findByName("complex2");

        // insert texttypes
        textTypeDao.insert(
                new TextType(1, "complex1"),
                new TextType(2, "complex2")
        );
        typ1 = textTypeDao.findByName("complex1");
        typ2 = textTypeDao.findByName("complex2");

        // insert texts
        textDao.insert(
                new Text(sub1, typ1, new GregorianCalendar(YEAR, MONTH, 2), 1.1f, "title1", "text1", "source1"),
                new Text(sub1, typ2, new GregorianCalendar(YEAR, MONTH, 2), 1.1f, "title2", "text2", "source2"),
                new Text(sub1, typ1, new GregorianCalendar(YEAR, MONTH, 3), 1.2f, "title3", "text3", "source3"),
                new Text(sub2, typ2, new GregorianCalendar(YEAR, MONTH, 4), 2.1f, "title4", "text4", "source4")
        );

        // check dates
        assertEquals(0, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 1)).size());
        assertEquals(2, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 2)).size());
        assertEquals(1, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 3)).size());
        assertEquals(1, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 4)).size());

        // check order
        List<TextInfo> txts = textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 2));
        assertEquals("title1", txts.get(1).text.getTitle());

        // delete
        subscriptionDao.delete(sub1);
        assertEquals(0, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 1)).size());
        assertEquals(0, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 2)).size());
        assertEquals(0, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 3)).size());
        assertEquals(1, textDao.findByDate(new GregorianCalendar(YEAR, MONTH, 4)).size());
    }

    // test csv file handling
    @Test
    public void testImportCsv() throws IOException {
        final Context context = InstrumentationRegistry.getContext();
        int id = context.getResources().getIdentifier("data_csv", "raw", context.getPackageName());
        final String name = "text_csv";

        for (de.escoand.readdaily.enums.TextType type : de.escoand.readdaily.enums.TextType.values())
            textTypeDao.insert(new TextType(type.getPriority(), type.name()));

        importer.importCSV(name, context.getResources().openRawResource(id));
        assertNotNull(subscriptionDao.findByName(name));
        assertEquals(4, textDao.findByDate(new GregorianCalendar(2000, 11 - 1, 1)).size());
        assertEquals(3, textDao.findByDate(new GregorianCalendar(2000, 11 - 1, 2)).size());
        assertEquals(4 + 3 + 3 + 2, textDao.getAllDays().size());
        assertEquals(4, textDao.getCalendar().size());
        assertEquals(2, textDao.findByPattern("%Month%").size());
        assertEquals(2, textDao.findByPattern("%Week%").size());
    }

    // test xml file handling
    @Test
    public void testImportXml() throws IOException, XmlPullParserException {
        final Context context = InstrumentationRegistry.getContext();
        int id = context.getResources().getIdentifier("data_xml", "raw", context.getPackageName());
        final String name = "text_xml";

        for (de.escoand.readdaily.enums.TextType type : de.escoand.readdaily.enums.TextType.values())
            textTypeDao.insert(new TextType(type.getPriority(), type.name()));

        importer.importXML(name, context.getResources().openRawResource(id));
        assertNotNull(subscriptionDao.findByName(name));
        assertEquals(5, textDao.findByDate(new GregorianCalendar(2000, 1 - 1, 1)).size());
        assertEquals(5, textDao.findByDate(new GregorianCalendar(2000, 1 - 1, 2)).size());
        assertEquals(5 + 5, textDao.getAllDays().size());
        assertEquals(2, textDao.getCalendar().size());
        assertEquals(2, textDao.findByPattern("%text%").size());
    }
}
