////////////////////////////////////////////////////////////////////////////////
//
//  FILE
//      GridAuth.java     -  edu.ucsd.auth
//
//  CLASS HIERARCHY
//      java.lang.Object
//          |
//          +-.GridAuth
//
//  PRINCIPAL AUTHOR
//      Timothy J Warnock, UCSD/NEES
//   Maintenance:
//      Lawrence J. Miller. SDSC/NEES, 050127
//
////////////////////////////////////////////////////////////////////////////////
package edu.ucsd.auth;

import java.net.*;
import javax.net.ssl.*;
import java.io.*;
import java.util.Hashtable;
import java.util.regex.*;
import java.util.Enumeration;

/**
 * Grid-based Authentication and Authorization client API
 * - fully implements the Grid-Auth API Specification.
 *<P>
 * @author      Timothy J Warnock
 */
public class GridAuth {

  //////////////////////////////////////////////////////////////////////////////
  // Fields
  //////////////////////////////////////////////////////////////////////////////
  /** * The GridAuth version. */
  public static final String VERSION = "1.0";
  
  /** A Hashtable that stores user-related values received from the service
  * handler. */
  protected Hashtable<String, String> userMapHashTable;

  /** The URL for auth service handling. */
  protected URL serviceHost;


  ////////////////////////////////////////////////////////////////////////////// 
  // GridAuth Constructor
  ////////////////////////////////////////////////////////////////////////////// 
  /**
   * This is the constructor method which simply allocates space.
   * The constructor does not communicate with the service handler in any way.
   * <P>
   */
  public GridAuth() {
    try {

      userMapHashTable = new Hashtable<String, String> ();
      serviceHost = new URL ("http://www.gridauth.com/handler.cgi");
      
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
      };
    
      // Install the all-trusting trust manager
      try {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      } catch (Exception e) {
        System.err.println(e);
      }
    
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }


