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
                    list();
                    player.sendMessage("list items...");
                    return false;
                }
                case "get": {
                    String[] params = new String[args.length - 1];
                    System.arraycopy(args, 1, params, 0, args.length - 1);
                    
                    get(params);
                    player.sendMessage("get item...");
                    return false;
                }
                case "getall": {
                    getall();
                    player.sendMessage("get all items...");
                    return false;
                }
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    
    
    
    
    
    private void list() {
        PlayerInventory inventory = player.getInventory();
        String path = "/server/players/" + player.getDisplayName() + "/storage/items?secret_key=" + ApiPlugin.secretKey;
        
        
        JSONArray jsonItemsArray = (JSONArray) JSONValue.parse(HttpRequest.get(path));
        
        ItemStack[] listItems = jsonArrToItemStack(jsonItemsArray);
        
        for (ItemStack item : listItems) {
            //give item to player
            //inventory.addItem(item);
            
            //send info about item to player
            player.sendMessage(item.toString());
        }
    }
    
    
    
    
    
    private void get(String[] args) {
        PlayerInventory inventory = player.getInventory();
        String path = "/server/players/" + player.getDisplayName() + "/storage/ship?secret_key=" + ApiPlugin.secretKey;
        
        
        JSONArray jsonGetItemsArr = new JSONArray();
        
        //JSONObject jsonItem;
        for (int i = 0; i < args.length; i = i + 2) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("materialId", args[i]);
            jsonItem.put("amount", args[i + 1]);
            
            jsonGetItemsArr.add(jsonItem);
        }
        
        JSONObject jsonResponse = (JSONObject) JSONValue.parse(HttpRequest.post(path, jsonGetItemsArr.toJSONString()));
        
        String shipmentId = (String) jsonResponse.get("shipmentId");
        JSONArray jsonStorageItemsArray = (JSONArray) jsonResponse.get("items");
        
        
        
        
        int needFreeSlots = jsonStorageItemsArray.size();
        
        
        //get full stack inventory of player, 36 position
        ItemStack[] inventoryItems = inventory.getContents();
        
        //how much player have a got free places in inventory
        int freeslots = 0;
        for (ItemStack item : inventoryItems) {
            if (item == null) {
                freeslots =+ 1;
            }
        }
        
        
        
        
        if (needFreeSlots > freeslots) {
            //not good, cancel the shipment
            //server/players/{playerId}/storage/shipments/{shipmentId}/cancel{?secret_key}
            String cancelShip = "/server/players/" + player.getDisplayName() + "/storage/shipments/" + shipmentId + "/cancel?secret_key=" + ApiPlugin.secretKey;
            HttpRequest.get(path);
        } else {
            //well, we have a got enough free space and put this fucking items
            
            //get array of items
            ItemStack[] listItems = jsonArrToItemStack(jsonStorageItemsArray);
            
            for (ItemStack item : listItems) {
                //give item to player
                inventory.addItem(item);
            }
            
            //ok, fine, now we need to close the shipment
            //server/players/{playerId}/storage/shipments/{shipmentId}/close{?secret_key}
            String closeShip = "/server/players/" + player.getDisplayName() + "/storage/shipments/" + shipmentId + "/close?secret_key=" + ApiPlugin.secretKey;
            HttpRequest.get(path);
        }
    }
    
    
    
    
    private void getall() {
        PlayerInventory inventory = player.getInventory();
        String path = "/server/players/" + player.getDisplayName() + "/storage/items?secret_key=" + ApiPlugin.secretKey;
        
        
        JSONArray jsonItemsArray = (JSONArray) JSONValue.parse(HttpRequest.get(path));
        
        ItemStack[] listItems = jsonArrToItemStack(jsonItemsArray);
        
        for (ItemStack item : listItems) {
            //give item to player
            inventory.addItem(item);
            
            //send info about item to player
            //player.sendMessage(item.toString());
        }
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
            /*JSONArray enchantmentsArr = (JSONArray) jsonItemObject.get("enchantments");
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
            }*/
            
            
            items[i] = item;
        }
        
        return items;
    }
}
