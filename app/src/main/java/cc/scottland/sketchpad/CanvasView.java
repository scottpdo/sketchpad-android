package cc.scottland.sketchpad;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Canvas;
import android.content.Context;
import android.util.AttributeSet;

import java.util.List;
import java.util.ArrayList;

import cc.scottland.sketchpad.shapes.Circle;
import cc.scottland.sketchpad.shapes.Cursor;
import cc.scottland.sketchpad.shapes.Generic;
import cc.scottland.sketchpad.shapes.Line;
import cc.scottland.sketchpad.shapes.Point;
import cc.scottland.sketchpad.shapes.Shape;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class CanvasView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Shape> objects = new ArrayList<Shape>();

    private boolean isTouchDown = false;
    private Cursor cursor = new Cursor();
    private String action = "";

    int bg = Color.BLACK;

    private Shape activeObj;

    public CanvasView(Context context) {
        super(context);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        paint.setColor(bg);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        if (isTouchDown) drawCursor(canvas);
        for (Shape object : objects) object.draw(canvas);
    }

    private void init() {

        invalidate();
        requestLayout();
    }

    /**
     * If the given action is the current state action, turn it off.
     * Otherwise, set it to be the new state action.
     */
    public void toggleAction(String action) {

        if (is(action)) {
            this.action = "";
            return;
        }

        this.action = action;
    }

    private boolean is(String action) {
        return this.action == action;
    }

    public void onTouchStart(MotionEvent e) {
        isTouchDown = true;
        update(e, false);
    }

    public void onTouchEnd(MotionEvent e) {
        isTouchDown = false;
        cancel(e);
    }

    public void cancel(MotionEvent e) {
        update(e, true);
        toggleAction("");
        activeObj = null;
    }

    public void drawCursor(Canvas canvas) {

        int x = cursor.x;
        int y = cursor.y;

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);

        canvas.drawLine(x - 48, y, x - 12, y, paint);
        canvas.drawLine(x + 48, y, x + 12, y, paint);
        canvas.drawLine(x, y - 12, x, y - 48, paint);
        canvas.drawLine(x, y + 12, x, y + 48, paint);

        Point center = cursor.target();

        canvas.drawCircle(center.x, center.y, 3, paint);

    }

    public void update(MotionEvent e, boolean isFinal) {

        int x = (int)e.getX();
        int y = (int)e.getY();

        // update cursor
        cursor.x = x;
        cursor.y = y;
        cursor.off();

        // determine if cursor is `near` any object
        for (Shape object : objects) {

            if (is("moving")) break; // but not if we're moving an object
            if (object == activeObj) break;

            Shape near = object.near(cursor.target());
            if (near == null) continue;

            cursor.on(near);
        }

        // update active object, if it exists
        if (activeObj != null) {
            activeObj.update(cursor, isFinal);
        }

        invalidate();
        requestLayout();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                return createCircle();
            case KeyEvent.KEYCODE_2:
                return createLine();
            case KeyEvent.KEYCODE_3:
                return moveObject();
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public boolean createCircle() {

        if (!isTouchDown) return false;
        if (this.is("drawing")) return false;

        this.toggleAction("drawing");

        Point pt = cursor.target();
        Circle c = new Circle(pt.x, pt.y, 0);
        activeObj = c;
        objects.add(c);

        return true;
    }

    public boolean createLine() {

        if (!isTouchDown) return false;
        if (is("drawing")) return false;

        this.toggleAction("drawing");

        Point pt = cursor.target();
        Line line = new Line(
            pt,
            new Point(pt.x, pt.y)
        );

        activeObj = line;
        objects.add(line);

        return true;
    }

    public boolean moveObject() {

        if (!isTouchDown) return false;
        if (!is("moving") || !cursor.isOn()) return false;

        for (Shape object : objects) {
            Shape near = object.near(cursor);
            if (near == null) continue;
            activeObj = near;
        }

        if (activeObj != null) activeObj.update(cursor, false);

        return true;
    }
}