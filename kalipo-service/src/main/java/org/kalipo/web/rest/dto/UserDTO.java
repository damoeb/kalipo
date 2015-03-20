package org.kalipo.web.rest.dto;

import java.util.List;

public class UserDTO {

    private String login;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private String langKey;

    // todo use a more complex field, providing the changes of last week. Label could be 156 +4
    private int reputation;

    private List<String> roles;

    public UserDTO() {
    }

    public UserDTO(String login, String password, String firstName, String lastName, String email, String langKey,
                   List<String> roles, int reputation) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.langKey = langKey;
        this.roles = roles;
        this.reputation = reputation;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getLangKey() {
        return langKey;
    }

    public int getReputation() {
        return reputation;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserDTO{");
        sb.append("login='").append(login).append('\'');
        if(password != null) {
            sb.append(", password='").append(password.length()).append('\'');
        }
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", langKey='").append(langKey).append('\'');
        sb.append(", roles=").append(roles);
        sb.append('}');
        return sb.toString();
    }
}
