package unknown.httpinterceptor.util;

import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HexUtil {

    private HexUtil() { }

    public static String hex2String(String hex) {
        ByteBuffer buff = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i+=2) {
            buff.put((byte)Integer.parseInt(hex.substring(i, i + 2), 16));
        }
        ((Buffer) buff).rewind();
        Charset cs = StandardCharsets.UTF_8;
        CharBuffer cb = cs.decode(buff);
        return cb.toString();
    }

    public static String string2Hex(String hex) {
        return String.format("%040x", new BigInteger(1, hex.getBytes(StandardCharsets.UTF_8)));
    }
}
