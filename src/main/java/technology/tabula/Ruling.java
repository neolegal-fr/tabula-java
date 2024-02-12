package technology.tabula;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class Ruling extends Line2D.Float {
    
    public final static int PERPENDICULAR_PIXEL_EXPAND_AMOUNT = 2;
    public final static int COLINEAR_OR_PARALLEL_PIXEL_EXPAND_AMOUNT = 1;
    private enum SOType { VERTICAL, HRIGHT, HLEFT }

    public Ruling(float top, float left, float width, float height) {
        this(new Point2D.Float(left, top), new Point2D.Float(left+width, top+height));
    }
    
    public Ruling(Point2D p1, Point2D p2) {
        super(p1, p2);
        this.normalize();
    }

    /**
     * Normalize almost horizontal or almost vertical lines
     */
    public void normalize() {

        double angle = this.getAngle();
        if (Utils.within(angle, 0, 1) || Utils.within(angle, 180, 1)) { // almost horizontal
            this.setLine(this.x1, this.y1, this.x2, this.y1);
        }
        else if (Utils.within(angle, 90, 1) || Utils.within(angle, 270, 1)) { // almost vertical
            this.setLine(this.x1, this.y1, this.x1, this.y2);
        }
    }

    public boolean vertical() {
        return this.length() > 0 && Utils.feq(this.x1, this.x2); //diff < ORIENTATION_CHECK_THRESHOLD;
    }
    
    public boolean horizontal() {
        return this.length() > 0 && Utils.feq(this.y1, this.y2); //diff < ORIENTATION_CHECK_THRESHOLD;
    }
    
    public boolean oblique() {
        return !(this.vertical() || this.horizontal());
    }
    
    // attributes that make sense only for non-oblique lines
    // these are used to have a single collapse method (in page, currently)
    
    public float getPosition() {
        if (this.oblique()) {
            throw new UnsupportedOperationException();
        }
        return this.vertical() ? this.getLeft() : this.getTop();
    }
    
    public void setPosition(float v) {
        if (this.oblique()) {
            throw new UnsupportedOperationException();
        }
        if (this.vertical()) {
            this.setLeft(v);
            this.setRight(v);
        }
        else {
            this.setTop(v);
            this.setBottom(v);
        }
    }
    
    public float getStart() {
        if (this.oblique()) {
            throw new UnsupportedOperationException();
        }
        return this.vertical() ? this.getTop() : this.getLeft();
    }
    
    public void setStart(float v) {
        if (this.oblique()) {
            throw new UnsupportedOperationException();
        }
        if (this.vertical()) {
            this.setTop(v);
        }
        else {
            this.setLeft(v);
        }
    }
    
    public float getEnd() {
        if (this.oblique()) {
            throw new UnsupportedOperationException();
        }
        return this.vertical() ? this.getBottom() : this.getRight();
    }
    
    public void setEnd(float v) {
        if (this.oblique()) {
            throw new UnsupportedOperationException();
        }
        if (this.vertical()) {
            this.setBottom(v);
        }
        else {
            this.setRight(v);
        }
    }

    private void setStartEnd(float start, float end) {
        if (this.oblique()) {
            throw new UnsupportedOperationException();
        }
        if (this.vertical()) {
            this.setTop(start);
            this.setBottom(end);
        }
        else {
            this.setLeft(start);
            this.setRight(end);
        }
    }
    
    // -----
        
    public boolean perpendicularTo(Ruling other) {
        return this.vertical() == other.horizontal();
    }
    
    public boolean colinear(Point2D point) {
        return point.getX() >= this.x1
                && point.getX() <= this.x2
                && point.getY() >= this.y1
                && point.getY() <= this.y2;
    }
    
    // if the lines we're comparing are colinear or parallel, we expand them by a only 1 pixel,
    // because the expansions are additive
    // (e.g. two vertical lines, at x = 100, with one having y2 of 98 and the other having y1 of 102 would
    // erroneously be said to nearlyIntersect if they were each expanded by 2 (since they'd both terminate at 100).
    // By default the COLINEAR_OR_PARALLEL_PIXEL_EXPAND_AMOUNT is only 1 so the total expansion is 2.
    // A total expansion amount of 2 is empirically verified to work sometimes. It's not a magic number from any
    // source other than a little bit of experience.)
    public boolean nearlyIntersects(Ruling another) {
        return this.nearlyIntersects(another, COLINEAR_OR_PARALLEL_PIXEL_EXPAND_AMOUNT, PERPENDICULAR_PIXEL_EXPAND_AMOUNT);
    }

    public boolean nearlyIntersects(Ruling another, int colinearOrParallelExpandAmount, int perpendicularExpandAmout) {
        if (this.intersectsLine(another)) {
            return true;
        }
        
        boolean rv = false;
        
        if (this.perpendicularTo(another)) {
            rv = this.expand(perpendicularExpandAmout).intersectsLine(another.expand(perpendicularExpandAmout));
        }
        else {
            rv = this.expand(colinearOrParallelExpandAmount)
                    .intersectsLine(another.expand(colinearOrParallelExpandAmount));
        }
        
        return rv;
    }
    
    public double length() {
        return Math.sqrt(Math.pow(this.x1 - this.x2, 2) + Math.pow(this.y1 - this.y2, 2));
    }
    
    public Ruling intersect(Rectangle2D clip) {
        Line2D.Float clipee = (Line2D.Float) this.clone();
        boolean clipped = new CohenSutherlandClipping(clip).clip(clipee);

        if (clipped) {
            return new Ruling(clipee.getP1(), clipee.getP2());
        }
        else {
            return this;
        }
    }
    
    public Ruling expand(float amount) {
        Ruling r = (Ruling) this.clone();
        r.setStart(this.getStart() - amount);
        r.setEnd(this.getEnd() + amount);
        return r;
    }
    
    public Point2D intersectionPoint(Ruling other, int perpendicularExpandAmount) {
        Ruling this_l = this.expand(perpendicularExpandAmount);
        Ruling other_l = other.expand(perpendicularExpandAmount);
        Ruling horizontal, vertical;
        
        if (!this_l.intersectsLine(other_l)) {
            return null;
        }
        
        if (this_l.horizontal() && other_l.vertical()) {
            horizontal = this_l; vertical = other_l;
        }
        else if (this_l.vertical() && other_l.horizontal()) {
            vertical = this_l; horizontal = other_l;
        }
        else {
            throw new IllegalArgumentException("lines must be orthogonal, vertical and horizontal");
        }
        return new Point2D.Float(vertical.getLeft(), horizontal.getTop());        
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) 
            return true;
        
        if (!(other instanceof Ruling))
            return false;
        
        Ruling o = (Ruling) other;
        return this.getP1().equals(o.getP1()) && this.getP2().equals(o.getP2());
    }
    
    public float getTop() {
        return this.y1;
    }
    
    public void setTop(float v) {
        setLine(this.getLeft(), v, this.getRight(), this.getBottom());
    }
    
    public float getLeft() {
        return this.x1;
    }
    
    public void setLeft(float v) {
        setLine(v, this.getTop(), this.getRight(), this.getBottom());
    }
    
    public float getBottom() {
        return this.y2;
    }
    
    public void setBottom(float v) {
        setLine(this.getLeft(), this.getTop(), this.getRight(), v);
    }

    public float getRight() {
        return this.x2;
    }
    
    public void setRight(float v) {
        setLine(this.getLeft(), this.getTop(), v, this.getBottom());
    }
    
    public float getWidth() {
        return this.getRight() - this.getLeft();
    }
    
    public float getHeight() {
        return this.getBottom() - this.getTop();
    }
    
    public double getAngle() {
        double angle = Math.toDegrees(Math.atan2(this.getP2().getY() - this.getP1().getY(),
                this.getP2().getX() - this.getP1().getX()));

        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public boolean parallelTo(Ruling other) {
        if (other == null) {
            return false;
        }
        return Utils.feq(this.getAngle(), other.getAngle());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        String rv = formatter.format(Locale.US, "%s[x1=%f y1=%f x2=%f y2=%f]", this.getClass().toString(), this.x1, this.y1, this.x2, this.y2).toString();
        formatter.close();
        return rv;
    }
    
    public static List<Ruling> cropRulingsToArea(List<Ruling> rulings, Rectangle2D area) {
        ArrayList<Ruling> rv = new ArrayList<>();
        for (Ruling r : rulings) {
            if (r.intersects(area)) {
                rv.add(r.intersect(area));
            }
        }
        return rv;
    }
    
    public static Map<Point2D, Ruling[]> findIntersections(List<Ruling> horizontals, List<Ruling> verticals) {
        return findIntersections(horizontals, verticals, PERPENDICULAR_PIXEL_EXPAND_AMOUNT, PERPENDICULAR_PIXEL_EXPAND_AMOUNT);
    }
    
    // log(n) implementation of find_intersections
    // based on http://people.csail.mit.edu/indyk/6.838-old/handouts/lec2.pdf
    public static Map<Point2D, Ruling[]> findIntersections(List<Ruling> horizontals, List<Ruling> verticals, int horizontalExpandAmount, int verticalExpandAmount) {
        
        class SortObject {
            protected SOType type;
            protected float position;
            protected Ruling ruling;
            
            public SortObject(SOType type, float position, Ruling ruling) {
                this.type = type;
                this.position = position;
                this.ruling = ruling;
            }
        }
        
        List<SortObject> sos = new ArrayList<>();
        
        TreeMap<Ruling, Boolean> tree = new TreeMap<>(new Comparator<Ruling>() {
            @Override
            public int compare(Ruling o1, Ruling o2) {
                return java.lang.Double.compare(o1.getTop(), o2.getTop());
            }});
        
        TreeMap<Point2D, Ruling[]> rv = new TreeMap<>(new Comparator<Point2D>() {
            @Override
            public int compare(Point2D o1, Point2D o2) {
                if (o1.getY() > o2.getY()) return  1;
                if (o1.getY() < o2.getY()) return -1;
                if (o1.getX() > o2.getX()) return  1;
                if (o1.getX() < o2.getX()) return -1;
                return 0;
            }
        });
        
        for (Ruling h : horizontals) {
            sos.add(new SortObject(SOType.HLEFT, h.getLeft() - horizontalExpandAmount, h));
            sos.add(new SortObject(SOType.HRIGHT, h.getRight() + horizontalExpandAmount, h));
        }

        for (Ruling v : verticals) {
            sos.add(new SortObject(SOType.VERTICAL, v.getLeft(), v));
        }
        
        Collections.sort(sos, new Comparator<SortObject>() {
            @Override
            public int compare(SortObject a, SortObject b) {
                int rv;
                if (Utils.feq(a.position, b.position)) {
                    if (a.type == SOType.VERTICAL && b.type == SOType.HLEFT) {
                       rv = 1;     
                    }
                    else if (a.type == SOType.VERTICAL && b.type == SOType.HRIGHT) {
                       rv = -1;
                    }
                    else if (a.type == SOType.HLEFT && b.type == SOType.VERTICAL) {
                       rv = -1;
                    }
                    else if (a.type == SOType.HRIGHT && b.type == SOType.VERTICAL) {
                       rv = 1;
                     }
                    else {
                       rv = java.lang.Double.compare(a.position, b.position);
                    }
                }
                else {
                    return java.lang.Double.compare(a.position, b.position);
                }
                return rv;
            }
        });
        
        for (SortObject so : sos) {
            switch(so.type) {
            case VERTICAL:
                for (Map.Entry<Ruling, Boolean> h : tree.entrySet()) {
                    Point2D i = h.getKey().expand(horizontalExpandAmount).intersectionPoint(so.ruling, verticalExpandAmount);
                    if (i == null) {
                        continue;
                    }
                    rv.put(i, 
                           new Ruling[] { h.getKey().expand(horizontalExpandAmount), 
                                          so.ruling.expand(verticalExpandAmount) });
                }
                break;
            case HRIGHT:
                tree.remove(so.ruling);
                break;
            case HLEFT:
                tree.put(so.ruling, true);
                break;
            }
        }
        
        return rv;
        
    }

    public static List<Ruling> collapseOrientedRulings(List<Ruling> lines) {
        return collapseOrientedRulings(lines, COLINEAR_OR_PARALLEL_PIXEL_EXPAND_AMOUNT);
    }
    
    public static List<Ruling> collapseOrientedRulings(List<Ruling> lines, int colinearExpandAmount) {
        return collapseOrientedRulings(lines, colinearExpandAmount, PERPENDICULAR_PIXEL_EXPAND_AMOUNT);
    }

    public static List<Ruling> collapseOrientedRulings(List<Ruling> lines, int colinearExpandAmount, int perpendicularExpandAmout) {
        return collapseOrientedRulings(lines, colinearExpandAmount, perpendicularExpandAmout, 0f);
    }

    public static List<Ruling> collapseOrientedRulings(List<Ruling> lines, int colinearExpandAmount, int perpendicularExpandAmount, float minSpacing) {
        ArrayList<Ruling> rv = new ArrayList<>();

        LinkedList<Ruling> remainings = new LinkedList<>(lines);
        Collections.sort(remainings, new Comparator<Ruling>() {
            @Override
            public int compare(Ruling a, Ruling b) {
                final float diff = a.getPosition() - b.getPosition();
                return java.lang.Float.compare(diff == 0 ? a.getStart() - b.getStart() : diff, 0f);
            }
        });

        while (!remainings.isEmpty()) {
            Ruling collapsedLine = remainings.pop();
            LinkedList<Ruling> others = new LinkedList<>(remainings);
            remainings.clear();
            collapsedLine = new Ruling(collapsedLine.getP1(), collapsedLine.getP2());
            if (collapsedLine.getStart() > collapsedLine.getEnd()) {
                collapsedLine.setStartEnd(collapsedLine.getEnd(), collapsedLine.getStart());
            }
            for (Ruling otherLine : others) {                
                boolean overlapping = minSpacing > 0 && collapsedLine.nearlyOverlaps(otherLine, minSpacing);
                if (overlapping || (Utils.feq(collapsedLine.getPosition(), otherLine.getPosition())
                        && otherLine.nearlyIntersects(collapsedLine, colinearExpandAmount, perpendicularExpandAmount))) {
                    final float otherStart = Math.min(otherLine.getStart(), otherLine.getEnd());
                    final float otherEnd = Math.max(otherLine.getStart(), otherLine.getEnd());

                    float collapsedStart = Math.min(collapsedLine.getStart(), collapsedLine.getEnd());
                    float collapsedEnd = Math.max(collapsedLine.getStart(), collapsedLine.getEnd());

                    final float newStart = Math.min(collapsedStart, otherStart);
                    final float newEnd = Math.max(collapsedEnd, otherEnd);

                    if (collapsedLine.getPosition() != otherLine.getPosition() && overlapping
                            && collapsedLine.length() > 0) {                        
                        // Set the position of the collapsed line as a weighted average of the two lines position
                        float otherLineWeight = (float) (otherLine.length() / (otherLine.length() + collapsedLine.length()));
                        float collapsedLineWeight = 1f - otherLineWeight;
                        collapsedLine.setPosition(
                                otherLineWeight * otherLine.getPosition() + collapsedLineWeight * collapsedLine.getPosition());
                    }

                    collapsedLine.setStartEnd(newStart, newEnd);
                    
                    assert !collapsedLine.oblique();
                } else {
                    remainings.add(otherLine);
                }
            }
            rv.add(collapsedLine);
        }
        return rv;
    }

    private boolean nearlyOverlaps(Ruling other, float proximityThreshold) {
        if (!this.parallelTo(other) || (Math.abs(this.getPosition() - other.getPosition()) > proximityThreshold)) {
            return false;
        }

        if (this.getStart() >= other.getStart() && this.getStart() <= other.getEnd()) {
            return true;
        }

        if (this.getEnd() >= other.getStart() && this.getEnd() <= other.getEnd()) {
            return true;
        }

        if (other.getStart() >= this.getStart() && other.getStart() <= this.getEnd()) {
            return true;
        }

        if (other.getEnd() >= this.getStart() && other.getEnd() <= this.getEnd()) {
            return true;
        }

        return false;
    }
}
