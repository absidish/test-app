package starter;


import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.ConsistencyLevel;


public class Main
{

    public static void main( String args[] )
    {
        String[] actions = { "create", "addenv", "delenv", "deleteUser", "select" };
        //                String[] actions = { "delenv", "deleteUser" };


        String[] nodes = { "http://192.168.0.101:4567", "http://192.168.0.104:4567" };


        for ( int i = 0; i < 3; i++ )
        {
            for ( String action : actions )
            {
                for ( String node : nodes )
                {
                    RunnableDemo R1 = new RunnableDemo( action, node );
                    R1.start();
                }
            }
        }
    }
}
