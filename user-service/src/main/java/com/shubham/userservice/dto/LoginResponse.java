package com.shubham.userservice.dto;

public class LoginResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String message;
    private String token;

    public LoginResponse() {}
    public LoginResponse(Long userId, String name, String email, String role, String message, String token) {
        this.userId = userId; this.name = name; this.email = email; this.role = role; this.message = message; this.token = token;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public static LoginResponseBuilder builder() { return new LoginResponseBuilder(); }

    public static class LoginResponseBuilder {
        private Long userId; private String name; private String email; private String role; private String message; private String token;
        LoginResponseBuilder() {}
        public LoginResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public LoginResponseBuilder name(String name) { this.name = name; return this; }
        public LoginResponseBuilder email(String email) { this.email = email; return this; }
        public LoginResponseBuilder role(String role) { this.role = role; return this; }
        public LoginResponseBuilder message(String message) { this.message = message; return this; }
        public LoginResponseBuilder token(String token) { this.token = token; return this; }
        public LoginResponse build() { return new LoginResponse(userId, name, email, role, message, token); }
    }
}