import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class SSLUtil
{
//    private static final Logger LOG = LoggerFactory.getLogger( SSLUtil.class );


    public static void disableCertificateValidation()
    {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager()
                {
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return new X509Certificate[0];
                    }


                    public void checkClientTrusted( X509Certificate[] certs, String authType )
                    {
                    }


                    public void checkServerTrusted( X509Certificate[] certs, String authType )
                    {
                    }
                }
        };


        HostnameVerifier hv = new HostnameVerifier()
        {
            public boolean verify( String hostname, SSLSession session )
            {
                return true;
            }
        };


        try
        {
            SSLContext sc = SSLContext.getInstance( "SSL" );
            sc.init( null, trustAllCerts, new SecureRandom() );
            HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
            HttpsURLConnection.setDefaultHostnameVerifier( hv );
        }
        catch ( Exception e )
        {
//            LOG.error( e.getMessage() );
        }
    }
}
