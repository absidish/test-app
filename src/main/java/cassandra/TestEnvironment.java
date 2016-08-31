package cassandra;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;


@Table( name = TestEnvironment.TABLE )
public class TestEnvironment
{

    public static final String TABLE = "test_environments";
    public static final String USER_BY_ENVIRONMENT = "test_user_by_environment";

    @Id
    @Column( name = "id" )
    private UUID id;

    @Column( name = "name" )
    private String name;

    @Column( name = "description" )
    private String description;


    public TestEnvironment()
    {
    }


    public TestEnvironment( final UUID envId, final String name, final String description )
    {
        this.id = envId;
        this.name = name;
        this.description = description;
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


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }
}
