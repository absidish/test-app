import java.util.HashMap;
import java.util.Map;


public class Main
{

    public static Map<String, Long> COUNT = new HashMap();


    public static void main( String args[] )
    {

        String ip1 = "http://192.168.0.101:4567";
        String ip2 = "http://192.168.0.103:4567";
//        String ip3 = "http://192.168.0.104";

        //ap-southeast-1
//        RunnableDemo R1 = new RunnableDemo( "create", ip1 );
//        R1.start();

//        RunnableDemo R3 = new RunnableDemo( "addenv", ip1 );
//        R3.start();
//
//        RunnableDemo R4 = new RunnableDemo( "delenv", ip1 );
//        R4.start();
//
        RunnableDemo R2 = new RunnableDemo( "deleteUser", ip2 );
        R2.start();
//
        RunnableDemo R411 = new RunnableDemo( "delenv", ip2 );
        R411.start();

//        RunnableDemo R211 = new RunnableDemo( "deleteUser", ip2 );
//        R211.start();

//        RunnableDemo sel = new RunnableDemo( "select", ip1 );
//        sel.start();
//
//
//        //us
//        RunnableDemo R11 = new RunnableDemo( "create", ip2 );
//        R11.start();
//
////        RunnableDemo R31 = new RunnableDemo( "addenv", ip2 );
////        R31.start();
//
//        RunnableDemo R41 = new RunnableDemo( "delenv", ip2 );
//        R41.start();
//
//        RunnableDemo R21 = new RunnableDemo( "deleteUser", ip2 );
//        R21.start();
//
//        RunnableDemo R412 = new RunnableDemo( "delenv", ip2 );
//        R412.start();
//
//        RunnableDemo R212 = new RunnableDemo( "deleteUser", ip2 );
//        R212.start();
//
//        RunnableDemo sel1 = new RunnableDemo( "select", ip2 );
//        sel1.start();

//        //us1
//        RunnableDemo R5 = new RunnableDemo( "create", ip3 );
//        R5.start();
//
//        RunnableDemo R51 = new RunnableDemo( "addenv", ip3 );
//        R51.start();
//
//        RunnableDemo R61 = new RunnableDemo( "delenv", ip3 );
//        R61.start();
//
//        RunnableDemo R71 = new RunnableDemo( "deleteUser", ip3 );
//        R71.start();
//
//        RunnableDemo R81 = new RunnableDemo( "delenv", ip3 );
//        R81.start();
//
//        RunnableDemo R91 = new RunnableDemo( "deleteUser", ip3 );
//        R91.start();
//
//        RunnableDemo sel19 = new RunnableDemo( "select", ip3 );
//        sel19.start();

    }
}
