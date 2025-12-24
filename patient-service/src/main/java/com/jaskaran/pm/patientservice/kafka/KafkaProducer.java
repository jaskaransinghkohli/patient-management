package com.jaskaran.pm.patientservice.kafka;

import com.jaskaran.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    //To send the messages. This means we will send a key value pair as an event
    private final KafkaTemplate<String,byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String,byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendEvent(Patient patient){
        //Create an event that has all the methods in it

        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();
        try{
            kafkaTemplate.send("patient",event.toByteArray());
        }catch(Exception e){
            log.error("Error sending PatientCreated event : {}",event);
            e.printStackTrace();
        }
    }

}
