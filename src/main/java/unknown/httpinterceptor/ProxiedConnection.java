package unknown.httpinterceptor;

import com.google.common.base.Splitter;
import sun.net.www.protocol.http.Handler;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;

public class ProxiedConnection extends HttpURLConnection {

    private final Proxy proxy;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private ByteArrayInputStream inputStream;
    private boolean outClosed = false;

    private final Function<Map<String, String>, byte[]> response;

    public ProxiedConnection(URL url, Proxy proxy, Function<Map<String, String>, byte[]> response) throws IOException {
        super(url, new Handler());
        this.proxy = proxy;
        this.response = response;
    }

    @Override
    public void disconnect() { }

    @Override
    public boolean usingProxy() {
        return proxy != null;
    }

    @Override
    public void connect() throws IOException { }

    @Override
    public InputStream getInputStream() throws IOException {
        System.out.println("[BukkitHttpInterceptor] Intercepted " + getURL().toString()); // No instance, logger dead

        final Map<String, String> query = Splitter.on('&').trimResults()
                .withKeyValueSeparator('=')
                .split(url.getQuery());

        if (inputStream == null) {
            outClosed = true;
            responseCode = HTTP_OK;
            inputStream = new ByteArrayInputStream(this.response.apply(query));
        }
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outClosed) {
            throw new RuntimeException("Write after send");
        }
        return outputStream;
    }
}
