package com.avnish.descriptAI_backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Document(collection = "user")
public class User {

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    @NotBlank(message = "Password cannot be blank")
    private String password; // save hashed format  BCrypt hashed
    private String role;  // user or admin

}
