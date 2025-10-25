package org.example.new_new_mvp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyContent {
    
    private UUID id;
    
    private Company company;
    
    private ContentType contentType;
    
    private String title;
    
    private Object data;
}