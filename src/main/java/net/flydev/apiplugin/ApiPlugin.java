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
    protected static String host = new String("http://apiserver.apiary.io");
    /**
     * Connection to database
     */
    static Db db;
    
    
    @Override
    public void onEnable() {
        logger.info("Hello!");
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        
        //FileConfiguration conf = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "customConfig.yml"));
        FileConfiguration config = getConfig();
        
        String hostname = config.getString("mysql.hostname");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        
        
        logger.info(hostname);
        //host, db, user, pass
        db = new Db(hostname, database, username, password);
        
        getCommand("storage").setExecutor(new CommandStorage());
        /*getCommand("shop").setExecutor(new CommandShop());*/
    }
}
