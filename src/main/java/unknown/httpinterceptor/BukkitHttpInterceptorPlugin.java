package unknown.httpinterceptor;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;

public class BukkitHttpInterceptorPlugin extends JavaPlugin {

    private static final InterceptedStreamHandlerFactory urlStreamHandlerFactory = new InterceptedStreamHandlerFactory();

    static {
        unsetURLStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);
    }

    private static String unsetURLStreamHandlerFactory() {
        try {
            Field f = URL.class.getDeclaredField("factory");
            f.setAccessible(true);
            Object curFac = f.get(null);
            f.set(null, null);
            URL.setURLStreamHandlerFactory(null);
            return curFac.getClass().getName();
        } catch (Exception e) {
            return null;
        }
    }

    public static void addInterceptor(String url, String path, Function<Map<String, String>, byte[]> responseGenerator) {
        urlStreamHandlerFactory.addInterceptor(url, path, responseGenerator);
    }
}