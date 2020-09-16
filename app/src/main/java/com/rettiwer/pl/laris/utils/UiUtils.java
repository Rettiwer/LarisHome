package com.rettiwer.pl.laris.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UiUtils {

    public static void showMessageDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .show();
    }
}
