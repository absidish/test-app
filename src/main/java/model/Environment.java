package model;


import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import starter.Start;


@Table( keyspace = Start.KEYSPACE, name = Environment.TABLE )
public class Environment
{

    public static final String TABLE = "test_environments";
    public static final String USER_BY_ENVIRONMENT = "test_user_by_environment";

    public static final String ENVIRONMENT_ID = "id";
    public static final String ENVIRONMENT_ID_HLPER = "environment_id";
    public static final String ENVIRONMENT_NAME = "test_environments";
    public static final String ENVIRONMENT_DESCRIPTION = "test_environments";
    public static final String ENVIRONMENT_ACCESS_TOKEN = "access_token";


    @PartitionKey
    @Column( name = ENVIRONMENT_ID )
    private UUID id;

    @Column( name = ENVIRONMENT_NAME )
    private String name;

    @Column( name = ENVIRONMENT_DESCRIPTION )
    private String description;

    @Column( name = ENVIRONMENT_ACCESS_TOKEN )
    private UUID accessToken;


    public Environment()
    {
    }


    public Environment( final UUID envId, final String name, final String description )
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


    public UUID getAccessToken()
    {
        return accessToken;
    }


    public void setAccessToken( final UUID accessToken )
    {
        this.accessToken = accessToken;
    }
}
