package com.example.lab04.models.documents;



import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomFieldError implements Serializable {

    private static final long serialVersionUID = -2274980594076951337L;

    private String field;
    private String description;


}
