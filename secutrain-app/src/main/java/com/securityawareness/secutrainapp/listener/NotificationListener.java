package com.securityawareness.secutrainapp.listener;

import com.securityawareness.secutrainapp.event.TrainingAssignedEvent;
import com.securityawareness.secutrainapp.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final MailService mailService;

    @Async
    @EventListener
    public void handleTrainingAssigned(TrainingAssignedEvent event){
      String subject="A new training session has been scheduled: "+ event.getTrainingTitle();
      String body=String.format(
              "Hello %s,\n\nYou have been assigned the training titled '%s'.\nPlease log in to the platform and start the training.\n\nBest regards,\nSecuTrain",
              event.getUserName(),
              event.getTrainingTitle());
      try{
          mailService.sendSimpleMail(event.getUserEmail(), subject, body);
          log.info("email sent: {}", event.getUserEmail());
      }catch(Exception e){
          log.error("Email not sent: {}", event.getUserEmail(),e);
      }
    }
}
