package bigbrother.child_monitoring_system;

import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.plus.model.people.Person;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wbroome14 on 1/23/17.
 */
@IgnoreExtraProperties
public class User implements Serializable {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String schoolName;
    private String childFirstName;
    private String childLastName;
    private UserType type;
    private String token;
    private boolean banned;
    private ArrayList<ChildDataObject> children;


    public User() {
        //defualt
    }

    public ArrayList<ChildDataObject> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<ChildDataObject> children) {
        this.children = children;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSchoolName(String school) {
        this.schoolName = school;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public String toString() {
        return getFirstName() + " " + getLastName();
    }

    public UserType getType() {
        return type;
    }

    public boolean getBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }

    public void setValues(String firstName, String lastName, String schoolName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolName = schoolName;
        this.email = email;
    }
}