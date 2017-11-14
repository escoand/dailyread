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
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Locale;

@Entity(
        indices = {@Index(value = {"name"}, unique = true)}
)
public class Subscription {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private int revision;

    public Subscription(String name, int revision) {
        this.name = name;
        this.revision = revision;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Subscription))
            return false;

        Subscription tmp = (Subscription) obj;
        return (name == null && tmp.getName() == null || name.equals(tmp.getName()))
                && revision == tmp.getRevision();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "%s [#%d name='%s' revision=%d]",
                getClass().getSimpleName(), id, name, revision);
    }
}