package com.jaskaran.pm.patientservice.service;

import com.jaskaran.pm.patientservice.dto.PatientRequestDTO;
import com.jaskaran.pm.patientservice.dto.PatientResponseDTO;
import com.jaskaran.pm.patientservice.exception.EmailAlreadyExistsException;
import com.jaskaran.pm.patientservice.exception.PatientNotFoundException;
import com.jaskaran.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.jaskaran.pm.patientservice.mapper.PatientMapper;
import com.jaskaran.pm.patientservice.model.Patient;
import com.jaskaran.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    //dependency injection
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;


    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {

        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient withh this email" + "already exists" + patientRequestDTO.getEmail());
        }
        Patient mewPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(mewPatient.getId().toString(), mewPatient.getName(), mewPatient.getEmail());

        return PatientMapper.toDTO(mewPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {

        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with ID:" + id));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)) {
            throw new EmailAlreadyExistsException("A patient with this email" + "already exists" + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }


    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }


}
