import java.util.HashMap;
import java.util.Map;


public class Main
{

    public static Map<String, Long> COUNT = new HashMap();


    public static void main( String args[] )
    {
//                String[] actions = { "create", "delenv", "deleteUser",  "select" , "addenv"};
        String[] actions = { "delenv", "deleteUser" };


        String[] nodes = { "http://192.168.0.101:4567", "http://192.168.0.104:4567" };


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
