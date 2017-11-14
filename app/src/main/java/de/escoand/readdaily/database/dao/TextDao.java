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

package de.escoand.readdaily.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;
import android.arch.persistence.room.Transaction;

import java.util.Calendar;
import java.util.List;

import de.escoand.readdaily.database.entity.Text;
import de.escoand.readdaily.database.entity.TextInfo;

@Dao
public interface TextDao {

    @Insert
    long insert(Text text);

    @Insert
    List<Long> insert(Text... texts);

    @Query("UPDATE text SET read = 1 WHERE id = :id")
    int markAsRead(long id);

    @Query("UPDATE text SET read = 1 WHERE date = :date")
    int markAsRead(Calendar date);

    @Transaction
    @Query("SELECT * FROM text WHERE id = :id")
    TextInfo findById(long id);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM text JOIN texttype ON text.type=texttype.id WHERE date = :calendar ORDER BY date, texttype.priority DESC")
    List<TextInfo> findByDate(Calendar calendar);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM text JOIN texttype ON text.type=texttype.id WHERE text MATCH :pattern ORDER BY date, texttype.priority DESC")
    List<TextInfo> findByPattern(String pattern);

}