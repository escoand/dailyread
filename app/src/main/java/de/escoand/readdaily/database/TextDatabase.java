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

package de.escoand.readdaily.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import de.escoand.readdaily.database.dao.DownloadDao;
import de.escoand.readdaily.database.dao.SubscriptionDao;
import de.escoand.readdaily.database.dao.TextDao;
import de.escoand.readdaily.database.dao.TextTypeDao;
import de.escoand.readdaily.database.entity.Download;
import de.escoand.readdaily.database.entity.Subscription;
import de.escoand.readdaily.database.entity.Text;
import de.escoand.readdaily.database.entity.TextType;
import de.escoand.readdaily.database.util.Converters;

@Database(
        entities = {Download.class, Subscription.class, TextType.class, Text.class},
        version = 2,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class TextDatabase extends RoomDatabase {
    abstract public DownloadDao getDownloadDao();

    abstract public SubscriptionDao getSubscriptionDao();

    abstract public TextTypeDao getTextTypeDao();

    abstract public TextDao getTextDao();
}