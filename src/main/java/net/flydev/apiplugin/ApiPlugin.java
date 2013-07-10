package net.flydev.apiplugin;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ApiPlugin extends JavaPlugin {
    /**
     * The main logger.
     */
    private Logger logger = Logger.getLogger("Minecraft");
    /**
     * Remote host name
     */
    protected static String host = new String("http://apiserver.apiary.io");
    
    
    @Override
    public void onEnable() {
        logger.info("Hello!");
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        
        getCommand("storage").setExecutor(new CommandStorage());
        /*getCommand("shop").setExecutor(new CommandShop());*/
    }
}
