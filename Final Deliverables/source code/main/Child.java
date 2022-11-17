package bigbrother.child_monitoring_system;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by mboa3 on 2/14/17.
 */
@IgnoreExtraProperties
public class Child {

    private String parentEmail;
    private String firstName;
    private String lastName;
    private String schoolName;
    private int idNum;

    public Child() {
        //default
    }

    public void setEmail(String parentEmail) {
        this.parentEmail = parentEmail;
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

    public void setIdNum(int idNum) {
        this.idNum = idNum;
    }

    public String getParentEmail() {
        return this.parentEmail;
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

    public int getIdNum() { return this.idNum; }

    public void setValues(String firstName, String lastName, String schoolName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolName = schoolName;
        this.parentEmail = parentEmail;
    }
}