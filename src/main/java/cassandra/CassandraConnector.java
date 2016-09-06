package cassandra;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;


public class CassandraConnector
{
    private static final Logger LOG = LoggerFactory.getLogger( CassandraConnector.class );

    private static final int HIGHER_TIMEOUT = 30000;

    private static final int CONNECTION_TRY = 3;

    private static final int CONNECTION_TIMEOUT = 10000;


    private final Object syncObject = new Object();

    private Cluster.Builder clusterBuilder;
    private Cluster cluster;
    private Session session;


    public CassandraConnector( String node, int port )
    {
        clusterBuilder =
                Cluster.builder().addContactPoints( node ).withCredentials( "hub_user","m01bu1ak" ).withPort( port );

        for ( int i = 0; i < CONNECTION_TRY; i++ )
        {
            cluster = buildConnection();
            if ( cluster == null )
            {
                LOG.info( "Try to connect cassandra: " + i );
                try
                {
                    Thread.sleep( CONNECTION_TIMEOUT );
                }
                catch ( InterruptedException ex )
                {
                    LOG.error( ex.getMessage() );
                }
            }
            else
            {
                //after success connection to cassandra, have to break to out from cycle
                break;
            }
        }

        if ( cluster == null )
        {
            throw new IllegalStateException( "Cluster is null" );
        }

        session = cluster.connect();
    }


    public Session get()
    {
        return getSession();
    }


    private Cluster buildConnection()
    {
        try
        {
            Cluster newCluster = clusterBuilder.build();

            Metadata metadata = newCluster.getMetadata();

            LOG.info( String.format( "Connected to cluster: %s%n", metadata.getClusterName() ) );

            for ( Host host : metadata.getAllHosts() )
            {
                LOG.info(
                        String.format( "Datacenter: %s; Host: %s; Rack: %s%n", host.getDatacenter(), host.getAddress(),
                                host.getRack() ) );
            }

            newCluster.getConfiguration().getSocketOptions().setReadTimeoutMillis( HIGHER_TIMEOUT );

            return newCluster;
        }
        catch ( Exception ex )
        {
            LOG.error( String.format( "Could not connect to cassandra host: %s", ex ) );
        }

        return null;
    }


    public Session getSession()
    {
        if ( session != null && !session.isClosed() )
        {
            return session;
        }

        if ( cluster == null )
        {
            synchronized ( syncObject )
            {
                cluster = buildConnection();
                session = cluster.connect();
            }
        }

        return session;
    }


    public void close()
    {
        if ( session != null && !session.isClosed() )
        {
            session.close();
        }
        if ( cluster != null )
        {
            cluster.close();
        }
    }
}

