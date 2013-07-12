package net.flydev.apiplugin;


import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class CommandStorage implements CommandExecutor {
    /**
     * The main logger.
     */
    private Logger logger = Logger.getLogger("Minecraft");
    /**
     * The player
     */
    private Player player;

    
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        player = (Player) sender;
        
        if (args.length > 0) {
            switch (args[0]) {
                case "list": {
                    player.sendMessage("list items...");
                    
                    return list();
                }
                case "get": {
                    String[] params = new String[args.length - 1];
                    System.arraycopy(args, 1, params, 0, args.length - 1);
                    
                    player.sendMessage("get item...");
                    
                    return get(params);
                }
                case "getall": {
                    player.sendMessage("get all items...");
                    
                    return getall();
                }
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    
    
    
    
    
    private boolean list() {
        PlayerInventory inventory = player.getInventory();
        String path = "/server/players/" + player.getDisplayName() + "/storage?secret_key=" + ApiPlugin.secretKey;
        
        
        
        
        
        String responseItems = HttpRequest.get(path);
        if (responseItems == null) {
            //player.sendMessage("hm...something wrong");
            return false;
        }
        
        JSONObject jsonItemsObj = (JSONObject) JSONValue.parse(responseItems);
        JSONArray jsonItemsArray = (JSONArray) jsonItemsObj.get("items");
        
        ItemStack[] listItems = jsonArrToItemStack(jsonItemsArray);
        
        for (ItemStack item : listItems) {
            //give item to player
            //inventory.addItem(item);
            
            //send info about item to player
            player.sendMessage(item.toString());
        }
        return true;
    }
    
    
    
    
    
    
    
    private boolean get(String[] args) {
        PlayerInventory inventory = player.getInventory();
        String path = "/server/players/" + player.getDisplayName() + "/storage/shipments/open?secret_key=" + ApiPlugin.secretKey;
        
        
        
        
        //how much we want get items
        int getItemsSlots = args.length / 2;
        
        
        //get full stack inventory of player, 36 position
        ItemStack[] inventoryItems = inventory.getContents();
        
        int freeslots = 0;
        for (ItemStack item : inventoryItems) {
            if (item == null) {
                freeslots =+ 1;
            }
        }
        
        
        //if we have less free slots than we want, try to cut items array
        if (getItemsSlots > freeslots) {
            getItemsSlots = freeslots;
        }
        
        
        
        
        
        
        JSONArray jsonGetItemsArr = new JSONArray();
        
        //JSONObject jsonItem;
        for (int i = 0; i < (getItemsSlots * 2); i = i + 2) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("materialId", args[i]);
            jsonItem.put("amount", args[i + 1]);
            
            jsonGetItemsArr.add(jsonItem);
        }
        
        
        
        //send message to server for open shipment
        String response =  HttpRequest.post(path, jsonGetItemsArr.toJSONString());
        if (response == null) {
            //logger.warning("hm...something wrong");
            return false;
        }
        JSONObject jsonResponse = (JSONObject) JSONValue.parse(response);
        
        
        String shipmentId = jsonResponse.get("shipmentId").toString();
        JSONArray jsonStorageItemsArray = (JSONArray) jsonResponse.get("items");
        
        
        
        
        //well, we have a got enough free space and put this fucking items
        
        //get array of items
        ItemStack[] listItems = jsonArrToItemStack(jsonStorageItemsArray);
        
        for (ItemStack item : listItems) {
            //give item to player
            inventory.addItem(item);
            
            //player.sendMessage(item.toString());
        }
        
        //ok, fine, now we need to close the shipment
        //server/players/{playerId}/storage/shipments/{shipmentId}/close{?secret_key}
        String closeShip = "/server/players/" + player.getDisplayName() + "/storage/shipments/" + shipmentId + "/close?secret_key=" + ApiPlugin.secretKey;
        HttpRequest.get(path);
        
        
        return true;
    }
    
    
    
    
    
    
    private boolean getall() {
        PlayerInventory inventory = player.getInventory();
        String path = "/server/players/" + player.getDisplayName() + "/storage/items?secret_key=" + ApiPlugin.secretKey;
        
        String responseItemsArray = HttpRequest.get(path);
        if (responseItemsArray == null) {
            //logger.warning("hm...something wrong");
            return false;
        }
        
        JSONArray jsonItemsArray = (JSONArray) JSONValue.parse(responseItemsArray);
        
        ItemStack[] listItems = jsonArrToItemStack(jsonItemsArray);
        
        for (ItemStack item : listItems) {
            //give item to player
            inventory.addItem(item);
            
            //send info about item to player
            //player.sendMessage(item.toString());
        }
        
        return true;
    }


    
    

    
    private static ItemStack[] jsonArrToItemStack(JSONArray array) {
        ItemStack[] items = new ItemStack[array.size()];
        
        for (int i = 0; i < array.size(); i = i + 1) {
            JSONObject jsonItemObject = (JSONObject) array.get(i);
            
            String[] materialArr = jsonItemObject.get("materialId").toString().split(":");

            
            //id and data integer for creating item
            int materialId = Integer.parseInt(materialArr[0]);
            //modification for changing texture of item  
            int materialData = materialArr.length > 1 ? Integer.parseInt(materialArr[1]) : 0;

            int amount = Integer.parseInt(jsonItemObject.get("amount").toString());
            
            

            //creating item
            //ItemStack item = new ItemStack(materialId,amount,(byte) materialData);
            ItemStack item = new ItemStack(Material.getMaterial(materialId), amount, (byte) materialData);
            
            
            
            //different name for new item
            /*String nameString = (String) jsonItemObject.get("name");
            if (nameString != null) {
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(nameString);
                item.setItemMeta(itemMeta);
            }*/

            
            
            
            //enchantment for new item
            JSONArray enchantmentsArr = (JSONArray) jsonItemObject.get("enchantments");
            if (enchantmentsArr != null) {
                for (int j = 0; j < enchantmentsArr.size(); j = j + 1) {
                    JSONArray enchantmentsIdentify = (JSONArray) enchantmentsArr.get(j);
                    
                    //id enchant
                    int id = Integer.parseInt(enchantmentsIdentify.get(0).toString());
                    //level of enchanting
                    int level = Integer.parseInt(enchantmentsIdentify.get(1).toString());
                    
                    
                    Enchantment enchantment = Enchantment.getById(id);
                    
                    
                    item.addUnsafeEnchantment(enchantment, level);
                }
            }
            
            
            items[i] = item;
        }
        
        return items;
    }
}
