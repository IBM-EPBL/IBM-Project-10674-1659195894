package bigbrother.child_monitoring_system;

/**
 * Created by wbroome14 on 2/8/17.
 */

class SchoolName {
    private String name;

    public SchoolName() {
        //default
    }

    public SchoolName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}