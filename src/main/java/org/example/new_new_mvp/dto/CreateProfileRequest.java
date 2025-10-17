package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {
    
    private String employeeEmail;
    private String employeeName;
    private String currentPosition;
    private String currentSkills;
    private String currentResponsibilities;
    private String desiredPosition;
    private String desiredSkills;
    private String careerGoals;
}
