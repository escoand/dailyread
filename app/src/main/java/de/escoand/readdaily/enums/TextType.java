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

package de.escoand.readdaily.enums;

import de.escoand.readdaily.R;

public enum TextType {
    YEAR(50, R.string.type_voty),
    MONTH(40, R.string.type_votm),
    WEEK(30, R.string.type_votw),
    DAY(20, R.string.type_votd),
    EXEGESIS(10, 0),
    INTRO(25, 0),
    MEDIA(70, 0);

    private final int priority;
    private final int text;

    TextType(int priority, int text) {
        this.priority = priority;
        this.text = text;
    }

    public int getPriority() {
        return priority;
    }

    public int getText() {
        return text;
    }
}
