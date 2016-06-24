package com.alex.test.movies.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackbarMagic {
    public static void showSnackbar(View anchor, int messageId) {
        if (anchor != null) {
            Snackbar snackbar = Snackbar.make(anchor, messageId, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}
