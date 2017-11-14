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

package de.escoand.readdaily.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Locale;

@Entity
public class TextType {
    public static final int TYPE_YEAR = 50;
    public static final int TYPE_MONTH = 40;
    public static final int TYPE_WEEK = 30;
    public static final int TYPE_DAY = 20;
    public static final int TYPE_EXEGESIS = 10;
    public static final int TYPE_INTRO = 25;
    public static final int TYPE_MEDIA = 70;

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int priority;
    private String name;

    public TextType(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextType))
            return false;

        TextType tmp = (TextType) obj;
        return (name == null && tmp.getName() == null || name.equals(tmp.getName()))
                && priority == tmp.getPriority();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "%s [#%d priority=%d name='%s']",
                getClass().getSimpleName(), id, priority, name);
    }
}