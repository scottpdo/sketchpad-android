package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import cc.scottland.sketchpad.CanvasView;
import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Line implements Shape {

    public Point p1;
    public Point p2;
    public CanvasView cv;

    private boolean active;

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;

        // add line to points
        p1.lines.add(this);
        p2.lines.add(this);
    }

    public void setCanvasView(CanvasView cv) {
        this.cv = cv;
    }

    public void update(Cursor c, boolean isFinal) {

        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");

        setActive(!isFinal);

        if (!isFinal || !c.isOn()) {
            p2.update(c, isFinal);
            return;
        }

        p2 = c.target();
        p2.lines.add(this);
    }

    public void move(int dx, int dy) {
        p1.move(dx, dy);
        p2.move(dx, dy);
    }

    public void remove() {
        p1.lines.remove(this);
        p2.lines.remove(this);
        p1 = null;
        p2 = null;
    }

    private Point parametrize(double t) {
        return new Point(
            (int)(p1.x + t * (p2.x - p1.x)),
            (int)(p1.y + t * (p2.y - p1.y))
        );
    }

    public Shape near(Point pt) {

        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");

        int minDistance = 30;

        // close to an endpoint
        if (Utils.distance(pt, p1) < minDistance) return p1;
        if (Utils.distance(pt, p2) < minDistance) return p2;

        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float l2 = dx * dx + dy * dy; // distance squared
        if (l2 == 0) return null;

        // project point onto line segment to get closest point
        float t;
        t = (pt.x - p1.x) * (p2.x - p1.x) + (pt.y - p1.y) * (p2.y - p1.y);
        t /= l2;

        Point closest = parametrize(t);

        float d = Utils.distance(pt, closest);

        if (d < minDistance) {
            Generic g = new Generic(closest.x, closest.y, this);
            g.setCanvasView(cv);
            return g;
        }

        return null;
    }

    public Line clone() {
        Point p1 = this.p1.clone();
        Point p2 = this.p2.clone();
        Line l = new Line(p1, p2);
        l.setCanvasView(cv);
        return l;
    }

    @Override
    public void draw(Canvas canvas, Paint p) {

        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");

        p.setColor(Color.WHITE);

        if (isActive()) {
            DashPathEffect dashPath = new DashPathEffect(new float[]{8, 8}, (float) 1.0);
            p.setPathEffect(dashPath);
        } else {
            p.setPathEffect(null);
        }

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        Path path = new Path();
        path.moveTo(p1.x + cv.x, p1.y + cv.y);
        path.lineTo(p2.x + cv.x, p2.y + cv.y);
        canvas.drawPath(path, p);
    }

    public boolean isTruePoint() { return false; }

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }

    public void rotate(float angle, Point ref) {
        p1.rotate(angle, ref);
        p2.rotate(angle, ref);
    }

    public void scale(float factor, Point ref) {
        p1.scale(factor, ref);
        p2.scale(factor, ref);
    }
}
