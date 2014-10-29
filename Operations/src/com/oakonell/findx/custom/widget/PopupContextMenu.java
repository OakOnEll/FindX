/*
 * Copyright (C) 2010 Tani Group 
 * http://android-demo.blogspot.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oakonell.findx.custom.widget;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * adapted from
 * http://www.tanisoft.net/2010/09/android-context-menu-with-icon.html
 * 
 */
public class PopupContextMenu implements DialogInterface.OnCancelListener,
        DialogInterface.OnDismissListener {

    private static final int LIST_PREFERED_HEIGHT = 65;

    private ButtonMenuAdapter menuAdapter;
    private Activity parentActivity;
    private int dialogId;

    private ButtonContextMenuOnClickListener clickHandler = null;

    /**
     * constructor
     * 
     * @param parent
     * @param id
     */
    public PopupContextMenu(Activity parent, int id) {
        parentActivity = parent;
        dialogId = id;

        menuAdapter = new ButtonMenuAdapter(parentActivity);
    }

    /**
     * Add menu item
     * 
     * @param menuItem
     */
    public void addItem(Resources res, CharSequence title, int actionTag) {
        menuAdapter.addItem(new ButtonContextMenuItem(res, title, actionTag));
    }

    public void addItem(Resources res, int textResourceId, int actionTag) {
        menuAdapter.addItem(new ButtonContextMenuItem(res, textResourceId, actionTag));
    }

    /**
     * Set menu onclick listener
     * 
     * @param listener
     */
    public void setOnClickListener(ButtonContextMenuOnClickListener listener) {
        clickHandler = listener;
    }

    /**
     * Create menu
     * 
     * @return
     */
    public Dialog createMenu(CharSequence menuItitle) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTitle(menuItitle);
        builder.setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialoginterface, int i) {
                ButtonContextMenuItem item = (ButtonContextMenuItem) menuAdapter.getItem(i);

                if (clickHandler != null) {
                    clickHandler.onClick(item.actionTag);
                }
            }
        });

        builder.setInverseBackgroundForced(true);

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cleanup();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    private void cleanup() {
        // this was causing an error - dialog with id 'x' was never shown from
        // activity
        // parentActivity.dismissDialog(dialogId);
    }

    /**
     * IconContextMenu On Click Listener interface
     */
    public interface ButtonContextMenuOnClickListener {
        public abstract void onClick(int menuId);
    }

    /**
     * Menu-like list adapter with icon
     */
    protected class ButtonMenuAdapter extends BaseAdapter {
        private Context context = null;

        private ArrayList<ButtonContextMenuItem> mItems = new ArrayList<ButtonContextMenuItem>();

        public ButtonMenuAdapter(Context context) {
            this.context = context;
        }

        /**
         * add item to adapter
         * 
         * @param menuItem
         */
        public void addItem(ButtonContextMenuItem menuItem) {
            mItems.add(menuItem);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            ButtonContextMenuItem item = (ButtonContextMenuItem) getItem(position);
            return item.actionTag;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ButtonContextMenuItem item = (ButtonContextMenuItem) getItem(position);

            Resources res = parentActivity.getResources();

            if (convertView == null) {
                TextView temp = new TextView(context);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.WRAP_CONTENT);
                temp.setLayoutParams(param);
                temp.setPadding((int) toPixel(res, 15), 0, (int) toPixel(res, 15), 0);
                temp.setGravity(android.view.Gravity.CENTER_VERTICAL);

                Theme th = context.getTheme();
                TypedValue tv = new TypedValue();

                if (th.resolveAttribute(android.R.attr.textAppearanceLargeInverse, tv, true)) {
                    temp.setTextAppearance(context, tv.resourceId);
                }

                temp.setMinHeight(LIST_PREFERED_HEIGHT);
                temp.setCompoundDrawablePadding((int) toPixel(res, 14));
                convertView = temp;
            }

            TextView textView = (TextView) convertView;
            textView.setTag(item);
            textView.setText(item.text);
            // textView.setCompoundDrawablesWithIntrinsicBounds(item.image,
            // null, null, null);

            return textView;
        }

        private float toPixel(Resources res, int dip) {
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
            return px;
        }
    }

    /**
     * menu-like list item with icon
     */
    private static class ButtonContextMenuItem {
        public final CharSequence text;
        public final int actionTag;

        /**
         * public constructor
         * 
         * @param res
         *            resource handler
         * @param textResourceId
         *            id of title in resource
         * @param actionTag
         *            indicate action of menu item
         */
        public ButtonContextMenuItem(Resources res, int textResourceId, int actionTag) {
            text = res.getString(textResourceId);
            this.actionTag = actionTag;
        }

        /**
         * public constructor
         * 
         * @param res
         *            resource handler
         * @param title
         *            menu item title
         * @param actionTag
         *            indicate action of menu item
         */
        public ButtonContextMenuItem(Resources res, CharSequence title, int actionTag) {
            text = title;
            this.actionTag = actionTag;
        }
    }
}
