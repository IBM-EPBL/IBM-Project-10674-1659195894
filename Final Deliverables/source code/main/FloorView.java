package bigbrother.child_monitoring_system;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;

public class FloorView extends View implements MapInputDialog.OnCompleteListener {

    private onCircleCreateInterface mInterface;

    private static final String TAG = "FloorView";
    CircleArea touchedCircle;

    /* Our main bitmap */
    private Bitmap mainBitmap = null;

    private Rect mMeasuredRect;
    private int RADIUS = 100;

    /* Booleans for drawing map */
    boolean firstDraw;

    /* UserType for what they are able to do with the map */
    UserType userType;


    public FloorView(final Context context) {
        super(context);
        init(context);
    }
    public FloorView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }


    private class CircleArea {

        String macAddress;
        int radius;
        int centerX;
        int centerY;


        CircleArea(int centerX, int centerY, int radius) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }

        public int getCenterX() {
            return centerX;
        }

        public void setCenterX(int centerX) {
            this.centerX = centerX;
        }

        public int getCenterY() {
            return centerY;
        }

        public void setCenterY(int centerY) {
            this.centerY = centerY;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public String getMacAddress() {
            return macAddress;
        }

        @Override
        public String toString() {
            return "Circle[" + centerX + ", " + centerY + ", " + radius + "]";
        }
    }

    //Paint for circles
    Paint circlePaint;
    //Paint for text
    Paint textPaint;
    Paint textChildPaint;
    Paint smallCirclePaint;

    /* HashSet for circles */
    private HashSet<CircleArea> circles = new HashSet<>();
    private SparseArray<CircleArea> circlePointer = new SparseArray<>();

    public void init(final Context ct) {
        //generate the bitmap used for the background
        int w = 1080, h = 1920;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        mainBitmap = Bitmap.createBitmap(w, h, conf);

        circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#F99F20"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(10);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(48f);

        textChildPaint = new Paint();
        textChildPaint.setColor(Color.BLUE);
        textChildPaint.setTextSize(35f);

        smallCirclePaint = new Paint();
        smallCirclePaint.setColor(Color.BLUE);
        smallCirclePaint.setStyle(Paint.Style.FILL);

//        for (java.util.Map.Entry<String, RoomData> entry: Map.rooms.entrySet()) {
//            //make the rooms into circle
//            CircleArea tempCircle = new CircleArea(Integer.parseInt(entry.getValue().getX()), Integer.parseInt(entry.getValue().getY()),
//                    Integer.parseInt(entry.getValue().getRadius()));
//            tempCircle.setMacAddress(entry.getValue().getMac());
//            circles.add(tempCircle);
//        }

        firstDraw = false;
        userType = null;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawBitmap(mainBitmap, null, mMeasuredRect, null);

        int margin = 0;

        if(firstDraw == false) {
            for (java.util.Map.Entry<String, RoomData> entry: Map.rooms.entrySet()) {
                //make the rooms into circle
                CircleArea tempCircle = new CircleArea(Integer.parseInt(entry.getValue().getX()), Integer.parseInt(entry.getValue().getY()),
                        Integer.parseInt(entry.getValue().getRadius()));
                tempCircle.setMacAddress(entry.getValue().getMac());
                circles.add(tempCircle);
            }
            firstDraw = true;
        }

        for (CircleArea circle : circles) {
            canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, circlePaint);

            if(circle.getMacAddress() == null) {
                canvas.drawText("None", circle.centerX - 200, circle.centerY - 120, textPaint);
            } else {
                canvas.drawText(circle.getMacAddress(), circle.centerX - 150, circle.centerY-140, textPaint);
            }

            //draw the children in relative location to the MAC address of the raspberry pi
            for (ChildDataObject child : Map.children) {

                //check to see if the circle mac is null which it will be if it was just created
                if(circle.getMacAddress() != null) {

                    //find the child who has been located by this Rasp Pi
                    if(circle.getMacAddress().equals(child.getLocationMAC())) {
                        //they have been located by that reciever
                        canvas.drawText(child.getName(), circle.centerX - 30, circle.centerY + margin, textChildPaint);
                        canvas.drawCircle(circle.centerX - 40, circle.centerY + margin - 10, 10, smallCirclePaint);
                        margin += 20;
                    }

                }

            }

        }

    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        int xTouch;
        int yTouch;
        int pointerId;

        int actionIndex = event.getActionIndex();

        if(userType != null && userType == UserType.ADMIN) {
            // get the touch event and make a transparent circle with it
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    //first pointer so clear all existing pointer data
                    clearCirclePointer();

                    xTouch = (int) event.getX(0);
                    yTouch = (int) event.getY(0);

                    //check if we touched the inside of a circle already
                    touchedCircle = obtainTouchedCircle(xTouch, yTouch);
                    touchedCircle.centerX = xTouch;
                    touchedCircle.centerY = yTouch;
                    circlePointer.put(event.getPointerId(0), touchedCircle);

                    invalidate();
                    handled = true;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.w(TAG, "Pointer down");
                    // It secondary pointers, so obtain their ids and check circles
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    // check if we've touched inside some circle
                    touchedCircle = obtainTouchedCircle(xTouch, yTouch);

                    circlePointer.put(pointerId, touchedCircle);
                    touchedCircle.centerX = xTouch;
                    touchedCircle.centerY = yTouch;
                    invalidate();
                    handled = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int pointerCount = event.getPointerCount();

                    for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                        // Some pointer has moved, search it by pointer id
                        pointerId = event.getPointerId(actionIndex);

                        xTouch = (int) event.getX(actionIndex);
                        yTouch = (int) event.getY(actionIndex);

                        touchedCircle = circlePointer.get(pointerId);

                        //check to see if the circle has the data of the mac address
                        Log.w(TAG, ""+touchedCircle.getMacAddress());

                        //print x and y coordinated
                        Log.d("COORDS", "x: "+xTouch + ", y: "+yTouch);

                        ////check to see if the circle is brought to delete section of the map
                        if(xTouch >= 1000 && yTouch >= 1450) {
                            mInterface.onCircleDelete(touchedCircle.getMacAddress());
                            circles.remove(touchedCircle);
                            break;
                        }



                        if (null != touchedCircle.getMacAddress()) {
                            touchedCircle.centerX = xTouch;
                            touchedCircle.centerY = yTouch;

                            //send the data back to the Map class to send off to the database
                            mInterface.onCircleCreate(touchedCircle.getMacAddress(), touchedCircle.getRadius(),
                                    touchedCircle.getCenterX(), touchedCircle.getCenterY());

                        }
                    }
                    invalidate();
                    handled = true;
                    break;
                case MotionEvent.ACTION_UP:
                    clearCirclePointer();;
                    invalidate();
                    handled = true;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    // not general pointer was up
                    pointerId = event.getPointerId(actionIndex);

                    circlePointer.remove(pointerId);
                    invalidate();
                    handled = true;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    handled = true;
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event) || handled;

    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    private CircleArea obtainTouchedCircle(final int xTouch, final int yTouch) {
        touchedCircle = getTouchedCircle(xTouch, yTouch);

        if (null == touchedCircle) {

            touchedCircle = new CircleArea(xTouch, yTouch, RADIUS);

            //open dialog here to input name and mac of the PI
            //interface with the Map activity to get it to open a dialog box
            mInterface.onCircleCreate();

            Log.w(TAG, "Added circle " + touchedCircle);
            circles.add(touchedCircle);
        }

        return touchedCircle;
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return {@link CircleArea} touched circle or null if no circle has been touched
     */
    private CircleArea getTouchedCircle(final int xTouch, final int yTouch) {
        CircleArea touched = null;

        for (CircleArea circle : circles) {
            if ((circle.centerX - xTouch) * (circle.centerX - xTouch) + (circle.centerY - yTouch) * (circle.centerY - yTouch) <= circle.radius * circle.radius) {
                touched = circle;
                break;
            }
        }

        return touched;
    }

    /**
     * Clears all CircleArea - pointer id relations
     */
    private void clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer");

        circlePointer.clear();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }


    @Override
    public void onComplete(String name, String mac) {
        if(mac == null) {
            circles.remove(touchedCircle);
        }
        touchedCircle.setMacAddress(mac);

    }


    //create interface to allow the parent activity to open a dialog
    public static interface onCircleCreateInterface {
        public abstract void onCircleCreate();
        public abstract void onCircleCreate(String mac, int radius, int xCor, int yCor);
        public abstract void onCircleDelete(String mac);
    }


    //set the listener for the creation of a circle
    public void setViewListener(onCircleCreateInterface iface) {
        mInterface = iface;
    }




}