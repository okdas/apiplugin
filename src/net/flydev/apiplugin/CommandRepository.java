package net.flydev.apiplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CommandRepository implements CommandExecutor {
    /**
     * The main logger.
     */
    private Logger logger = Logger.getLogger("Minecraft");
    
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "list": {
                    ItemStack[] items = list();
                    sender.sendMessage("you have a got...");
                    for (Integer i = 0; i < items.length; i = i + 1) {
                        sender.sendMessage(items[i].toString());
                    }
                }
                case "get": {
                }
                case "getall": {
                    ItemStack[] items = list();
                    Player player = (Player) sender;
                    PlayerInventory inventory = player.getInventory();
                    
                    for (Integer i = 0; i < items.length; i = i + 1) {
                        inventory.addItem(items[i]);
                    }
                    sender.sendMessage("this you fucking items...");
                }
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    
    
    private ItemStack[] list() {
        try {
            String input;
            StringBuilder responseData = new StringBuilder();
            ItemStack[] arrItems;

            
            URL remote = new URL(ApiPlugin.host + "/item/list");
            BufferedReader in = new BufferedReader(new InputStreamReader(remote.openStream()));

            while ((input = in.readLine()) != null) {
                responseData.append(input);
            }
            
            in.close();
            
            JSONObject responseObject = (JSONObject) JSONValue.parse(responseData.toString());

            if (responseObject.get("request") != null) {
                JSONArray jsonArray = (JSONArray) responseObject.get("request");
                
                arrItems = new ItemStack[jsonArray.size()];
            
                for (Integer i = 0; i < jsonArray.size(); i = i + 1) {
                    JSONArray arrItem = (JSONArray) jsonArray.get(i);
                    int id = Integer.parseInt(arrItem.get(0).toString());
                    int amount = Integer.parseInt(arrItem.get(1).toString());
                    
                    arrItems[i] = new ItemStack(id, amount);
                }
                
                return arrItems;
            } else {
                logger.info("WTF");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void get() {
    }
}
