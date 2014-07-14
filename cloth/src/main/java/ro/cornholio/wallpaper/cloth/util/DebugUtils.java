package ro.cornholio.wallpaper.cloth.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

/**
 * Created by rares on 7/11/2014.
 */
public class DebugUtils {

    private static final X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");

    public static boolean isDebuggable(Context ctx)
    {
        boolean debuggable = false;

        try
        {
            PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature signatures[] = pinfo.signatures;

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for ( int i = 0; i < signatures.length;i++)
            {
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
                if (debuggable)
                    break;
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            //debuggable variable will remain false
        }
        catch (CertificateException e)
        {
            //debuggable variable will remain false
        }
        return debuggable;
    }
}
