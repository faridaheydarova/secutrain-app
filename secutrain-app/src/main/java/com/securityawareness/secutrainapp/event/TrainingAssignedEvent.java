package com.securityawareness.secutrainapp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TrainingAssignedEvent extends ApplicationEvent {
    private final String userEmail;
    private final String userName;
    private final String trainingTitle;

    public TrainingAssignedEvent(Object source,String userEmail, String userName, String trainingTitle) {
        super(source);
        this.userEmail=userEmail;
        this.userName=userName;
        this.trainingTitle=trainingTitle;
    }
}
