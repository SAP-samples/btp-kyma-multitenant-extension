package dev.kyma.samples.easyfranchise.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.communication.ConnectionParameter.AuthorizationType;
import dev.kyma.samples.easyfranchise.communication.ConnectionParameter.RequestMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * Connect to outside rest services and return string result
 *
 */
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    public final static int SIZE_RECEIVE_MAX = 1024 * 1024 * 500; // 500 MB, anything larger will be considered DoS attack
    public final static int SIZE_RECEIVE_INFO = 1024 * 1024 * 10; // notify with log msg after that amount
    final static int BUFFER_SIZE = 16 * 1024; // 16k buffer for reading

    /**
     * Make the call. All communication exceptions handled internally.
     * 
     * @param param set input parameters like user and url. Read result string and
     *              return code
     */
    public static void call(ConnectionParameter param) {
        HttpURLConnection conn = null;
        int curBufferLengthUsed = 0; // holds length of received bytes - used in exception logging
        int infoCount = 0; // count parts of size SIZE_RECEIVE_INFO received
        try {
            URL url = new URL(param.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(param.getRequestMethod().toString());
            if (param.authorizationType == AuthorizationType.Basic) {
                // check for empty password to avoid locking of user because of missing
                // configuration
                if (param.pass == null || "".equals(param.pass)) {
                    throw new IllegalArgumentException("Non empty password is requred for Basic Authentication");
                }
                String basicAuth = param.user + ":" + param.pass;
                basicAuth = "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes());
                conn.setRequestProperty(HttpHeaders.AUTHORIZATION, basicAuth);
            } else if (param.authorizationType == AuthorizationType.BearerToken) {
                // check for empty password to avoid locking of user because of missing
                // configuration
                if (param.token == null || "".equals(param.token)) {
                    throw new IllegalArgumentException("No token provided");                    
                }
                conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + param.token);
            } else {
                // we allow no-auth connections
                // throw new OperationNotSupportedException("invalid authorization");
            }
            if (param.isAcceptJsonHeader()) {
                conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }
            conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            conn.setRequestProperty("charset", "utf-8");
            byte[] postData = null;
            int postDataLength = 0;
            
            // we must determine content length before we connect:
            if (param.getRequestMethod() == RequestMethod.POST || param.getRequestMethod() == RequestMethod.DELETE || param.getRequestMethod() == RequestMethod.PUT) {
                if (param.payload != null) {
                    postData = param.payload.getBytes(StandardCharsets.UTF_8);
                    postDataLength = postData.length;
                    conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                }
            }
            conn.setUseCaches(false);
            conn.setReadTimeout(120000);
            conn.connect();
            DataOutputStream wr = null;
            InputStream in = null;
            StringBuilder sb = null;
            try {
                // write request content if we have any
                if (param.getRequestMethod() == RequestMethod.POST || param.getRequestMethod() == RequestMethod.DELETE || param.getRequestMethod() == RequestMethod.PUT) {
                    if (postDataLength > 0) {
                        // logger.info("writing post data body");
                        wr = new DataOutputStream(conn.getOutputStream());
                        wr.write(postData);
                        wr.flush();            
                    }
                }
                in = conn.getInputStream();
                if (in == null) {
                    in = conn.getInputStream();
                }
                sb = new StringBuilder();
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = in.read(buffer)) >= 0) {
                    String str = new String(buffer, 0, len, StandardCharsets.UTF_8);
                    sb.append(str);
                    curBufferLengthUsed = sb.length();
                    if (curBufferLengthUsed / SIZE_RECEIVE_INFO > infoCount) {
                        infoCount++;
                    }
                    if (curBufferLengthUsed > SIZE_RECEIVE_MAX) {
                        // DOS attack assumed: cancel receiving further input
                        String msg = "Input cancelled because max input length (" + SIZE_RECEIVE_MAX + ") was exceeded.";
                        // logger.error(msg);
                        logger.error(msg);
                        param.status = 500;
                        param.content = msg;
                        sb = new StringBuilder();
                        return;
                    }
                }
            } catch (SocketTimeoutException ste) {
                String msg = "TIMEOUT SocketTimeoutException after reading [" + curBufferLengthUsed + "] bytes";
                handleException(conn, param, ste, msg);
            } catch (IOException e) {
                String msg = "IOException while reading connection input stream";
                handleException(conn, param, e, msg);
            } finally {
                if (wr != null) {
                    try {
                        wr.close();
                    } catch (IOException iox) {
                        // logger.error("Could not close output stream in CloudConnector");
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException iox) {
                        // logger.error("Could not close input stream in CloudConnector");
                    }
                }
            }
            String content = sb == null ? "" : sb.toString();
            int respCode = conn.getResponseCode();
            if (respCode == 302) {
                // redirect
                String location = conn.getHeaderField("Location");
                logger.warn("redirect requested to: " + location);
            }
            param.status = respCode;
            if (!param.contentSetByExceptionHandling) {
                param.content = content;
            }
        } catch (Exception e) {
            String msg = null;
            if (e instanceof SocketTimeoutException) {
                msg = "TIMEOUT SocketTimeoutException after reading [" + curBufferLengthUsed + "] bytes from call to " + param.getUrl() + " " + e.getMessage();
            } else {
                msg = "Exception in connection to " + param.getUrl() + " " + e.getMessage();
            }
            handleException(conn, param, e, msg);
        }
    }

    private static void tryReadingContentFromErrorStream(ConnectionParameter param, HttpURLConnection conn) {
        int curBufferLengthUsed = 0;
        if (conn == null) {
            return;
        }
        InputStream in = conn.getErrorStream();
        if (in == null) {
            // we cannot read any input
            return;
        }
        StringBuilder sb = new StringBuilder();
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                String str = new String(buffer, 0, len, StandardCharsets.UTF_8); 
                sb.append(str);
                curBufferLengthUsed = sb.length();
                if (curBufferLengthUsed > SIZE_RECEIVE_MAX) {
                    // DOS attack assumed: cancel receiving further input
                    String msg = "Input cancelled in error stream because max input length (" + SIZE_RECEIVE_MAX +") was exceeded.";
                    logger.error(msg);
                    param.status = 500;
                    param.content = null;
                    sb = new StringBuilder();
                    return;
                }
            }
            param.content = sb.toString();
            param.contentSetByExceptionHandling = true;
        } catch (SocketTimeoutException ste) {
            logger.error("TIMEOUT SocketTimeoutException after reading [" + curBufferLengthUsed + "] bytes: ", ste);
        } catch (IOException e) {
            param.content = "";
            logger.error(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static void handleException(HttpURLConnection conn, ConnectionParameter param, Exception e, String msg) {
        logger.error(msg, e);
        tryReadingContentFromErrorStream(param, conn);
        if (param.content != null) {
            // we could successfully read from error stream
            if (param.status == 0) {
                param.status = 550;
            }
            return;
        }
        // set exception msg and error code
        param.content = msg;
        param.status = 550;
    }

}
