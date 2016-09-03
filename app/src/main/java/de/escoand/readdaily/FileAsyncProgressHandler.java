/*
 * Copyright (c) 2016 escoand.
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

package de.escoand.readdaily;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

public abstract class FileAsyncProgressHandler extends FileAsyncHttpResponseHandler {
    private final ProgressBar progressView;

    public FileAsyncProgressHandler(Context context, ProgressBar progressView) {
        super(context);
        this.progressView = progressView;
    }

    @Override
    public void onStart() {
        super.onStart();
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
        progressView.setMax((int) totalSize);
        progressView.setProgress((int) bytesWritten);
        super.onProgress(bytesWritten, totalSize);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        progressView.setVisibility(View.INVISIBLE);
    }
}
