package dao;


import java.util.ArrayList;
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

        return Start.mappingSession.mapper( User.class ).map( resultSet ).all().stream()
                                   .filter( user -> user.getAccessToken() != null ).collect( Collectors.toList() );
    }


    public static User addEnvironmentToUser( User user, Environment... testEnvironments )
    {
        BatchStatement batchStatement = new BatchStatement();


        for ( Environment testEnvironment : testEnvironments )
        {
            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.TABLE )
                                            .value( Environment.ENVIRONMENT_ID, testEnvironment.getId() )
                                            .value( Environment.ENVIRONMENT_NAME, testEnvironment.getName() )
                                            .value( Environment.ENVIRONMENT_DESCRIPTION,
                                                    testEnvironment.getDescription() )
                                            .value( Environment.ENVIRONMENT_ACCESS_TOKEN, UUID.randomUUID() ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                            .value( User.USER_ID_HELPER, user.getId() )
                                            .value( Environment.ENVIRONMENT_ID_HLPER, testEnvironment.getId() ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                            .value( User.USER_ID_HELPER, user.getId() )
                                            .value( Environment.ENVIRONMENT_ID_HLPER, testEnvironment.getId() ) );
        }

        //        batchStatement
        //                .add( QueryBuilder.insertInto( Start.KEYSPACE, User.TABLE ).value( User.USER_ID, user.getId
        // () )
        //                                  .value( User.USER_LOGIN, user.getLogin() )
        //                                  .value( User.USER_EMAIL, user.getEmail() )
        //                                  .value( User.USER_NAME, user.getName() ) );


        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );

        //        log.info( batchStatement.getStatements().toString() );

        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );

        return user;
    }


    private static void deleteUser( UUID userId )
    {
        BatchStatement batchStatement = new BatchStatement();

        List<UUID> envsId = getUserEnvironments( userId );

        Statement deleteEnvsRelations = QueryBuilder.delete().from( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                                    .where( in( Environment.ENVIRONMENT_ID_HLPER, envsId ) );

        Statement deleteEnvs = QueryBuilder.delete().from( Start.KEYSPACE, Environment.TABLE )
                                           .where( in( Environment.ENVIRONMENT_ID, envsId ) );

        Statement deleteEnvsByUser = QueryBuilder.delete().from( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                                 .where( eq( User.USER_ID_HELPER, userId ) );

        Statement deleteUser =
                QueryBuilder.delete().from( Start.KEYSPACE, User.TABLE ).where( eq( User.USER_ID, userId ) );


        batchStatement.add( deleteEnvsRelations );
        batchStatement.add( deleteEnvs );
        batchStatement.add( deleteEnvsByUser );
        batchStatement.add( deleteUser );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );

        if ( !resultSet.wasApplied() )
        {
            log.info( "Delete user was not applied! statement  =  {}", batchStatement.toString() );
        }
    }


    public static ResultSet delete( UUID userId )
    {
        BatchStatement batchStatement = new BatchStatement();

        List<UUID> envsId = getUserEnvironments( userId );

        for ( UUID envId : envsId )
        {
            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.TABLE )
                                            .value( Environment.ENVIRONMENT_ID, envId )
                                            .value( Environment.ENVIRONMENT_ACCESS_TOKEN, null ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                            .value( User.USER_ID_HELPER, userId )
                                            .value( Environment.ENVIRONMENT_ACCESS_TOKEN, null )
                                            .value( Environment.ENVIRONMENT_ID_HLPER, envId ) );

            batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                            .value( User.USER_ID_HELPER, userId )
                                            .value( Environment.ENVIRONMENT_ACCESS_TOKEN, null )
                                            .value( Environment.ENVIRONMENT_ID_HLPER, envId ) );
        }

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, User.TABLE ).value( User.USER_ID, userId )
                                        .value( Environment.ENVIRONMENT_ACCESS_TOKEN, null ) );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );
        ResultSet resultSet = Start.mappingSession.getSession().execute( batchStatement );

        log.info( "WasApplied = {}", resultSet.wasApplied() );

        deleteUser( userId );

        return resultSet;
    }


    public static List<UUID> getUserEnvironments( UUID userId )
    {
        Statement statement = QueryBuilder.select().from( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                          .where( eq( User.USER_ID_HELPER, userId ) );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet result = null;
        result = Start.mappingSession.getSession().execute( statement );

        List<UUID> envsId = new ArrayList<>();
        for ( Row row : result )
        {
            if ( !row.isNull( Environment.ENVIRONMENT_ACCESS_TOKEN ) )
            {
                envsId.add( row.getUUID( Environment.ENVIRONMENT_ID ) );
            }
        }

        return envsId;
    }


    public static User find( UUID userId )
    {
        User user = Start.mappingSession.mapper( User.class ).get( userId );
        if ( user != null && user.getAccessToken() != null )
        {
            return user;
        }
        else
        {
            return null;
        }
    }


    public static void addEnvironment( UUID userId, Environment testEnvironment )
    {
        BatchStatement batchStatement = new BatchStatement();

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.TABLE )
                                        .value( Environment.ENVIRONMENT_ID, testEnvironment.getId() )
                                        .value( Environment.ENVIRONMENT_NAME, testEnvironment.getName() )
                                        .value( Environment.ENVIRONMENT_DESCRIPTION,
                                                testEnvironment.getDescription() ) );

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, User.ENVIRONMENTS_BY_USER )
                                        .value( User.USER_ID_HELPER, userId )
                                        .value( Environment.ENVIRONMENT_ID_HLPER, testEnvironment.getId() ) );

        batchStatement.add( QueryBuilder.insertInto( Start.KEYSPACE, Environment.USER_BY_ENVIRONMENT )
                                        .value( User.USER_ID_HELPER, userId )
                                        .value( Environment.ENVIRONMENT_ID_HLPER, testEnvironment.getId() ) );

        batchStatement.setConsistencyLevel( CONSISTENCY_LEVEL );

        Start.mappingSession.getSession().execute( batchStatement );
    }


    public static void create( User user )
    {
        Statement statement = QueryBuilder.insertInto( Start.KEYSPACE, User.TABLE ).value( User.USER_ID, user.getId() )
                                          .value( User.USER_LOGIN, user.getLogin() )
                                          .value( User.USER_EMAIL, user.getEmail() )
                                          .value( User.USER_NAME, user.getName() )
                                          .value( User.USER_ACCESS_TOKEN, UUID.randomUUID() ).ifNotExists();

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );
        Start.mappingSession.getSession().execute( statement );
    }


    public static boolean update( User user )
    {

        Statement statement =
                QueryBuilder.update( Start.KEYSPACE, User.TABLE ).where( eq( User.USER_ID, user.getId() ) )
                            .with( set( User.USER_LOGIN, user.getLogin() ) )
                            .and( set( User.USER_EMAIL, user.getEmail() ) ).and( set( User.USER_NAME, user.getName() ) )
                            .and( set( User.USER_ACCESS_TOKEN, UUID.randomUUID() ) )
                            .onlyIf( eq( User.USER_ACCESS_TOKEN, user.getAccessToken() ) );

        statement.setConsistencyLevel( CONSISTENCY_LEVEL );

        ResultSet resultSet = Start.mappingSession.getSession().execute( statement );

        return resultSet.wasApplied();
    }
}
