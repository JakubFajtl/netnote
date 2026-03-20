package commons.constants;

/**
 * This class contains some constants that are used by the application
 */
public class AppConstants {
    public static final String AppName = "NetNote";

    // This header is used for error messages
    public static final String ErrorMessageHeader = "x_error_message";

    // This header is used for error messages that can be presented to the user
    public static final String UserErrorMessageHeader = "x_user_error_message";

    // The default url of a server
    public static final String DefaultServerUrl = "http://localhost:8080/";

    // The location of the config file
    public static final String DefaultConfigFileName = "config";

    // The name of the starting default collection
    public static final String DefaultCollectionStartingNameClient = "my notes";
    public static final String DefaultCollectionStartingNameServer = "my-notes";

    // The maximum upload size we allow the user
    public static final long maximumAllowedFileSizeInB = 64L*1024*1024;


}
