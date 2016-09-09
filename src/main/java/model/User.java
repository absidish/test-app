package model;


import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import starter.Start;


@Table( keyspace = Start.KEYSPACE, name = User.TABLE )
public class User
{
    public static final String TABLE = "test_users";
    public static final String ENVIRONMENTS_BY_USER = "test_environments_by_user";

    public static final String USER_ID = "id";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_LOGIN = "login";
    public static final String USER_ACCESS_TOKEN = "access_token";

    public static final String USER_ID_HELPER = "user_id";

    @PartitionKey
    @Column( name = USER_ID )
    private UUID id;

    @Column( name = USER_NAME )
    private String name;

    @Column( name = USER_EMAIL )
    private String email;

    @Column( name = USER_LOGIN )
    private String login;

    @Column( name = USER_ACCESS_TOKEN )
    private UUID accessToken;


    public User()
    {

    }


    public User( final UUID useId, final String email, final String login, final String name )
    {
        this.id = useId;
        this.name = name;
        this.email = email;
        this.login = login;
    }


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getEmail()
    {
        return email;
    }


    public void setEmail( final String email )
    {
        this.email = email;
    }


    public String getLogin()
    {
        return login;
    }


    public void setLogin( final String login )
    {
        this.login = login;
    }


    public UUID getAccessToken()
    {
        return accessToken;
    }


    public void setAccessToken( final UUID accessToken )
    {
        this.accessToken = accessToken;
    }
}
