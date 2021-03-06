package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Authority;
import org.kalipo.domain.Comment;
import org.kalipo.domain.PersistentToken;
import org.kalipo.domain.User;
import org.kalipo.repository.AuthorityRepository;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.PersistentTokenRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.RandomUtil;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    public static final int FAILED_LOGIN_COOLDOWN = 3;

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private ReputationModifierService reputationModifierService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private PersistentTokenRepository persistentTokenRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    public User activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return Optional.ofNullable(userRepository.getUserByActivationKey(key))
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);
                    userRepository.save(user);
                    log.debug("Activated user: {}", user);
                    return user;
                })
                .orElse(null);
    }

    public User createUserInformation(String login, String password, String firstName, String lastName, String email,
                                      String langKey) {
        User newUser = new User();
        Authority authority = authorityRepository.findOne("ROLE_USER");
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setLangKey(langKey);
        newUser.setRegistrationDate(DateTime.now());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);

        reputationModifierService.onUserCreation(newUser);

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public void updateUserInformation(String firstName, String lastName, String email) {
        User currentUser = userRepository.findOne(SecurityUtils.getCurrentLogin());
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        userRepository.save(currentUser);
        log.debug("Changed Information for User: {}", currentUser);
    }

    public void changePassword(String password) {
        User currentUser = userRepository.findOne(SecurityUtils.getCurrentLogin());
        String encryptedPassword = passwordEncoder.encode(password);
        currentUser.setPassword(encryptedPassword);
        userRepository.save(currentUser);
        log.debug("Changed password for User: {}", currentUser);
    }

    public User getUserWithAuthorities() {
        User currentUser = userRepository.findOne(SecurityUtils.getCurrentLogin());
        currentUser.getAuthorities().size(); // eagerly load the association
        return currentUser;
    }

    /**
     * Persistent Token are used for providing automatic authentication, they should be automatically deleted after
     * 30 days.
     * <p/>
     * <p>
     * This is scheduled to get fired everyday, at midnight.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldPersistentTokens() {
        LocalDate now = new LocalDate();
        List<PersistentToken> tokens = persistentTokenRepository.findByTokenDateBefore(now.minusMonths(1));
        for (PersistentToken token : tokens) {
            log.debug("Deleting token {}", token.getSeries());
            persistentTokenRepository.delete(token);
        }
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void removeNotActivatedUsers() {
//        DateTime now = new DateTime();
//        List<User> users = userRepository.findNotActivatedUsersByCreationDateBefore(now.minusDays(3));
//        for (User user : users) {
//            log.debug("Deleting not activated user {}", user.getLogin());
//            userRepository.delete(user);
//        }
//    }

    /**
     * Reset loginTries to 0 after {FAILED_LOGIN_COOLDOWN} hours
     */
    @Scheduled(cron = "0 * * * * ?") // hourly
    public void resetLoginTries() {
        List<User> list = userRepository.findHavingLoginTries(new DateTime().minusHours(FAILED_LOGIN_COOLDOWN));
        for (User user : list) {
            user.setLoginTries(0);
        }
        userRepository.save(list);
    }


    public User findOne(String login) {
        return userRepository.findOne(login);
    }

    @RolesAllowed(Privileges.BAN_USER)
    public User ban(String login) throws KalipoException {
        User user = userRepository.findOne(login);

        Asserts.isNotNull(user, "login");

        user.setBanned(true);

        return userRepository.save(user);
    }

    public boolean isSuperMod(String userId) {
        User one = userRepository.findOne(userId);
        return isAdmin(userId) || (one != null && one.isSuperMod());
    }

    public Boolean ignoreAuthorOfComment(String commentId) throws KalipoException {
        Asserts.isNotNull(commentId, "commentId");
        Comment comment = commentRepository.findOne(commentId);
        Asserts.isNotNull(comment, "comment does not exist");

        User user = userRepository.findOne(SecurityUtils.getCurrentLogin());

        if (Boolean.TRUE == comment.getAnonymous()) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "Comment author posted anonymous");
        }
        if (!user.getIgnoredUsers().contains(comment.getAuthorId())) {
            user.getIgnoredUsers().add(comment.getAuthorId());
            log.info("User '%s' ignores '%s'", SecurityUtils.getCurrentLogin(), comment.getAuthorId());
        }

        return true;
    }

    public User updateUser(User modified) throws KalipoException {
        Asserts.isNotNull(modified, "payload");

        final String login = modified.getLogin();
        final User original = userRepository.findOne(login);
        final String operator = SecurityUtils.getCurrentLogin();
        final User self = userRepository.findOne(operator);
        final boolean isSuperMod = self.isSuperMod();
        final boolean isAdmin = isAdmin(self.getLogin());

        if (!isSuperMod && !isAdmin) {
            log.warn(String.format("%s tried to alter user %s", operator, login));
            throw new KalipoException(ErrorCode.PERMISSION_DENIED, "Superpowers required");
        }

        // update only fields that are set
        if (dirty(modified.getFirstName(), original.getFirstName())) {
            original.setFirstName(modified.getFirstName());
            logFieldChange(operator, login, "firstName", modified.getFirstName());
        }
        if (dirty(modified.getLastName(), original.getLastName())) {
            original.setLastName(modified.getLastName());
            logFieldChange(operator, login, "lastName", modified.getLastName());
        }
        if (dirty(modified.getEmail(), original.getEmail())) {
            original.setEmail(modified.getEmail());
            logFieldChange(operator, login, "email", modified.getEmail());
        }
        if (dirty(modified.isBanned(), original.isBanned())) {
            original.setBanned(modified.isBanned());
            if (modified.getBannedUntilDate() == null) {
                throw new KalipoException(ErrorCode.INVALID_PARAMETER, "BannedUntilDate is missing");
            }
            original.setBannedUntilDate(modified.getBannedUntilDate());

            logFieldChange(operator, login, "banned", modified.isBanned());
            logFieldChange(operator, login, "bannedUntilDate", modified.getBannedUntilDate());
        }
        if (dirty(modified.getStrikes(), original.getStrikes())) {
            original.setStrikes(modified.getStrikes());
            logFieldChange(operator, login, "strikes", modified.getStrikes());
        }
        if (dirty(modified.getLastStrikeDate(), original.getLastStrikeDate())) {
            original.setLastStrikeDate(modified.getLastStrikeDate());
            logFieldChange(operator, login, "lastStrikeDate", modified.getLastStrikeDate());
        }
        if (dirty(modified.getLockoutEndDate(), original.getLockoutEndDate())) {
            original.setLockoutEndDate(modified.getLockoutEndDate());
            logFieldChange(operator, login, "lockoutEndDate", modified.getLockoutEndDate());
        }

        if (isAdmin) {
            if (dirty(modified.getActivated(), original.getActivated())) {
                original.setActivated(modified.getActivated());
                logFieldChange(operator, login, "activated", modified.getActivated());
            }
            if (dirty(original.getReputation(), modified.getReputation())) {
                original.setReputation(modified.getReputation());
                logFieldChange(operator, login, "reputation", modified.getReputation());
            }
            if (dirty(original.isSuperMod(), modified.isSuperMod())) {
                original.setSuperMod(modified.isSuperMod());
                logFieldChange(operator, login, "superMod", modified.isSuperMod());
            }

        } else {
            // must not be changed by supermods
            Asserts.nullOrEqual(modified.getActivated(), original.getActivated(), "activated");
            Asserts.nullOrEqual(modified.getReputation(), original.getReputation(), "reputation");
            Asserts.nullOrEqual(modified.isSuperMod(), original.isSuperMod(), "superMod");
        }

        if (!original.equals(modified)) {
//        todo log new version, fix equals
            log.info(String.format("User %s changed to", modified.getLogin()), modified);
        }

        return userRepository.save(original);
    }

    private boolean isAdmin(String userId) {
        return StringUtils.equals(userId, "admin");
    }

    private void logFieldChange(String operator, String login, String fieldName, Object fieldValue) {
        log.info(String.format("%s changes %s value of %s to '%s'", operator, login, fieldValue, fieldName));
    }

    private boolean dirty(int dirty, int original) {
        return dirty != original;
    }

    private boolean dirty(boolean dirty, boolean original) {
        return dirty != original;
    }

    private boolean dirty(DateTime dirty, DateTime original) {
        return dirty != null && !dirty.equals(original);
    }

    private boolean dirty(String dirty, String original) {
        return StringUtils.isNotBlank(dirty) && !StringUtils.equals(dirty, original);
    }
}
