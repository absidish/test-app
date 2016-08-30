public class Main
{
    public static void main( String args[] )
    {
        //ap-southeast-1
        RunnableDemo R1 = new RunnableDemo( "create", "https://52.76.249.178" );
        R1.start();

        RunnableDemo R3 = new RunnableDemo( "addenv", "https://52.76.249.178" );
        R3.start();

        RunnableDemo R4 = new RunnableDemo( "delenv", "https://52.76.249.178" );
        R4.start();

        RunnableDemo R2 = new RunnableDemo( "deleteUser", "https://52.76.249.178" );
        R2.start();


        RunnableDemo R411 = new RunnableDemo( "delenv", "https://52.76.249.178" );
        R411.start();

        RunnableDemo R211 = new RunnableDemo( "deleteUser", "https://52.76.249.178" );
        R211.start();


        //us
        RunnableDemo R11 = new RunnableDemo( "create", "https://52.9.99.197" );
        R11.start();

        RunnableDemo R31 = new RunnableDemo( "addenv", "https://52.9.99.197" );
        R31.start();

        RunnableDemo R41 = new RunnableDemo( "delenv", "https://52.9.99.197" );
        R41.start();

        RunnableDemo R21 = new RunnableDemo( "deleteUser", "https://52.9.99.197" );
        R21.start();

        RunnableDemo R412 = new RunnableDemo( "delenv", "https://52.9.99.197" );
        R412.start();

        RunnableDemo R212 = new RunnableDemo( "deleteUser", "https://52.9.99.197" );
        R212.start();
    }
}
