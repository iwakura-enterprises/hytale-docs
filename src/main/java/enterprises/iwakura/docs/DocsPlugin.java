package enterprises.iwakura.docs;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.BeanDefinition;
import enterprises.iwakura.sigewine.core.Sigewine;

public class DocsPlugin extends JavaPlugin {

    private final Sigewine sigewine = new Sigewine();

    private Docs docsInstance;
    private Logger logger = new Logger("Docs");

    public DocsPlugin(@NonNull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        logger.info("Docs Enabled!");
        logger.info("Starting up...");

        logger.info("Injecting beans into Sigewine...");
        sigewine.registerBean(BeanDefinition.of(DocsPlugin.class), this);
        sigewine.registerBean(BeanDefinition.of(Logger.class), logger);

        logger.info("Initializing Sigewine...");
        sigewine.scan("enterprises.iwakura.docs", this.getClassLoader());
        logger.info("Sigewine initialized successfully!");

        logger.info("Injecting Docs class...");
        docsInstance = sigewine.inject(Docs.class);

        logger.info("Setting up Docs...");
        docsInstance.setup();
    }

    @Override
    protected void start() {
        logger.info("Starting Docs...");
        docsInstance.start();
    }

    @Override
    protected void shutdown() {
        logger.info("Shutting down Docs...");
        if (docsInstance != null) {
            docsInstance.shutdown();
        }
    }
}
