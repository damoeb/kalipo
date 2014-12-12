package org.kalipo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A user.
 */

@Document(collection = "T_USER")
public class User extends AbstractAuditingEntity implements Serializable {

    // todo login for admin only from localhost
    @NotNull
    @Size(min = 0, max = 20)
    @Id
    private String login;

    @JsonIgnore
    @Size(min = 0, max = 100)
    private String password;

    @Size(min = 0, max = 50)
    @Field("first_name")
    private String firstName;

    @Size(min = 0, max = 50)
    @Field("last_name")
    private String lastName;

    @Email
    @Size(min = 0, max = 100)
    private String email;

    private boolean activated = false;

    @Size(min = 2, max = 5)
    @Field("lang_key")
    private String langKey;

    @Size(min = 0, max = 20)
    @Field("activation_key")
    private String activationKey;

    @Field("reputation")
    private int reputation;

    @JsonIgnore
    private boolean banned;

    private DateTime bannedUntilDate;

    @NotNull(message = "{constraint.notnull.registrationDate}")
    @Field("registration_date")
    private DateTime registrationDate;

    // Count violations
    private int strikes;

    @JsonIgnore
    private DateTime lastStrikeDate;

    private boolean superMod;

    // prevent brute force login attacks
    private int loginTries;

    private DateTime lastLoginTry;

    private DateTime lockoutEndDate;

    @JsonIgnore
    private Set<Authority> authorities = new HashSet<Authority>();

    private Set<PersistentToken> persistentTokens = new HashSet<PersistentToken>();

    public DateTime getLockoutEndDate() {
        return lockoutEndDate;
    }

    public void setLockoutEndDate(DateTime lockoutEndDate) {
        this.lockoutEndDate = lockoutEndDate;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isSuperMod() {
        return superMod;
    }

    public void setSuperMod(boolean superMod) {
        this.superMod = superMod;
    }

    public int getStrikes() {
        return strikes;
    }

    public void setStrikes(int strikes) {
        this.strikes = strikes;
    }

    public DateTime getBannedUntilDate() {
        return bannedUntilDate;
    }

    public void setBannedUntilDate(DateTime bannedUntilDate) {
        this.bannedUntilDate = bannedUntilDate;
    }

    public DateTime getLastStrikeDate() {
        return lastStrikeDate;
    }

    public void setLastStrikeDate(DateTime lastStrikeDate) {
        this.lastStrikeDate = lastStrikeDate;
    }

    public int getLoginTries() {
        return loginTries;
    }

    public void setLoginTries(int loginTries) {
        this.loginTries = loginTries;
    }

    public DateTime getLastLoginTry() {
        return lastLoginTry;
    }

    public void setLastLoginTry(DateTime lastLoginTry) {
        this.lastLoginTry = lastLoginTry;
    }

    public DateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(DateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (!login.equals(user.login)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", activated='" + activated + '\'' +
                ", langKey='" + langKey + '\'' +
                ", activationKey='" + activationKey + '\'' +
                "}";
    }
}
