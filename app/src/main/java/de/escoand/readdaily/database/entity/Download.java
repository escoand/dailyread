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
        indices = {@Index(value = {"subscription"}, unique = true)}
)
public class Download {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String subscription;
    private int downloadId;

    public Download() {
    }

    public Download(String subscription, int downloadHandlerId) {
        this.subscription = subscription;
        this.downloadId = downloadHandlerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Download))
            return false;

        Download tmp = (Download) obj;
        return (subscription == null && tmp.getSubscription() == null || subscription.equals(tmp.getSubscription()))
                && downloadId == tmp.getDownloadId();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "%s [#%d subscription='%s' downloadId=%d]",
                getClass().getSimpleName(), id, subscription, downloadId);
    }
}