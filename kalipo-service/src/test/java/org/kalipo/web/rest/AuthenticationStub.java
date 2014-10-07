package org.kalipo.web.rest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Class to support Authentication in Tests
 * Created by damoeb on 21.09.14.
 */
public class AuthenticationStub implements Authentication {

    private Collection<? extends GrantedAuthority> authorities;
    private Object credentials;
    private Object details;
    private Object principal;
    private boolean authenticated;
    private String name;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public void setName(String name) {
        this.name = name;
    }
}
