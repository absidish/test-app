package dao;


import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import starter.Start;
import model.Environment;
import model.User;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;


public class EnvironmentDao
{
    public static final ConsistencyLevel CONSISTENCY_LEVEL = ConsistencyLevel.QUORUM;

    private static final Logger log = LoggerFactory.getLogger( EnvironmentDao.class );


    public static List<Environment> getEnvironments()
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, Environment.TABLE );
        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );
        statement.setConsistencyLevel( CONSISTENCY_LEVEL );
        return Start.mappingSession.mapper( Environment.class ).map( resultSet ).all();
    }


    public static void delete( UUID environmentId )
    {
        BatchStatement batchStatement = new BatchStatement();

        UUID userId = getOwnerId( environmentId );

        Statement deleteUser =
                QueryBuilder.delete().from( Start.KEYSPACE, Environment.TABLE ).where( eq( "id", environmentId ) );

        if ( userId != null )
        {

            Statement deleteEnvsRelations =
                    QueryBuilder.delete().from( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                .where( eq( "user_id", userId ) ).and( eq( "environment_id", environmentId ) );

            Statement delete = QueryBuilder.delete().from( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                           .where( eq( "user_id", userId ) )
                                           .and( eq( "environment_id", environmentId ) );

            batchStatement.add( deleteEnvsRelations );
            batchStatement.add( delete );
        }
        batchStatement.add( deleteUser );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );

        if ( !resultSet.wasApplied() )
        {
            log.error( " delete environment was not applied! statement  =  {}", batchStatement.toString() );
        }

    }


    public static UUID getOwnerId( UUID environmentId )
    {

        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                          .where( eq( "environment_id", environmentId ) );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );
        ResultSet result = Start.mappingSession.getSession().execute( statement );

        try
        {
            if ( result.wasApplied()  )
            {
                return result.one().getUUID( "user_id" );
            }
            else
            {
                return null;
            }
        }
        catch ( Exception e )
        {
            return null;
        }
    }


    private static void printRow( String delete, ResultSet rs )
    {
    }


    public static List<Environment> findIn( List<UUID> uuids )
    {
        Statement statement =
                QueryBuilder.select().from( Start.KEYSPACE, Environment.TABLE ).where( in( "id", uuids ) );
        statement.setConsistencyLevel( ConsistencyLevel.ALL );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );
        return Start.mappingSession.mapper( Environment.class ).map( resultSet ).all();
    }
}
