package com.meitalk.auth.service;

import com.meitalk.auth.mapper.UserMapper;
import com.meitalk.auth.model.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String mailId) throws UsernameNotFoundException {
        CustomUserDetails user = userMapper.selectUserByEmail(mailId);
        if (user == null) {
            throw new IllegalArgumentException("not found user for email");
        }
        return user;
    }
}