  //////////////////////////////////////////////////////////////////////////////
  // Set the URL location for remote logins
  //////////////////////////////////////////////////////////////////////////////
  /**
   * Dynamically set the service handler at runtime (not recommended).
   * 
   * @param  newServiceHost host url string for the auth service handler
   */
  public void setServiceHandler (String newServiceHost) {
    try {
      serviceHost = new URL (newServiceHost);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }


  //////////////////////////////////////////////////////////////////////////////
  // Login with username/password
  //////////////////////////////////////////////////////////////////////////////
  /**
   * The login method in this form will attempt to authenticate with the default
   * GridAuth service handler (or other grid if the
   * setServiceHandler method was used) using a supplied
   * username and password.
   *
   * @param   user login username
   * @param   pass password for the user login
   * @return  true IFF login was successful, false otherwise
   */
  public boolean login (String user, String pass) {
    try {
      boolean returnValue = false;
      user = URLEncoder.encode (user, "UTF-8");
      pass = URLEncoder.encode (pass, "UTF-8");
      URLConnection conn = serviceHost.openConnection ();
      conn.setDoOutput (true);
      PrintWriter out = new PrintWriter (conn.getOutputStream ());
      String postData =
        "username=" + user +
        "&password=" + pass +
        "&service=Java GridAuth " + VERSION;
      out.print (postData);
      out.flush ();
      out.close ();

      //Read XML Response
      BufferedReader in = new BufferedReader (new InputStreamReader (conn.getInputStream () ));
      String inputLine;
      String xmlResponse = "";
      while ((inputLine = in.readLine()) != null)
        xmlResponse = xmlResponse + inputLine + "\n";
      in.close ();

      //Split XML
      Pattern keySplitter = Pattern.compile ("(?is)</key>");
      String[] splitInputLine = keySplitter.split (xmlResponse);

      //Parse XML
      String patternStr = "(?is)<key name[ ]*=[\" ]*([^>\"]+)[\" ]*>(.*)";
      Pattern pattern = Pattern.compile(patternStr);
      Matcher matcher = pattern.matcher("");
      for (int i = 0; i < splitInputLine.length; i++) {
        matcher.reset( splitInputLine[i] );
        if ( matcher.find() ) {
          returnValue = true;
          userMapHashTable.put (matcher.group(1), matcher.group(2));
        }
      }
      return returnValue;
    } catch (Throwable e) {
      e.printStackTrace ();
    }
    return true;
  }


  //////////////////////////////////////////////////////////////////////////////
  // Login with existing session (delegation)
  //////////////////////////////////////////////////////////////////////////////
  /**
   * The login method in this form will attempt to authenticate with the default
   * GridAuth test service handler
   * (or other grid if the setServiceHandler method was used) using a supplied
   * session hash. A session hash is available to all authenticated services and
   * is used to enable advanced pipelines and workflows where credentials can be
   * shared from one application to another on a time sensitive basis. This form
   * of login is necessary to support a single-sign-on environment across grid
   * services.
   *
   * @param   session hash identifying current session.
   * @return  true IFF login was successful, false otherwise
   */
  public boolean login (String session) {
    try {
      boolean returnValue = false;
      session = URLEncoder.encode (session, "UTF-8");
      URLConnection conn = serviceHost.openConnection ();
      conn.setDoOutput (true);
      PrintWriter out = new PrintWriter (conn.getOutputStream ());
      String postData =
        "session=" + session +
        "&service=Java GridAuth " + VERSION;
      out.print (postData);
      out.flush ();
      out.close ();

      //Read XML Response
      BufferedReader in = new BufferedReader (new InputStreamReader (conn.getInputStream () ));
      String inputLine;
      String xmlResponse = "";
      while ((inputLine = in.readLine()) != null)
        xmlResponse = xmlResponse + inputLine + "\n";
      in.close ();

      //Split XML
      Pattern keySplitter = Pattern.compile ("(?is)</key>");
      String[] splitInputLine = keySplitter.split (xmlResponse);

      //Parse XML
      String patternStr = "(?is)<key name[ ]*=[\" ]*([^>\"]+)[\" ]*>(.*)";
      Pattern pattern = Pattern.compile(patternStr);
      Matcher matcher = pattern.matcher("");
      for (int i = 0; i < splitInputLine.length; i++) {
        matcher.reset( splitInputLine[i] );
        if ( matcher.find() ) {
          returnValue = true;
          userMapHashTable.put (matcher.group(1), matcher.group(2));
        }
      }
      return returnValue;
    } catch (Throwable e) {
      e.printStackTrace ();
    }
    return true;
  }


  //////////////////////////////////////////////////////////////////////////////
  // Logout
  //////////////////////////////////////////////////////////////////////////////
  /**
   * The logout method destroys local information, as well as remote session
   * information. This method should be called at the end of a session, not
   * necessarily at the end of a program. That is, a program that may exist in
   * a larger pipeline should not call this function unless they deliberately
   * want to terminate the entire session.
   *
   * @return  true IFF logout was successful, false otherwise
   */
  public boolean logout() {
    try {
      String sessionid = (String)userMapHashTable.get("session");
      String session = URLEncoder.encode (sessionid, "UTF-8");
      URLConnection conn = serviceHost.openConnection ();
      conn.setDoOutput (true);
      PrintWriter out = new PrintWriter (conn.getOutputStream ());
      String postData =
        "logout=" + session +
        "&service=Java GridAuth " + VERSION;
      out.print (postData);
      out.flush ();
      out.close (); 
    } catch (Throwable e) {
      e.printStackTrace ();
    }
    return true;
  }


  //////////////////////////////////////////////////////////////////////////////
  // Generic GET method
  //////////////////////////////////////////////////////////////////////////////
  /** An access method that will query and return the value of a supplied key.
  * @return userMapHashTable value as a string.
  * @param key userMapHashTable key
  */
  public String get (String key)
  {
    if ( userMapHashTable.containsKey(key) )
      return (String)userMapHashTable.get(key);
    return "";
  }


  //////////////////////////////////////////////////////////////////////////////
  // Authorization method to determine if a user is in a particular group
  //////////////////////////////////////////////////////////////////////////////
  /**
   * A method to get the keys from the userMapHashTable.
   * @return key list as a String
   */
  public String getKeys ()
  {
    try {
      String returnString = "";
      Enumeration<String> keys = userMapHashTable.keys();
      while ( keys.hasMoreElements() ) {
        returnString = returnString + " " + keys.nextElement();
      }
      return returnString;
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return "";
  }

 
  //////////////////////////////////////////////////////////////////////////////
  // Authorization method to determine if a user is in a particular group
  //////////////////////////////////////////////////////////////////////////////
  /**
   * Authorization method to determine if a user is in a particular group.
   *
   * @return  true IFF authenticated user is a member of group
   * @param   group query group
   */
  public boolean isMember(String group) {
    try {
      String[] groups = ((String)userMapHashTable.get("groups")).split("\\s+");
      for (int i = 0; i < groups.length; i++) {
        if (groups[i].equals(group))
          return true;
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return false;
  }

}
