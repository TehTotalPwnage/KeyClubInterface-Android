/*
 * Copyright (c) 2017 Michael Nguyen
 *
 * This file is part of KeyClubInterface.
 *
 * KeyClubInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyClubInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyClubInterface.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.tehtotalpwnage.keyclubinterface.authenticator;

import android.accounts.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.tehtotalpwnage.keyclubinterface.R;

public class AccountAdapter extends BaseAdapter {
    private Account[] mAccounts;

    private Context mContext;


    public AccountAdapter(Context context, Account[] values) {
        this.mAccounts = values;

        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mAccounts.length;
    }

    @Override
    public Object getItem(int position) {
        return mAccounts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.meeting_spinner, parent, false);
            holder = new ViewHolder();
            holder.accountInfo = (TextView) convertView.findViewById(R.id.textView3);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.accountInfo.setText(mAccounts[position].name);

        return convertView;
    }

    private static class ViewHolder {
        TextView accountInfo;
    }
}
