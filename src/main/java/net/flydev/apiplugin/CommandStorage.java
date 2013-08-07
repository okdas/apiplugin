package net.flydev.apiplugin;


import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;



class CommandStorage implements CommandExecutor {
    /**
     * Главный логер
     */
    private Logger logger = Logger.getLogger("Minecraft");
    /**
     * Игрок, который вызвал команду.
     */
    private Player player;

    
    
    
    
    /**
     * Возникает при выполнении команды зарегистрированной в {@link ApiPlugin}.
     * На данный момент может обрабатывать 3 типа аргументов. Первый 'list' это
     * список купленных товаров. Дальше 'get' это получение определенного товара\товаров
     * со склада (для их получения нужно указывать их id и количество). И 'getall' - 
     * получение всех товаров со склада.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        player = (Player) sender;
        
        if (args.length > 0) {
            switch (args[0]) {
                
                case "list": {
                    player.sendMessage("list items...");
                    
                    /* /storage list */
                    return list();
                }
                
                case "get": {
                    String[] params = new String[args.length - 1];
                    System.arraycopy(args, 1, params, 0, args.length - 1);
                    
                    player.sendMessage("get item...");
                    
                    /* /storage get 5 16 8 20 */
                    return get(params);
                }
                
                case "getall": {
                    player.sendMessage("get all items...");
                    
                    /* /storage getall */
                    return getall();
                }
                
                case "test": {
                    player.sendMessage("test...");
                    
                    /* /storage test */
                    return test();
                }
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    
    
    
    
    
    /**
     * Список доступных айтемов на складе.
     * 
     * @return возвращает состояние выполенения команды
     */
    private boolean list() {
        String path = "/api/v1/server/storage/" + player.getDisplayName() + "/list?key=" + ApiPlugin.secretKey;

        
        
        String responseItems = HttpRequest.get(path);
        if (responseItems == null) {
            //player.sendMessage("hm...something wrong");
            return false;
        }
        
        JSONObject jsonItemsObj = (JSONObject) JSONValue.parse(responseItems);
        
        // { items: [...] }
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
    
    
        
    
    
    /**
     * Выдача определенных айтемов доступных на складе.
     * 
     * @param args массив содержащий id и количество айтемов
     * @return возвращает состояние выполенения команды 
     */
    private boolean get(String[] args) {
        PlayerInventory inventory = player.getInventory();
        String path = "/server/players/" + player.getDisplayName() + "/storage/shipments/open?key=" + ApiPlugin.secretKey;
        
        
        /*
         * у заданных предметов нужно получить максимальный стек
         * дабы не интерпретировать 128 штук за один стек
         */
        //ItemStack item = new ItemStack(Material.getMaterial(materialId), amount, (byte) materialData);
        
        // все ли верно указано...
        if (args.length % 2 != 0) {
            return false;
        }
        
        
        
        // строим двумерный массив требуемых айтемов
        String[][] requestItems = new String[args.length / 2][2];
        
        ItemStack[] wantItems = new ItemStack[args.length / 2];
        
        for (int i = 0, j = 0; i < wantItems.length; i++) {
                        
            
            j += 2;
        }
        
        
        // получаем весь инвентарь игрока, 36 позиций
        ItemStack[] playerInventoryItems = inventory.getContents();
        
        int freeslots = 0;
        for (ItemStack item : playerInventoryItems) {
            if (item == null) {
                freeslots =+ 1;
            }
        }
        
        
        /*
         * если у нас меньше свободных слотов чем мы запросили
         * то добавим только то количество айтемов сколько можно
         */
        /*if (getItemsSlots > freeslots) {
            getItemsSlots = freeslots;
        }

        
        
        
        
        JSONArray jsonGetItemsArr = new JSONArray();
        
        
        //генерируем json для удаленного запроса нужных айтемов         
        for (int i = 0; i < (getItemsSlots * 2); i = i + 2) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("materialId", args[i]);
            jsonItem.put("amount", args[i + 1]);
            
            jsonGetItemsArr.add(jsonItem);
        }*/
        
        
        
        /*
         * отправляем json с предметами и открываем шипмент на сервере
         * получаем ответ, который содержит айтемы и их количество которое нам
         * доступно из запршиваемого списка
         */
        /*String response =  HttpRequest.post(path, jsonGetItemsArr.toJSONString());
        if (response == null) {
            //logger.warning("hm...something wrong");
            return false;
        }
        JSONObject jsonResponse = (JSONObject) JSONValue.parse(response);
        
        //идентификатор шипмента
        String shipmentId = jsonResponse.get("shipmentId").toString();
        
        //массив айтемов которые есть на складе и которые мы дадим игроку
        JSONArray jsonStorageItemsArray = (JSONArray) jsonResponse.get("items");*/
        
        
        
        
        
        //преобразовываем json айтемов в ItemStack[]
        /*ItemStack[] listItems = jsonArrToItemStack(jsonStorageItemsArray);
        
        for (ItemStack item : listItems) {
            //даем игроку айтем
            inventory.addItem(item);
            /*
             * нужно добавить создание массва добавленых айтемов
             * его мы будем отправлять для закрытия шипмента
             */
            //player.sendMessage(item.toString());
        //}
        
        //ок, все сделали, закрываем шипмент теперь
        //server/players/{playerId}/storage/shipments/{shipmentId}/close{?secret_key}
        /*String closeShip = "/server/players/" + player.getDisplayName() + "/storage/shipments/" + shipmentId + "/close?secret_key=" + ApiPlugin.secretKey;
        HttpRequest.get(closeShip);*/
        
        
        return true;
    }
    
    
    
    
    
    /**
     * Выдача всех айтемов доступных на складе.
     * 
     * @return возвращает состояние выполенения команды
     */
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
            //выдаем айтем игроку
            inventory.addItem(item);
            
            //отправляем инфу о выданном айтеме игроку
            //player.sendMessage(item.toString());
        }
        
        return true;
    }


    
    
    
    private boolean test() {
        PlayerInventory inventory = player.getInventory();
        //ItemStack item = new ItemStack(Material.getMaterial(5), 320);
        //inventory.addItem(item);
        return false;
    }
    
    
    
    
    
    /**
     * Преобразование {@link JSONArray} массива в массив {@link ItemStack}.
     * @param array массив состоящий из id, количества, имени, зачарований элемента
     * @return возвращает массив {@link ItemStack}
     */
    private static ItemStack[] jsonArrToItemStack(JSONArray array) {
        ItemStack[] items = new ItemStack[array.size()];
        
        for (int i = 0; i < array.size(); i = i + 1) {
            JSONObject jsonItemObject = (JSONObject) array.get(i);
            
            String[] materialArr = jsonItemObject.get("materialId").toString().split(":");

            
            //id число для определения айтема
            int materialId = Integer.parseInt(materialArr[0]);
            
            //модификатор определяющий текстуру айтема (есть не у всех)  
            int materialData = materialArr.length > 1 ? Integer.parseInt(materialArr[1]) : 0;

            //количество айтемов
            int amount = Integer.parseInt(jsonItemObject.get("amount").toString());
            
            

            //создаем айтем, путем получения сначала материала
            //ItemStack item = new ItemStack(materialId,amount,(byte) materialData);
            ItemStack item = new ItemStack(Material.getMaterial(materialId), amount, (byte) materialData);
            
            
            
            //может быть другое имя айтема, устанавливаем егоe
            String nameString = (String) jsonItemObject.get("name");
            if (nameString != null) {
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(nameString);
                item.setItemMeta(itemMeta);
            }

            
            
            
            //зачарования для айтема
            JSONArray enchantmentsArr = (JSONArray) jsonItemObject.get("enchantments");
            if (enchantmentsArr != null) {
                for (int j = 0; j < enchantmentsArr.size(); j = j + 1) {
                    JSONArray enchantmentsIdentify = (JSONArray) enchantmentsArr.get(j);
                    
                    //id зачарования и его уровень
                    int id = Integer.parseInt(enchantmentsIdentify.get(0).toString());
                    int level = Integer.parseInt(enchantmentsIdentify.get(1).toString());
                    
                    
                    //создаем зачарование
                    Enchantment enchantment = Enchantment.getById(id);
                    
                    
                    //небезопасно добавляем его к предмету
                    item.addUnsafeEnchantment(enchantment, level);
                }
            }
            
            
            items[i] = item;
        }
        
        return items;
    }
    
    
    
    private static JSONArray itemStackToJsonArr(ItemStack[] items) {
        JSONArray jsonItemsArray = new JSONArray();
        return jsonItemsArray;
    }
}
