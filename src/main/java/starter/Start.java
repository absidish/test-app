package starter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.mapping.MappingManager;

import cassandra.CassandraConnector;
import dao.EnvironmentDao;
import dao.UserDao;
import model.Environment;
import model.User;

import static java.lang.String.format;

import static spark.Spark.get;


public class Start
{
    private static final Logger log = LoggerFactory.getLogger( Start.class );
    public static final String KEYSPACE = "hub";
    private static CassandraConnector cassandraConnector;
    public static MappingManager mappingSession;


    private static void init()
    {
        cassandraConnector = new CassandraConnector( "cassandra-node", 9042 );
        mappingSession = new MappingManager( cassandraConnector.getSession() );
    }


    public static void main( String[] args )
    {
        init();

        List<User> list = UserDao.getUsers();

        log.info( "zise = {}", list.size() );
        list.forEach( testUser -> {
            log.info( "userId = {}", testUser.getId() );
        } );
        startSpark();
    }


    private static void startSpark()
    {
        get( "/rest/users", ( request, response ) -> {

            log.info( "route = {}", "/rest/users" );
            List<User> testUsers = UserDao.getUsers();
            List<UUID> list = new ArrayList();
            testUsers.stream().forEach( testUser -> {
                list.add( testUser.getId() );
            } );

            log.info( "returned users" );
            return list.toString();
        } );

        get( "/rest/users/create", ( request, response ) -> {
            log.info( "route = {}", "/rest/users/create" );

            UUID envId = UUID.randomUUID();
            UUID envId1 = UUID.randomUUID();

            Environment testEnvironment = new Environment( envId, "env1", "description1" );
            Environment testEnvironment1 = new Environment( envId1, "env2", "description2" );

            User testUser = UserDao.createUser( testEnvironment, testEnvironment1 );
            log.info( "user created" );
            return testUser.getId().toString();
        } );

        get( "/rest/users/:id/delete", ( request, response ) -> {
            UUID userId = UUID.fromString( request.params( ":id" ) );
            log.info( "route = {}", format( "/rest/users/%s/delete", userId ) );
            UserDao.delete( userId );
            log.info( "user deleted" );
            return "deleted";
        } );

        get( "/rest/users/:id/addenv", ( request, response ) -> {
            UUID envId = UUID.randomUUID();
            UUID userId = UUID.fromString( request.params( ":id" ) );
            log.info( "route = {}", format( "/rest/users/%s/addenv", userId ) );
            Environment testEnvironment = new Environment( envId, "env1", "description1" );

            UserDao.addEnvironment( userId, testEnvironment );
            log.info("env added to user");
            return "added";
        } );


        get( "/rest/environments", ( request, response ) -> {
            log.info( "route = {}", "/rest/environments");
            List<Environment> testEnvironments = EnvironmentDao.getEnvironments();

            List<UUID> list = new ArrayList();

            testEnvironments.stream().forEach( testEnvironment -> {
                list.add( testEnvironment.getId() );
            } );

            log.info( "returned list" );
            return list.toString();
        } );


        get( "/rest/environments/:id/delete", ( request, response ) -> {

            UUID environmentId = UUID.fromString( request.params( ":id" ) );
            log.info( "route = {}", format( "/rest/environments/%s/delete", environmentId ) );

            EnvironmentDao.delete( environmentId );

            log.info( "env deleted" );
            return "deleted";
        } );

        get( "/rest/health", ( request, response ) -> {

            log.info( "route = {}", "/rest/health");


                    Map<String, String> map = new HashMap<>();
            List<User> testUserList = UserDao.getUsers();
            List<UUID> uuids = null;

            int n = 0;

            for ( User testUser : testUserList )
            {
                uuids = UserDao.getUserEnvs( testUser.getId() );
                if ( uuids != null && !uuids.isEmpty() )
                {
                    List<Environment> testEnvironments = EnvironmentDao.findIn( uuids );

                    if ( testEnvironments != null && !testEnvironments.isEmpty() )
                    {
                        if ( uuids.size() != testEnvironments.size() )
                        {
                            map.put( testUser.getId() + "", "" + uuids.size() + "-" + testEnvironments.size() );
                        }
                    }
                }
            }

            List<Environment> envs = EnvironmentDao.getEnvironments();

            User testUser = null;
            for ( Environment testEnvironment : envs )
            {
                UUID userId = EnvironmentDao.getOwnerId( testEnvironment.getId() );

                if ( userId != null )
                {
                    testUser = UserDao.find( userId );
                }

                if ( testUser == null )
                {
                    map.put( testEnvironment.getId().toString(),
                            format( "owner not found envId=%s, ownerId=%s", testEnvironment.getId(), userId ) );
                }
            }


            log.info( " health status ={} ", map.isEmpty() );
            log.info( "map = {} ", map.toString() );

            return map.toString();
        } );


    }
}