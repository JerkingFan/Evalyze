package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.new_new_mvp.model.ContentType;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyContentDto {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private ContentType contentType;
    private String title;
    private String data;
}
