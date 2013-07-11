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
        
        for (int i = 0; i < jsonItemsArray.size(); i = i + 1) {
            JSONObject jsonItemObject = (JSONObject) jsonItemsArray.get(i);
            
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
            
            
            //give item to player
            //inventory.addItem(item);
            //send info about item to player
            player.sendMessage(item.toString());
        }
    }
    
    
    
    
    
    private void get(String[] args) {
        String path = "/server/players/" + player.getDisplayName() + "/storage/ship?secret_key=" + ApiPlugin.secretKey;
        
        
        JSONArray jsonArr = new JSONArray();
        
        //JSONObject jsonItem;
        for (int i = 0; i < args.length; i = i + 2) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("materialId", args[i]);
            jsonItem.put("amount", args[i + 1]);
            
            jsonArr.add(jsonItem);
        }
        
        String response = HttpRequest.post(path, jsonArr.toJSONString());
        
        
        PlayerInventory inventory = player.getInventory();
        
        /*for (int i = 0; i < args.length; i = i + 2) {
            String[] materialArr = args[i].split(":");

            //id and data integer for creating item
            int materialId = Integer.parseInt(materialArr[0]);
            //modification for changing texture of item  
            int materialData = materialArr.length > 1 ? Integer.parseInt(materialArr[1]) : 0;
            //i think you understand this variable
            int amount = Integer.parseInt(args[i + 1]);
            
            

            //creating item
            //ItemStack item = new ItemStack(materialId,amount,(byte) materialData);
            ItemStack item = new ItemStack(Material.getMaterial(materialId), amount, (byte) materialData);

            player.sendMessage(item.toString());
        }*/
        
        
        /*ItemStack item = new ItemStack(5,64,(byte)2);
        ItemStack item0 = new ItemStack(5,64,(byte)0);
        ItemStack item1 = new ItemStack(264,64,(byte)0);*/
        
        /*MaterialData data = item.getData();
        data.setData((byte)0);
        item.setData(data);*/
        
        //meta.setDisplayName("MY INTENT");
        //aml.setItemMeta(meta);
        
        
        /*JSONArray list = new JSONArray();
        list.add("foo");
        list.add(new Integer(100));
        list.add(new Double(1000.21));
        list.add(new Boolean(true));
        list.add(null);
        System.out.print(list);*/
        
        
        /*JSONObject obj = new JSONObject();
        obj.put("materialId", "5");
        obj.put("amount", "64");
        
        JSONArray list = new JSONArray();
        list.add(obj);
        
        obj = new JSONObject();
        obj.put("materialId", "279");
        obj.put("amount", "1");
        
        list.add(obj);*/

        /*StringWriter out = new StringWriter();
        obj.writeJSONString(out);
        String jsonText = out.toString();
        System.out.print(jsonText);
        params.put{ "materialId": "5:0", "amount": 64 },
        { "materialId": "5:2", "amount": 64 },
        { "materialId": "279", "amount": 1 }

        HttpRequest.post(path, list.toJSONString());*/
    }
    
    private void getall() {
    }


    
    
    /*private ItemStack[] list() {
     * //ItemStack[] items = list();
                    //PlayerInventory inventory = player.getInventory();
        try {
            String input;
            StringBuilder responseData = new StringBuilder();
            ItemStack[] arrItems;

            
            URL remote = new URL(ApiPlugin.host + "/storage/");
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
    }*/
}
