package hum.constants;

/**
 * @author hum
 */
public class Constants {
    public static final byte[] HEADER = new byte[]{7, 7, 7, 7, 7, 7, 7, 7};

    public static final int TCP_PORT_SERVER = 30401;

    public static final int UDP_PORT_SERVER = 30201;
    public static final int UDP_PORT_RESPONSE_CLIENT = 30202;

    public static final short UDP_SEARCH_COMMAND = 1;
    public static final short UDP_RESPONSE_COMMAND = 2;
    public static final int UDP_COMMAND_LENGTH = 2;
    public static final int PORT_LENGTH = 4;

}
