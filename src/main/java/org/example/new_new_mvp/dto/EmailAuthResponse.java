package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailAuthResponse {
    
    private boolean success;
    private String message;
    private String token;
    private String email;
    private String fullName;
    
    public static EmailAuthResponse success(String token, String email, String fullName) {
        return new EmailAuthResponse(true, "Авторизация успешна", token, email, fullName);
    }
    
    public static EmailAuthResponse error(String message) {
        return new EmailAuthResponse(false, message, null, null, null);
    }
}
