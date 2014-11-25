package com.course.lab.dailyselfie;


public enum AppStatus
{
    INSTANCE;
    
    private Status status;
    
    enum Status
    {
        Created, Started, Resumed, Paused, Stopped, Destroyed
    }
    
    Status getStatus()
    {
        return status;
    }
    
    void setStatus(Status s)
    {
        status = s;
    }
}
