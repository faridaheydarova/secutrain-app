package com.securityawareness.secutrainapp.security.jwt;

import com.securityawareness.secutrainapp.model.User;
import com.securityawareness.secutrainapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =userRepository.findByUsername(username).orElseThrow(()->
                new UsernameNotFoundException("user not found:"+username));
        return UserDetailsImpl.build(user);
    }
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException{
        User user=userRepository.findById(userId).orElseThrow(()->
                new UsernameNotFoundException("user not found with id:"+userId));
        return UserDetailsImpl.build(user);
    }
}
