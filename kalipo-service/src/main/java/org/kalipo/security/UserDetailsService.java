package org.kalipo.security;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Authority;
import org.kalipo.domain.Ban;
import org.kalipo.domain.Privilege;
import org.kalipo.domain.User;
import org.kalipo.repository.BanRepository;
import org.kalipo.repository.PrivilegeRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private BanRepository banRepository;

    @Inject
    private PrivilegeRepository privilegeRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.info("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase();

        User userFromDatabase = userRepository.findOne(lowercaseLogin);
        if (userFromDatabase == null) {
            throw new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database");
        } else if (!userFromDatabase.getActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }

        List<Ban> bans = banRepository.findByUserId(userFromDatabase.getLogin());
        for (Ban ban : bans) {
            if (ban.getValidUntil().isAfterNow()) {
                throw new UserDeniedAuthorizationException(String.format("User %s is banned until %s", ban.getUserId(), ban.getValidUntil()));
            }
        }

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Authority authority : userFromDatabase.getAuthorities()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName()));
        }

        // append privileges according to reputation
        // todo negative reputation cannot be lower than 0, now
        List<Privilege> privileges = privilegeRepository.findByReputationLowerThanOrEqual(Math.max(0, userFromDatabase.getReputation()));
        for (Privilege privilege : privileges) {
            grantedAuthorities.add(new SimpleGrantedAuthority(privilege.getName()));
        }

        return new org.springframework.security.core.userdetails.User(lowercaseLogin, userFromDatabase.getPassword(),
                grantedAuthorities);
    }
}
