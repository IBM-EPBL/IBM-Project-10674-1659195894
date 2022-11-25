package bigbrother.child_monitoring_system;

//class for roomdata
public class RoomData {

    private String mac, x, y, radius;

    public RoomData(){
        //stuff
    }

    public RoomData(String mac, int x, int y, int radius) {
        this.mac = mac;
        this.x = Integer.toString(x);
        this.y = Integer.toString(y);
        this.radius = Integer.toString(radius);
    }

    public String getRadius() {
        return radius;
    }
    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getX() {
        return x;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getY() {
        return y;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getMac() {
        return mac;
    }

    public void setY(String y) {
        this.y = y;
    }

}