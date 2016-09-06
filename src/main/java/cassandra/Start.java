package cassandra;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.mapping.MappingManager;

import controller.EnvironmentDao;
import controller.UserDao;

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
        UUID userId = UUID.fromString( "9398dab7-a6b1-4745-8fee-313fde5bf0f3" );


        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                User testUser1 = new User( userId, "Sydyk1", "absidish@gmail.com", "absidih" );
                UserDao.save( testUser1 );
            }
        };

        Thread thread1 = new Thread()
        {
            @Override
            public void run()
            {
                User testUser1 = new User( userId, "Ulan", "ulan@gmail.com", "ulan" );
                UserDao.save( testUser1 );
            }
        };
        thread.start();
        thread1.start();

        User testUser1 = new User( userId,  "momoto2@gmail.com", "absidish2", "Momoto" );
        UserDao.update( testUser1 );

        //        List<User> list = UserDao.getUsers();

        //        log.info( "zise = {}", list.size() );
        //        list.forEach( testUser -> {
        //            log.info( "userId = {}", testUser.getId() );
        //        } );
        //        startSpark();
    }


    private static void startSpark()
    {
        get( "/rest/users", ( request, response ) -> {

            List<User> testUsers = UserDao.getUsers();
            List<UUID> list = new ArrayList();
            testUsers.stream().forEach( testUser -> {
                list.add( testUser.getId() );
            } );

            return list.toString();
        } );

        get( "/rest/users/create", ( request, response ) -> {

            UUID envId = UUID.randomUUID();
            UUID envId1 = UUID.randomUUID();

            TestEnvironment testEnvironment = new TestEnvironment( envId, "env1", "description1" );
            TestEnvironment testEnvironment1 = new TestEnvironment( envId1, "env2", "description2" );

            User testUser = UserDao.createUser( testEnvironment, testEnvironment1 );
            return testUser.getId().toString();
        } );

        get( "/rest/users/:id/delete", ( request, response ) -> {
            UUID userId = UUID.fromString( request.params( ":id" ) );
            UserDao.delete( userId );
            return "deleted";
        } );

        get( "/rest/users/:id/addenv", ( request, response ) -> {
            UUID envId = UUID.randomUUID();
            UUID userId = UUID.fromString( request.params( ":id" ) );
            TestEnvironment testEnvironment = new TestEnvironment( envId, "env1", "description1" );

            UserDao.addEnvironment( userId, testEnvironment );

            return "deleted";
        } );


        get( "/rest/environments", ( request, response ) -> {

            List<TestEnvironment> testEnvironments = EnvironmentDao.getEnvironments();

            List<UUID> list = new ArrayList();

            testEnvironments.stream().forEach( testEnvironment -> {
                list.add( testEnvironment.getId() );
            } );

            return list.toString();
        } );


        get( "/rest/environments/:id/delete", ( request, response ) -> {
            UUID environmentId = UUID.fromString( request.params( ":id" ) );
            EnvironmentDao.delete( environmentId );
            return "deleted";
        } );

        get( "/rest/health", ( request, response ) -> {


            Map<String, String> map = new HashMap<>();
            List<User> testUserList = UserDao.getUsers();
            List<UUID> uuids = null;

            int n = 0;

            for ( User testUser : testUserList )
            {
                uuids = UserDao.getUserEnvs( testUser.getId() );
                if ( uuids != null && !uuids.isEmpty() )
                {
                    List<TestEnvironment> testEnvironments = EnvironmentDao.findIn( uuids );

                    if ( testEnvironments != null && !testEnvironments.isEmpty() )
                    {
                        if ( uuids.size() != testEnvironments.size() )
                        {
                            map.put( testUser.getId() + "", "" + uuids.size() + "-" + testEnvironments.size() );
                        }
                    }
                }
            }

            List<TestEnvironment> envs = EnvironmentDao.getEnvironments();

            User testUser = null;
            for ( TestEnvironment testEnvironment : envs )
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

            return map.toString();
        } );
    }
}
