package com.avnish.descriptAI_backend.service;


import com.avnish.descriptAI_backend.model.User;
import com.avnish.descriptAI_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Slf4j
@Service
public class UserDetailServiceImplementation implements  UserDetailsService{

    private final UserRepository userRepo ;


    // implement method to get userdetails using username
    @Override
    public UserDetails loadUserByUsername(String username) throws  UsernameNotFoundException {
        User user = userRepo.findByUserName(username)
                .orElseThrow( () -> new UsernameNotFoundException("User not found: " + username));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
        log.info("userdetails fetched from DB: " + userDetails);
             return userDetails;
    }

}
