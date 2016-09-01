package controller;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import cassandra.Start;
import cassandra.TestEnvironment;
import cassandra.TestUser;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;


public class UserDao
{

    public static List<TestUser> getUsers()
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, TestUser.TABLE );
        return Start.mappingSession.getByQuery( TestUser.class, statement );
    }


    public static TestUser createUser( TestEnvironment... testEnvironments )
    {
        BatchStatement batchStatement = new BatchStatement();
        UUID useId = UUID.randomUUID();
        TestUser testUser = new TestUser( useId, "Sydyk", "absidish@gmail.com", "absidih" );

        for ( TestEnvironment testEnvironment : testEnvironments )
        {
            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, TestEnvironment.TABLE )
                                            .value( "id", testEnvironment.getId() )
                                            .value( "name", testEnvironment.getName() )
                                            .value( "description", testEnvironment.getDescription() ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, TestUser.ENVIRONMENTS_BY_USER )
                                            .value( "user_id", testUser.getId() )
                                            .value( "environment_id", testEnvironment.getId() ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, TestEnvironment.USER_BY_ENVIRONMENT )
                                            .value( "user_id", testUser.getId() )
                                            .value( "environment_id", testEnvironment.getId() ) );
        }

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, TestUser.TABLE ).value( "id", testUser.getId() )
                                        .value( "login", testUser.getLogin() )
                                        .value( "email", testUser.getEmail() )
                                        .value( "name", testUser.getName()
                                                 ) );



        Start.mappingSession.getSession().execute( batchStatement );
        return testUser;
    }


    public static Object delete( UUID userId )
    {
        BatchStatement batchStatement = new BatchStatement();

        List<UUID> envsId = getUserEnvs( userId );

        Statement deleteEnvsRelations =
                QueryBuilder.delete().from( Start.KEYSPACE, TestEnvironment.USER_BY_ENVIRONMENT )
                            .where( in( "environment_id", envsId ) );
        Statement deleteEnvs =
                QueryBuilder.delete().from( Start.KEYSPACE, TestEnvironment.TABLE ).where( in( "id", envsId ) );

        Statement deleteEnvsByUser = QueryBuilder.delete().from( Start.KEYSPACE, TestUser.ENVIRONMENTS_BY_USER )
                                                 .where( eq( "user_id", userId ) );

        Statement deleteUser = QueryBuilder.delete().from( Start.KEYSPACE, TestUser.TABLE ).where( eq( "id", userId ) );


        batchStatement.add( deleteEnvsRelations );
        batchStatement.add( deleteEnvs );
        batchStatement.add( deleteEnvsByUser );
        batchStatement.add( deleteUser );

        return Start.mappingSession.getSession().execute( batchStatement );
    }


    public static List<UUID> getUserEnvs( UUID userId )
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, TestUser.ENVIRONMENTS_BY_USER )
                                          .where( eq( "user_id", userId ) );

        statement.setConsistencyLevel( ConsistencyLevel.ALL );

        ResultSet result = null;
        result = Start.mappingSession.getSession().execute( statement );

        List<UUID> envsId = new ArrayList<>();
        for ( Row row : result )
        {
            envsId.add( row.getUUID( "environment_id" ) );
        }
        return envsId;
    }


    public static TestUser find( UUID userId )
    {
        return Start.mappingSession.get( TestUser.class, userId );
    }
}
