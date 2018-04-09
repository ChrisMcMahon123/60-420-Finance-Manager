package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;

public class CustomOnTouchListener implements View.OnTouchListener{
    private int color;

    CustomOnTouchListener(int color) {
        this.color = color;
    }

    //do not override onclick as any button will already implement that
    //functionality, will result in double clicking if implemented
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                view.invalidate();
            break;
            case MotionEvent.ACTION_UP:
                view.getBackground().clearColorFilter();
                view.invalidate();
            break;
        }
        return false;
    }
}
