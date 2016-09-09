package dao;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import model.Environment;
import model.User;
import starter.Start;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.ttl;


public class EnvironmentDao
{
    public static final ConsistencyLevel CONSISTENCY_LEVEL = ConsistencyLevel.QUORUM;

    private static final Logger log = LoggerFactory.getLogger( EnvironmentDao.class );


    public static List<Environment> getEnvironments()
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, Environment.TABLE );

        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        return Start.mappingSession.mapper( Environment.class ).map( resultSet ).all().stream()
                                   .filter( environment -> environment.getAccessToken() == null )
                                   .collect( Collectors.toList() );
    }


    public static void deleteEnvironment( UUID environmentId )
    {
        BatchStatement batchStatement = new BatchStatement();

        UUID userId = getOwnerId( environmentId );

        Statement deleteUser = QueryBuilder.delete().from( Start.KEYSPACE, Environment.TABLE )
                                           .where( eq( Environment.ENVIRONMENT_ID, environmentId ) );

        if ( userId != null )
        {
            Statement deleteEnvsRelations =
                    QueryBuilder.delete().from( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                .where( eq( User.USER_ID_HELPER, userId ) )
                                .and( eq( Environment.ENVIRONMENT_ID_HLPER, environmentId ) );

            Statement delete = QueryBuilder.delete().from( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                           .where( eq( User.USER_ID_HELPER, userId ) )
                                           .and( eq( Environment.ENVIRONMENT_ID_HLPER, environmentId ) );

            batchStatement.add( deleteEnvsRelations );
            batchStatement.add( delete );
        }

        batchStatement.add( deleteUser );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );


        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );
    }


    public static void delete( UUID environmentId )
    {
        BatchStatement batchStatement = new BatchStatement();
        UUID userId = getOwnerId( environmentId );

        Statement deleteEnv = QueryBuilder.insertInto( Start.KEYSPACE, Environment.TABLE )
                                          .value( Environment.ENVIRONMENT_ID, environmentId );

        if ( userId != null )
        {

            Statement deleteEnvsRelations = QueryBuilder.insertInto( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                                        .value( User.USER_ID_HELPER, userId )
                                                        .value( Environment.ENVIRONMENT_ID_HLPER, environmentId );

            Statement deleteEnvsRelations1 = QueryBuilder.insertInto( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                                         .value( User.USER_ID_HELPER, userId )
                                                         .value( Environment.ENVIRONMENT_ID_HLPER, environmentId );

            batchStatement.add( deleteEnvsRelations );
            batchStatement.add( deleteEnvsRelations1 );
        }

        batchStatement.add( deleteEnv );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );


        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );

        if ( !resultSet.wasApplied() )
        {
            log.info( " delete environment was not applied! statement  =  {}", batchStatement.toString() );
        }

        deleteEnvironment( environmentId );
    }


    public static UUID getOwnerId( UUID environmentId )
    {

        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                          .where( eq( Environment.ENVIRONMENT_ID_HLPER, environmentId ) );


        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet result = Start.mappingSession.getSession().execute( statement );
        Row row = result.one();


        if ( row == null )
        {
            return null;
        }

        if ( !row.isNull( Environment.ENVIRONMENT_ACCESS_TOKEN ) )
        {
            return row.getUUID( User.USER_ID_HELPER );
        }

        return null;
    }


    public static List<Environment> findIn( List<UUID> uuids )
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, Environment.TABLE )
                                          .where( in( Environment.ENVIRONMENT_ID, uuids ) );
        statement.setConsistencyLevel( ConsistencyLevel.ALL );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );
        return Start.mappingSession.mapper( Environment.class ).map( resultSet ).all().stream()
                                   .filter( environment -> environment.getAccessToken() == null )
                                   .collect( Collectors.toList() );
    }
}
