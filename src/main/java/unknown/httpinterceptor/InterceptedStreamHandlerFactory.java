package unknown.httpinterceptor;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;
import java.util.function.Function;

public class InterceptedStreamHandlerFactory implements URLStreamHandlerFactory {

    private final Table<String, String, Function<Map<String, String>, byte[]>> interceptors = HashBasedTable.create();

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("http") || protocol.equals("https")) {
            return new InterceptedStreamHandler(protocol);
        }
        return null;
    }

    public void addInterceptor(String url, String path, Function<Map<String, String>, byte[]> responseGenerator) {
        interceptors.put(url, path, responseGenerator);
    }

    public class InterceptedStreamHandler extends URLStreamHandler {
        private final URLStreamHandler handler;
        private final Method openCon;
        private final Method openConProxy;

        public InterceptedStreamHandler(String protocol) {
            if (protocol.equals("http")) {
                handler = new sun.net.www.protocol.http.Handler();
            } else {
                handler = new sun.net.www.protocol.https.Handler();
            }
            try {
                openCon = handler.getClass().getDeclaredMethod("openConnection", URL.class);
                openCon.setAccessible(true);
                openConProxy = handler.getClass().getDeclaredMethod("openConnection", URL.class, Proxy.class);
                openConProxy.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            String host = u.getHost();
            String path = u.getPath().substring(u.getPath().lastIndexOf('/'));
            if (interceptors.containsRow(host) && interceptors.containsColumn(path)) {
                return interceptedConnection(u, interceptors.get(host, path));
            }
            return getDefaultConnection(u);
        }

        @Override
        protected URLConnection openConnection(URL u, Proxy p) throws IOException {
            String host = u.getHost();
            String path = u.getPath().substring(u.getPath().lastIndexOf('/'));
            if (interceptors.containsRow(host) && interceptors.containsColumn(path)) {
                return interceptedConnection(u, p, interceptors.get(host, path));
            }
            return getDefaultConnection(u, p);
        }

        private URLConnection interceptedConnection(URL u, Function<Map<String, String>, byte[]> response) {
            return interceptedConnection(u, null, response);
        }

        private URLConnection interceptedConnection(URL u, Proxy p, Function<Map<String, String>, byte[]> response) {
            try {
                return new ProxiedConnection(u, p, response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return getDefaultConnection(u, p);
        }

        public URLConnection getDefaultConnection(URL u) {
            try {
                return (URLConnection) openCon.invoke(handler, u);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }

        public URLConnection getDefaultConnection(URL u, Proxy p) {
            try {
                return (URLConnection) openConProxy.invoke(handler, u, p);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}


