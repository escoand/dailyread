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

package de.escoand.readdaily;

import android.app.Activity;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.escoand.readdaily.database.entity.TextInfo;
import de.escoand.readdaily.databinding.ItemContentBinding;
import de.escoand.readdaily.enums.TextType;
import de.escoand.readdaily.views.ViewHandlers;

public class TextInfoArrayAdapter extends ArrayAdapter<TextInfo> {
    private final Map<Integer, String> types = new HashMap() {{
        for (TextType type : TextType.values()) {
            if (type.getText() != 0)
                put(type.getPriority(), getContext().getString(type.getText()));
        }
    }};

    public TextInfoArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TextInfo> objects) {
        super(context, resource, objects);
    }

    @BindingAdapter({"android:imageResource"})
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        final ItemContentBinding binding = ItemContentBinding.inflate(inflater);
        final TextInfo data = getItem(position);
        final String imgName = String.format("img_month_%02d", data.text.getDate().get(Calendar.MONTH) + 1);

        binding.setData(data);
        binding.setTypes(types);
        binding.setImage(getContext().getResources().getIdentifier(imgName, "mipmap", getContext().getPackageName()));
        binding.setHandlers(new ViewHandlers());

        return binding.getRoot();
    }
}
