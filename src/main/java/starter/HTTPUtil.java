package starter;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HTTPUtil
{
    private static final Logger log = LoggerFactory.getLogger( HTTPUtil.class );

    private static final String USER_AGENT = "Mozilla/5.0";
    public static int count = 0;



    public static String get( String url ) throws Exception
    {

        if ( url.contains( "lock" ) )
        {
            log.info( "locked url =" + url );

            return "";
        }
        count++;
        SSLUtil.disableCertificateValidation();
        log.info( "url =" + url );
        URL obj = new URL( url );

        HttpURLConnection con = ( HttpURLConnection ) obj.openConnection();

        // optional default is GET
        con.setRequestMethod( "GET" );

        //add request header
        con.setRequestProperty( "User-Agent", USER_AGENT );

        //        int responseCode = con.getResponseCode();
        //        System.out.println( "\nSending 'GET' request to URL : " + url );
        //        System.out.println( "Response Code : " + responseCode );

        BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ( ( inputLine = in.readLine() ) != null )
        {
            response.append( inputLine );
        }
        in.close();

        //print result
        return response.toString();
    }
}
