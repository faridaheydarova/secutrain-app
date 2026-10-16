package com.securityawareness.secutrainapp.security.ad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

@Configuration
public class ActiveDirectoryConfig {

    @Value("${app.ad.domain}")
    private String domain;

    @Value("${app.ad.url}")
    private String url;

    @Value("${app.ad.root-dn}")
    private String rootDn;

    @Value("${app.ad.search-filter}")
    private String searchFilter;

    @Bean
    public ActiveDirectoryLdapAuthenticationProvider adAuthenticationProvider(AdUserDetailsContextMapper mapper) {
        ActiveDirectoryLdapAuthenticationProvider provider =
                new ActiveDirectoryLdapAuthenticationProvider(domain, url, rootDn);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setSearchFilter(searchFilter);
        provider.setUserDetailsContextMapper(mapper);
        return provider;
    }
}