package cassandra;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.datastax.driver.mapping.MappingSession;

import controller.EnvironmentDao;
import controller.UserDao;

import static java.lang.String.format;

import static spark.Spark.get;


public class Start
{
    public static final String KEYSPACE = "test";
    private static CassandraConnector cassandraConnector;
    public static MappingSession mappingSession;


    public  void main( String[] args )
    {
        init();

        get( "/rest/users", ( request, response ) -> {

            List<TestUser> testUsers = UserDao.getUsers();
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

            TestUser testUser = UserDao.createUser( testEnvironment, testEnvironment1 );
            return testUser.getId().toString();
        } );

        get( "/rest/users/:id/delete", ( request, response ) -> {
            UUID userId = UUID.fromString( request.params( ":id" ) );
            UserDao.delete( userId );
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
            List<TestUser> testUserList = UserDao.getUsers();
            List<UUID> uuids = null;

            int n = 0;

            for ( TestUser testUser : testUserList )
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

            TestUser testUser = null;
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


    private static void init()
    {
        cassandraConnector = new CassandraConnector( "192.168.0.101", 9042 );
        mappingSession = new MappingSession( KEYSPACE, cassandraConnector.getSession() );
    }
}
