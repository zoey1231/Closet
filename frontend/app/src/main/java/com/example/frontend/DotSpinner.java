package com.example.frontend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.Spinner;

@SuppressLint("AppCompatCustomView")
public class DotSpinner extends Spinner {
    OnItemSelectedListener listener;

    public DotSpinner(Context context) {
        super(context);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        if (listener != null) {
            listener.onItemSelected((AdapterView<?>)this, null, 0, 0);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }
}
