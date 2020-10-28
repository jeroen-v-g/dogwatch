package com.example.dogwatch.dao.entities;

/**
 * Hotel Booking User
 * 
 * @author karesti
 */

public class User
{

    public static final String ALL = "User.all";

    public static final String BY_USERNAME_OR_EMAIL = "User.byUserNameOrEmail";

    public static final String BY_CREDENTIALS = "User.byCredentials";

    private static final long serialVersionUID = 4060967693790504175L;


    private Long id;


    private String username;

    private String fullname;

    private String email;

    private String password;

    public User()
    {
    }

    /**
     * Construct a user
     * @param fullname the full name (e.g. "John Smith")
     * @param username the username
     * @param email the email address
     */
    public User(final String fullname, final String username, final String email)
    {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
    }

    /**
     * Construct a new user using an already-encrypted password
     * @param fullname the full name (e.g. "John Smith")
     * @param username the username
     * @param email the email address
     * @param password the password (already encrypted)
     */
    public User(final String fullname, final String username, final String email,
            final String password)
    {
        this(fullname, username, email);
        this.password = password;
    }

    /**
     * Construct a user using an already-known database ID
     * @param id the database ID (primary key)
     * @param fullname the full name (e.g. "John Smith")
     * @param username the username
     * @param email the email address
     * @param password the password (already encrypted)
     */
    public User(Long id, String username, String fullname, String email, String password)
    {
        super();
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("id ");
        builder.append(id);
        builder.append(",");
        builder.append("username ");
        builder.append(username);
        return builder.toString();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getFullname()
    {
        return fullname;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}