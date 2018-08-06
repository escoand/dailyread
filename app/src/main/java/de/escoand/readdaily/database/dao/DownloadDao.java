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

package de.escoand.readdaily.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.escoand.readdaily.database.entity.Download;

@Dao
public interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Download download);

    @Update
    int update(Download download);

    @Delete
    int delete(Download download);

    @Query("SELECT * FROM download")
    List<Download> getAll();

    @Query("SELECT * FROM download WHERE subscription = :subscription")
    Download findBySubscription(String subscription);

    @Query("SELECT * FROM download WHERE downloadId = :downloadId")
    Download findByDownloadId(long downloadId);

}
