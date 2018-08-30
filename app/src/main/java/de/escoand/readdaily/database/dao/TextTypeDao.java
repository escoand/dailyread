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
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.escoand.readdaily.database.entity.TextType;

@Dao
public interface TextTypeDao {

    @Insert
    long insert(TextType type);

    @Insert
    List<Long> insert(TextType... types);

    @Query("SELECT * FROM texttype")
    List<TextType> getAll();

    @Query("SELECT * FROM texttype WHERE priority = :priority")
    TextType findByPriority(long priority);

    @Query("SELECT * FROM texttype WHERE name = :name")
    TextType findByName(String name);

}
