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

package de.escoand.readdaily.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;

import de.escoand.readdaily.database.util.Converters;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        foreignKeys = {
                @ForeignKey(entity = Subscription.class, parentColumns = "id", childColumns = "subscription", onDelete = CASCADE),
                @ForeignKey(entity = TextType.class, parentColumns = "priority", childColumns = "type")
        },
        indices = {
                @Index(value = {"subscription", "type", "date"}, unique = true),
                @Index("type"),
                @Index("date")
        }
)
public class Text {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private long subscription;
    @NonNull
    private long type;
    @NonNull
    private Calendar date;
    private float group;
    private String title;
    private String text;
    private String source;
    private boolean read;

    @Ignore
    public Text() {
    }

    public Text(Subscription subscription, TextType type, Calendar date, float group, String title, String text, String source) {
        this(subscription.getId(), type.getPriority(), date, group, title, text, source);
    }

    public Text(long subscription, long type, Calendar date, float group, String title, String text, String source) {
        this.subscription = subscription;
        this.type = type;
        this.date = date;
        this.group = group;
        this.title = title;
        this.text = text;
        this.source = source;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSubscription() {
        return subscription;
    }

    public void setSubscription(@NonNull Subscription subscription) {
        this.subscription = subscription.getId();
    }

    public void setSubscription(@NonNull long subscription) {
        this.subscription = subscription;
    }

    public long getType() {
        return type;
    }

    public void setType(@NonNull long type) {
        this.type = type;
    }

    public void setType(@NonNull TextType type) {
        this.type = type.getPriority();
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(@NonNull int date) {
        this.date = Converters.intToCalendar(date);
    }

    public void setDate(@NonNull Calendar date) {
        this.date = date;
    }

    public float getGroup() {
        return group;
    }

    public void setGroup(float group) {
        this.group = group;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Text))
            return false;

        Text tmp = (Text) obj;
        return subscription == tmp.getSubscription()
                && type == tmp.getType()
                && (date == null && tmp.getDate() == null || date != null && tmp.getDate() != null && Converters.calendarToInt(date) == Converters.calendarToInt(tmp.getDate()))
                && group == tmp.getGroup()
                && (title == null && tmp.getTitle() == null || title != null && title.equals(tmp.getTitle()))
                && (text == null && tmp.getText() == null || text != null && text.equals(tmp.getText()))
                && (source == null && tmp.getSource() == null || source != null && source.equals(tmp.getSource()))
                && read == tmp.isRead();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "%s [#%d subscription=%d type=%d date=%d group=%f read=%b title=%.50s text=%.50s source=%.50s]",
                getClass().getSimpleName(), id, subscription, type, Converters.calendarToInt(date), group, read, title, text, source);
    }
}