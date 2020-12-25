package org.itron.itrain.core.model;

import javax.persistence.*;

/**
 * @Author: 着凉的皮皮虾
 * @Date: 2019/7/31 22:57
 */
@Table(name = "user")
@Entity
public class User {

    @Id
    @GeneratedValue
    private Integer id;

    private String username;

    private String password;

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
