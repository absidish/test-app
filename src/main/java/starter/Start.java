package starter;


import java.util.ArrayList;
import java.util.Collections;
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

    private static boolean isLocked = false;


    private static void init()
    {
        cassandraConnector = new CassandraConnector( "cassandra-node", 9042 );
        mappingSession = new MappingManager( cassandraConnector.getSession() );
    }


    public static void main( String[] args )
    {
        init();

        List<User> users = createUsers();
        createEnvironments( users );

        selectUsers();
        selectEnvironments();

//        deleteFiveUsers( UserDao.getUsers() );
//        deleteFiveEnironments( EnvironmentDao.getEnvironments() );

        selectUsers();
        selectEnvironments();

        updateUsers( UserDao.getUsers() );

        selectUsers();
        //
        //        startSpark();
    }


    private static void updateUsers( final List<User> users )
    {
        int n = 0;
        for ( User user : users )
        {
            if ( n < 5 )
            {
                n++;
                user.setName( "updatedUser" );
                UserDao.update( user );
            }
            else
            {
                break;
            }
        }

        n = 0;
        for ( User user : users )
        {
            if ( n < 5 )
            {
                n++;
                user.setName( "updatedUser2" );
                log.info( "was updated = {}", UserDao.update( user ) );
            }
            else
            {
                break;
            }
        }
    }


    private static void deleteFiveEnironments( List<Environment> environments )
    {
        int n = 0;
        for ( Environment environment : environments )
        {
            if ( n < 5 )
            {
                n++;
                EnvironmentDao.delete( environment.getId() );
            }
            else
            {
                break;
            }
        }
    }


    private static void selectEnvironments()
    {
        List<Environment> environments = EnvironmentDao.getEnvironments();

        log.info( "environments size = {}", environments.size() );
        environments.forEach( environment -> {
            log.info( "environment id = {}", environment.getId() );
        } );
    }


    private static void selectUsers()
    {
        List<User> users = UserDao.getUsers();
        log.info( "users size = {}", users.size() );

        users.forEach( testUser -> {
            log.info( "userId = {}", testUser.getId() );
        } );
    }


    private static void deleteFiveUsers( List<User> users )
    {
        int n = 0;
        for ( User user : users )
        {
            if ( n < 5 )
            {
                n++;
                UserDao.delete( user.getId() );
            }
            else
            {
                break;
            }
        }
    }


    private static void createEnvironments( List<User> users )
    {
        for ( User user : users )
        {
            Environment environment = new Environment( UUID.randomUUID(), "env1", "description1" );
            UserDao.addEnvironmentToUser( user, environment );
        }
    }


    private static List<User> createUsers()
    {
        List<User> users = new ArrayList<>();
        for ( int i = 0; i < 5; i++ )
        {
            User user = new User( UUID.randomUUID(), "Sydyk", "sydyk@gmail.com", "sydyk" );
            users.add( user );
            UserDao.create( user );
        }
        return users;
    }


    private static void startSpark()
    {

        get( "/rest/users", ( request, response ) -> {

            log.info( "route = {}", "/rest/users" );
            if ( !isLocked )
            {
                List<User> testUsers = UserDao.getUsers();
                List<UUID> list = new ArrayList();
                testUsers.stream().forEach( testUser -> {
                    list.add( testUser.getId() );
                } );

                log.info( "returned users" );
                return list.toString();
            }
            else
            {
                return "locked";
            }
        } );

        get( "/rest/users/create", ( request, response ) -> {

            log.info( "route = {}", "/rest/users/create" );

            if ( !isLocked )
            {
                UUID envId = UUID.randomUUID();

                Environment testEnvironment = new Environment( envId, "env1", "description1" );
                User user = new User( UUID.randomUUID(), "Sydyk", "sydyk@gmail.com", "sydyk" );

                log.info( "user created" );

                UserDao.create( user );
                UserDao.addEnvironmentToUser( user, testEnvironment );
                return user.getId().toString();
            }
            else
            {
                return "locked";
            }
        } );

        get( "/rest/users/:id/delete", ( request, response ) -> {
            UUID userId = UUID.fromString( request.params( ":id" ) );
            log.info( "route = {}", format( "/rest/users/%s/delete", userId ) );

            if ( !isLocked )
            {
                UserDao.delete( userId );

                log.info( "user deleted" );
                return "deleted";
            }
            else
            {
                return "locked";
            }
        } );

        get( "/rest/users/:id/addenv", ( request, response ) -> {
            UUID envId = UUID.randomUUID();
            UUID userId = UUID.fromString( request.params( ":id" ) );
            log.info( "route = {}", format( "/rest/users/%s/addenv", userId ) );

            if ( !isLocked )
            {
                Environment testEnvironment = new Environment( envId, "env1", "description1" );

                UserDao.addEnvironment( userId, testEnvironment );
                log.info( "env added to user" );
                return "added";
            }
            else
            {
                return "locked";
            }
        } );


        get( "/rest/environments", ( request, response ) -> {
            log.info( "route = {}", "/rest/environments" );

            if ( !isLocked )
            {
                List<Environment> testEnvironments = EnvironmentDao.getEnvironments();

                List<UUID> list = new ArrayList();

                testEnvironments.stream().forEach( testEnvironment -> {
                    list.add( testEnvironment.getId() );
                } );

                log.info( "returned list" );
                return list.toString();
            }
            else
            {
                return "locked";
            }
        } );


        get( "/rest/environments/:id/delete", ( request, response ) -> {

            UUID environmentId = UUID.fromString( request.params( ":id" ) );
            log.info( "route = {}", format( "/rest/environments/%s/delete", environmentId ) );

            if ( !isLocked )
            {
                try
                {
                    EnvironmentDao.delete( environmentId );
                }
                catch ( Exception e )
                {
                    log.error( e.getMessage() );
                }
                log.info( "env deleted" );
                return "deleted";
            }
            else
            {
                return "locked";
            }
        } );

        get( "/rest/health", ( request, response ) -> {


            Map<String, String> map = new HashMap<>();

            log.info( "route = {}", "/rest/health" );

            if ( !isLocked )
            {
                isLocked = true;
                try
                {
                    log.info( "Locked" );

                    List<User> testUserList = UserDao.getUsers();
                    List<UUID> uuids = null;

                    for ( User testUser : testUserList )
                    {
                        uuids = UserDao.getUserEnvironments( testUser.getId() );
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
                }
                catch ( Exception e )
                {
                    isLocked = false;
                    log.info( "ERROR {}", e.getMessage() );
                }
                isLocked = false;
                return map.toString();
            }
            else
            {
                return "locked";
            }
        } );
    }
}
