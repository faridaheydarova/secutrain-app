package com.securityawareness.secutrainapp.security.ad;

import com.securityawareness.secutrainapp.security.jwt.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class AdUserDetailsContextMapper implements UserDetailsContextMapper {

    private final AdJitProvisioningService jitProvisioningService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        String samAccountName = ctx.getStringAttribute("sAMAccountName");
        String userPrincipalName = ctx.getStringAttribute("userPrincipalName");
        String mail = ctx.getStringAttribute("mail");
        String displayName = ctx.getStringAttribute("displayName");
        String[] memberOf = ctx.getStringAttributes("memberOf");

        String resolvedUsername = (samAccountName != null) ? samAccountName : username;
        String email = (mail != null) ? mail : userPrincipalName;

        jitProvisioningService.provision(resolvedUsername, email, displayName, memberOf);

        return userDetailsService.loadUserByUsername(resolvedUsername);
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("AD inteqrasiyası read-only-dir");
    }
}