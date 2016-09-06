package dao;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import starter.Start;
import model.Environment;
import model.User;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;


public class UserDao
{
    public static final ConsistencyLevel CONSISTENCY_LEVEL = ConsistencyLevel.ALL;

    private static final Logger log = LoggerFactory.getLogger( UserDao.class );


    public static List<User> getUsers()
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, User.TABLE );
        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );
        return Start.mappingSession.mapper( User.class ).map( resultSet ).all();
    }


    public static User createUser( Environment... testEnvironments )
    {
        BatchStatement batchStatement = new BatchStatement();

        UUID useId = UUID.randomUUID();
        User testUser = new User( useId, "Sydyk", "absidish@gmail.com", "absidih" );

        for ( Environment testEnvironment : testEnvironments )
        {
            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.TABLE )
                                            .value( "id", testEnvironment.getId() )
                                            .value( "name", testEnvironment.getName() )
                                            .value( "description", testEnvironment.getDescription() ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                            .value( "user_id", testUser.getId() )
                                            .value( "environment_id", testEnvironment.getId() ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                            .value( "user_id", testUser.getId() )
                                            .value( "environment_id", testEnvironment.getId() ) );
        }

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, User.TABLE ).value( "id", testUser.getId() )
                                        .value( "login", testUser.getLogin() ).value( "email", testUser.getEmail() )
                                        .value( "name", testUser.getName() ) );


        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );

        return testUser;
    }


    public static Object delete( UUID userId )
    {
        BatchStatement batchStatement = new BatchStatement();

        List<UUID> envsId = getUserEnvs( userId );

        Statement deleteEnvsRelations = QueryBuilder.delete().from( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                                    .where( in( "environment_id", envsId ) );
        Statement deleteEnvs =
                QueryBuilder.delete().from( Start.KEYSPACE, Environment.TABLE ).where( in( "id", envsId ) );

        Statement deleteEnvsByUser = QueryBuilder.delete().from( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                                 .where( eq( "user_id", userId ) );

        Statement deleteUser = QueryBuilder.delete().from( Start.KEYSPACE, User.TABLE ).where( eq( "id", userId ) );


        batchStatement.add( deleteEnvsRelations );
        batchStatement.add( deleteEnvs );
        batchStatement.add( deleteEnvsByUser );
        batchStatement.add( deleteUser );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );

        if ( !resultSet.wasApplied() )
        {
            log.error( " delete user was not applied! statement  =  {}", batchStatement.toString() );
        }

        return resultSet;
    }


    private static void printRow( String delete, ResultSet rs )
    {
        //        if ( rs != null )
        //        {
        //            System.out.println( rs.isExhausted() );
        //        }
        //        try
        //        {
        //            System.out.println( rs.one().getBool( "applied" ) + "   " + delete );
        //        }
        //        catch ( Exception e )
        //        {
        //            System.out.println( "Error : " + e.getMessage() );
        //        }
    }


    public static List<UUID> getUserEnvs( UUID userId )
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                          .where( eq( "user_id", userId ) );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet result = null;
        result = Start.mappingSession.getSession().execute( statement );

        List<UUID> envsId = new ArrayList<>();
        for ( Row row : result )
        {
            envsId.add( row.getUUID( "environment_id" ) );
        }

        return envsId;
    }


    public static User find( UUID userId )
    {
        return Start.mappingSession.mapper( User.class ).get( userId );
    }


    public static void addEnvironment( UUID userId, Environment testEnvironment )
    {
        BatchStatement batchStatement = new BatchStatement();

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.TABLE )
                                        .value( "id", testEnvironment.getId() )
                                        .value( "name", testEnvironment.getName() )
                                        .value( "description", testEnvironment.getDescription() ) );

        batchStatement
                .add( QueryBuilder.insertInto( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER ).value( "user_id", userId )
                                  .value( "environment_id", testEnvironment.getId() ) );

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                        .value( "user_id", userId )
                                        .value( "environment_id", testEnvironment.getId() ) );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );

        Start.mappingSession.getSession().execute( batchStatement );
    }


    public static void save( User testUser )
    {
        Statement statement = QueryBuilder.insertInto( Start.KEYSPACE, User.TABLE ).value( "id", testUser.getId() )
                                          .value( "login", testUser.getLogin() ).value( "email", testUser.getEmail() )
                                          .value( "name", testUser.getName() );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );
        Start.mappingSession.getSession().execute( statement );
    }


    public static boolean update( User user )
    {

        Statement statement = QueryBuilder.update( Start.KEYSPACE, User.TABLE ).where( eq( "id", user.getId() ) )
                                          .with( set( "login", user.getLogin() ) )
                                          .and( set( "email", user.getEmail() ) ).and( set( "name", user.getName() ) )
                                          .and( set( "access_token", UUID.randomUUID() ) )
                                          .onlyIf( eq( "access_token", user.getAccessToken() ) );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );

        return resultSet.wasApplied();
    }
}
