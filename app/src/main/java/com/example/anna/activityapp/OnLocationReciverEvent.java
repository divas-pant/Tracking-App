package com.example.anna.activityapp;
/*......
Pass location to bakground reciver to main activity
 */
public class OnLocationReciverEvent {
    private String location;

    public OnLocationReciverEvent(String loc) {
        location=loc;
    }

    public String getLocation() {
        return location;
    }

}
