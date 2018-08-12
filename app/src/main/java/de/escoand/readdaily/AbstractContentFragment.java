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

package de.escoand.readdaily;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public abstract class AbstractContentFragment extends DialogFragment implements Observer {
    protected int layout = R.layout.item_content;
    protected String condition = null;
    protected String[] values = null;
    protected Date dateOnCreate = null;
    protected int dateOffset = 0;

    private ListItemBinding binding = null;

    public void setDateOnCreate(final Date date) {
        dateOnCreate = date;
    }

    private void initDateOnCreate() {
        if (dateOnCreate != null) {
            DatePersistence.getInstance().deleteObserver(this);
            if (binding != null)
                binding.setText(TextDatabase.getInstance(getContext()).getTextDao().findByType(values[0]));
        } else
            DatePersistence.getInstance().addObserver(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final ListView list = new ListView(getContext());
        list.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final ListItemBinding binding = DataBindingUtil.inflate(inflater, layout, list, false);

        return list;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initDateOnCreate();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Dialog dialog = new AlertDialog.Builder(getContext())
                .setView(onCreateView(getActivity().getLayoutInflater(), null, savedInstanceState))
                .setPositiveButton(getString(R.string.button_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dismiss();
                    }
                })
                .create();

        initDateOnCreate();

        return dialog;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (binding != null)
            binding.setText(TextDatabase.getInstance(getContext()).getTextDao().findByDate());
    }

    @Override
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
        View source = ((ViewGroup) view.getParent()).findViewById(R.id.daily_source);
        View wrapper = ((ViewGroup) view.getParent()).findViewById(R.id.wrapper_source);
        switch (cursor.getColumnName(columnIndex)) {

            // title
            case Database.COLUMN_TITLE:
                switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {
                    case Database.TYPE_YEAR:
                        ((TextView) view).setText(getContext().getString(R.string.type_voty));
                        return true;
                    case Database.TYPE_MONTH:
                        ((TextView) view).setText(getContext().getString(R.string.type_votm));
                        return true;
                    case Database.TYPE_WEEK:
                        ((TextView) view).setText(getContext().getString(R.string.type_votw));
                        return true;
                    case Database.TYPE_DAY:
                        ((TextView) view).setText(getContext().getString(R.string.type_votd));
                        source.setVisibility(View.GONE);
                        return true;
                    default: // do nothing
                        break;
                }
                break;

            // text
            case Database.COLUMN_TEXT:
                if (view instanceof TextView) {
                    ((TextView) view).setText(Html.fromHtml(cursor.getString(columnIndex)));
                    return true;
                }
                break;

            // do nothing
            default:
                break;
        }

        return false;
    }
}
