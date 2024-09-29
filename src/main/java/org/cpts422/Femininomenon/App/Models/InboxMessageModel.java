package org.cpts422.Femininomenon.App.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
public class InboxMessageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @Column(nullable = false)
    private String message;

    private LocalDateTime timestamp;

    private boolean isRead;



    public InboxMessageModel() {
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    public InboxMessageModel(UserModel user, String message) {
        this();
        this.user = user;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
