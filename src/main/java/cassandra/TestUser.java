package cassandra;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;


@Table( name = TestUser.TABLE )
public class TestUser
{
    public static final String TABLE = "test_users";
    public static final String ENVIRONMENTS_BY_USER = "test_environments_by_user";

    @Id
    @Column( name = "id" )
    private UUID id;

    @Column( name = "name" )
    private String name;

    @Column( name = "email" )
    private String email;

    @Column( name = "login" )
    private String login;


    public TestUser()
    {

    }


    public TestUser( final UUID useId, final String name, final String email, final String login )
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
}
