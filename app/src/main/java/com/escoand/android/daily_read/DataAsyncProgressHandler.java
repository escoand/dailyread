/*
 * Copyright (C) 2016  escoand
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

package com.escoand.android.daily_read;

import android.view.View;
import android.widget.ProgressBar;

import com.loopj.android.http.DataAsyncHttpResponseHandler;

public abstract class DataAsyncProgressHandler extends DataAsyncHttpResponseHandler {
    private final ProgressBar progressBar;

    public DataAsyncProgressHandler(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void onStart() {
        super.onStart();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
        progressBar.setMax((int) totalSize);
        progressBar.setProgress((int) bytesWritten);
        super.onProgress(bytesWritten, totalSize);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        progressBar.setVisibility(View.INVISIBLE);
    }
}
