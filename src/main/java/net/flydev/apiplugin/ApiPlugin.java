package net.flydev.apiplugin;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Главный класс, привязывает класс к команде.
 * На данный момент доступна одна команда - 'storage'. Которая выполняет операции
 * по купленным товарам, ее выполнение обрабатывает класс {@link CommandStorage}.
 *
 */
public class ApiPlugin extends JavaPlugin {
    /**
     * Главный логер
     */
    private Logger logger = Logger.getLogger("Minecraft");
    /**
     * Удаленное имя хоста для api запросов
     */
    protected static String host = new String("http://127.0.0.1:8000");
    /**
     * Секретный ключ сервера, для api запросов
     */
    protected static String secretKey; 

    

    /**
     * Возникает при доступности плагина.
     * Перегружаемый метод, содержит привязку событий к классу и выполенение команды к классу.
     * Так же из конфигурационного файла вытаскивается секретный ключ для запроса
     * на api сервер.
     */
    @Override
    public void onEnable() {
        logger.info("Hello!");
        //Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        
        //FileConfiguration conf = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "customConfig.yml"));
        FileConfiguration config = getConfig();
        
        secretKey = config.getString("apiserver-key");
        
        
        
        getCommand("storage").setExecutor(new CommandStorage());
        /*getCommand("shop").setExecutor(new CommandShop());*/
    }
}
