import static java.lang.String.format;


class RunnableDemo implements Runnable
{

    private String DNS = "";
    private Thread t;
    private String threadName;


    RunnableDemo( String name, String dns )
    {
        threadName = name;
        this.DNS = dns;
        //        System.out.println( "Creating " + threadName );
    }


    public void run()
    {
        try
        {
            for ( int i = 0; i < 100; i++ )
            {

                if ( threadName.equals( "create" ) )
                {
                    createUserWithEnvs();
                }
                else if ( threadName.equals( "deleteUser" ) )
                {
                    deleteUserWithEnv();
                }
                else if ( threadName.equals( "addenv" ) )
                {
                    addEnvToUser();
                }
                else if ( threadName.equals( "delenv" ) )
                {
                    deleteEnvFromUser();
                }
                else if ( threadName.equals( "select" ) )
                {
                    select();
                }
                Thread.sleep( 2000 );
            }
        }
        catch ( InterruptedException e )
        {
            //            System.out.println( "Thread " + threadName + " interrupted." );
        }
        System.out.println( "requests = " + HTTPUtil.count );
        System.out.println( "Thread " + DNS + " exiting. " + threadName );
    }


    private void select()
    {
        String url = "%s/rest/select";
        String res = "";
        try
        {
            res = HTTPUtil.get( format( url, DNS ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        if ( res.length() > 5 )
        {
            System.out.println( "---------------------------" );
            System.out.println( res );
        }
    }


    private void deleteUserWithEnv()
    {
        String u = "";
        HTTPUtil HTTPUtil = new HTTPUtil();
        String url = "%s/rest/v5/users";

        try
        {
            u = HTTPUtil.get( format( url, DNS ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        u = u.replace( "[", "" );
        u = u.replace( "]", "" );

        String[] uu = u.split( "," );

        int userIndex = ( int ) ( 0 + ( Math.random() * ( uu.length - 0 ) ) );

        String urlDel = "%s/rest/delete/%s";

        try
        {
            HTTPUtil.get( format( urlDel, DNS, uu[userIndex].trim() ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    private void deleteEnvFromUser()
    {

        String u = "";
        HTTPUtil HTTPUtil = new HTTPUtil();
        String envsurl = "%s/rest/v5/environments";

        try
        {
            u = HTTPUtil.get( format( envsurl, DNS ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        u = u.replace( "[", "" );
        u = u.replace( "]", "" );

        String[] uu = u.split( "," );
        String url = "%s/rest/env/delete/%s";
        int userId = ( int ) ( 0 + ( Math.random() * ( uu.length - 0 ) ) );

        try
        {
            HTTPUtil.get( format( url, DNS, uu[userId].trim() ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    private void addEnvToUser()
    {

        String u = "";
        HTTPUtil HTTPUtil = new HTTPUtil();
        String getUrl = "%s/rest/v5/users";

        try
        {
            u = HTTPUtil.get( format( getUrl, DNS ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        u = u.replace( "[", "" );
        u = u.replace( "]", "" );

        String[] uu = u.split( "," );

        int userIndex = ( int ) ( 0 + ( Math.random() * ( uu.length - 0 ) ) );


        String url = "%s/rest/add/%s";

        if ( uu.length > 1 )
        {
            try
            {
                HTTPUtil.get( format( url, DNS, uu[userIndex].trim() ) );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }


    private void createUserWithEnvs()
    {
        String url = "%s/rest/users/%s";
        HTTPUtil HTTPUtil = new HTTPUtil();

        try
        {
            HTTPUtil.get( format( url, DNS, "create" ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    public void start()
    {
        //        System.out.println( "Starting " + threadName );
        if ( t == null )
        {
            t = new Thread( this, threadName );
            t.start();
        }
    }
}