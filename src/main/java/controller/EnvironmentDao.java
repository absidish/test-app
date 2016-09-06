package controller;


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

import cassandra.Start;
import cassandra.TestEnvironment;
import cassandra.User;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;


public class EnvironmentDao
{
    private static final Logger log = LoggerFactory.getLogger( EnvironmentDao.class );


    public static List<TestEnvironment> getEnvironments()
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, TestEnvironment.TABLE );
        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );
        statement.setConsistencyLevel( ConsistencyLevel.SERIAL );
        return Start.mappingSession.mapper( TestEnvironment.class ).map( resultSet ).all();
    }


    public static void delete( UUID environmentId )
    {
        BatchStatement batchStatement = new BatchStatement();

        UUID userId = getOwnerId( environmentId );

        Statement deleteUser =
                QueryBuilder.delete().from( Start.KEYSPACE, TestEnvironment.TABLE ).where( eq( "id", environmentId ) );

        if ( userId != null )
        {

            Statement deleteEnvsRelations =
                    QueryBuilder.delete().from( Start.KEYSPACE, TestEnvironment.USER_BY_ENVIRONMENT )
                                .where( eq( "user_id", userId ) ).and( eq( "environment_id", environmentId ) );

            Statement delete = QueryBuilder.delete().from( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                           .where( eq( "user_id", userId ) )
                                           .and( eq( "environment_id", environmentId ) );

            batchStatement.add( deleteEnvsRelations );
            batchStatement.add( delete );
        }
        batchStatement.add( deleteUser );

        batchStatement.setConsistencyLevel( ConsistencyLevel.SERIAL );
        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );
        printRow( "delete", resultSet );
    }


    public static UUID getOwnerId( UUID environmentId )
    {

        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, TestEnvironment.USER_BY_ENVIRONMENT )
                                          .where( eq( "environment_id", environmentId ) );

        statement.setConsistencyLevel( ConsistencyLevel.SERIAL );
        ResultSetFuture result = Start.mappingSession.getSession().executeAsync( statement );

        try
        {
            if ( result.get().getAvailableWithoutFetching() > 0 )
            {
                return result.get().one().getUUID( "user_id" );
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
//        if ( rs != null )
//        {
//            System.out.println( rs.isExhausted() );
//        }
//
//        try
//        {
//            System.out.println( rs.one().getBool( "applied" ) + "   " + delete );
//        }
//        catch ( Exception e )
//        {
//            log.error( e.getMessage() );
//        }
    }


    public static List<TestEnvironment> findIn( List<UUID> uuids )
    {
        Statement statement =
                QueryBuilder.select().from( Start.KEYSPACE, TestEnvironment.TABLE ).where( in( "id", uuids ) );
        statement.setConsistencyLevel( ConsistencyLevel.ALL );

        statement.setConsistencyLevel( ConsistencyLevel.SERIAL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );
        return Start.mappingSession.mapper( TestEnvironment.class ).map( resultSet ).all();
    }
}
