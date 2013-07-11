package net.flydev.apiplugin;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ApiPlugin extends JavaPlugin {
    /**
     * The main logger.
     */
    private Logger logger = Logger.getLogger("Minecraft");
    /**
     * Remote host name
     */
    protected static String host = new String("http://storage1.apiary.io");
    /**
     * Key for requesting to APIserver
     */
    protected static String secretKey; 

    
    
    @Override
    public void onEnable() {
        logger.info("Hello!");
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        
        //FileConfiguration conf = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "customConfig.yml"));
        FileConfiguration config = getConfig();
        
        secretKey = config.getString("apiserver-key");
        
        
        
        getCommand("storage").setExecutor(new CommandStorage());
        /*getCommand("shop").setExecutor(new CommandShop());*/
    }
}
