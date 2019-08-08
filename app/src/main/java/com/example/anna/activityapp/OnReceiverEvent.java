package com.example.anna.activityapp;


/*......
Pass state to bakground reciver to main activity
 */
public class OnReceiverEvent {
    private String state_act;
    public OnReceiverEvent(String state){
        this.state_act = state;
    }
    public String getActivityState(){
        return state_act;
    }
}
