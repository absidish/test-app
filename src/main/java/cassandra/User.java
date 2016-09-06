package cassandra;


import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;


@Table( keyspace = Start.KEYSPACE, name = User.TABLE )
public class User
{
    public static final String TABLE = "test_users";
    public static final String ENVIRONMENTS_BY_USER = "test_environments_by_user";

    @PartitionKey
    @Column( name = "id" )
    private UUID id;

    @Column( name = "name" )
    private String name;

    @Column( name = "email" )
    private String email;

    @Column( name = "login" )
    private String login;

    @Column( name = "access_token" )
    private UUID accessToken;


    public User()
    {

    }


    public User( final UUID useId, final String email, final String login  , final String name )
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
