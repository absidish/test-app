package controller;


import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import cassandra.Start;
import cassandra.TestEnvironment;
import cassandra.TestUser;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;


public class EnvironmentDao
{
    public static List<TestEnvironment> getEnvironments()
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, TestEnvironment.TABLE );
        return Start.mappingSession.getByQuery( TestEnvironment.class, statement );
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

            Statement delete = QueryBuilder.delete().from( Start.KEYSPACE, TestUser.ENVIRONMENTS_BY_USER )
                                           .where( eq( "user_id", userId ) )
                                           .and( eq( "environment_id", environmentId ) );

            batchStatement.add( deleteEnvsRelations );
            batchStatement.add( delete );
        }
        batchStatement.add( deleteUser );

        Start.mappingSession.getSession().execute( batchStatement );
    }


    public static UUID getOwnerId( UUID environmentId )
    {

        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, TestEnvironment.USER_BY_ENVIRONMENT )
                                          .where( eq( "environment_id", environmentId ) );

        statement.setConsistencyLevel( ConsistencyLevel.ALL );
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


    public static  List<TestEnvironment> findIn( List<UUID> uuids )
    {
        Statement statement =
                QueryBuilder.select().from( Start.KEYSPACE, TestEnvironment.TABLE ).where( in( "id", uuids ) );
        statement.setConsistencyLevel( ConsistencyLevel.ALL );
        return Start.mappingSession.getByQuery( TestEnvironment.class, statement );

    }
}
