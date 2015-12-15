package com.example.dawiduk.podejscie2;


import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Created by dawiduk on 14-12-15.
 */
public class ShowSnackBar implements OnClickListener {
    @Override
    public void onClick(View v) {
        Snackbar.make(v, "you clicked me", Snackbar.LENGTH_LONG).show();
    }
}
