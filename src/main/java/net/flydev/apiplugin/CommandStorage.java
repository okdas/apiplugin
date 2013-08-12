package net.flydev.apiplugin;


import java.util.ArrayList;
import java.util.List;
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
        
        
        // все ли верно указано...
        if (args.length % 2 != 0) {
            return false;
        }
        

        // узнаем сколько слотов свободно 
        int freeSlots = 0;
        for(ItemStack i : inventory.getContents()) {
            if(i == null) {
                freeSlots++;
            } else if(i.getType() == Material.AIR) {
                freeSlots++;
            }
        }
        
        
        
        // список и количесво вещей которые мы запросим у сервера
        ArrayList<ArrayList<String>> reqItems = new ArrayList<ArrayList<String>>();
        
        // цикл проходит по вещам которые мы запросили в чате
        for (int i = 0, j = 0; i < (args.length / 2); i++) {
            // количество айтемов
            int sizeReqItems = reqItems.size();
            
            
            String[] materialArr = args[j].split(":");

            // id - число для определения айтема
            int materialId = Integer.parseInt(materialArr[0]);
            
            // модификатор определяющий текстуру айтема (есть не у всех)  
            int materialData = materialArr.length > 1 ? Integer.parseInt(materialArr[1]) : 0;
            
            // количество которое хотим получить
            int wantAmount = Integer.parseInt(args[j + 1]);
            
            // создаем предварительный айтем, для того что бы узнать размер стека
            ItemStack preAddItem = new ItemStack(materialId, wantAmount, (byte) materialData);
            
            
            // количество вещей которые мы добавили уже в массив
            int reqAmount = 0;
            
            // если количество запрошенных больше чем стек
            while (wantAmount > preAddItem.getMaxStackSize()) {
                
                // проверяем есть ли еще свободное место
                if (sizeReqItems < freeSlots) {
                    reqAmount += preAddItem.getMaxStackSize();
                    wantAmount -= preAddItem.getMaxStackSize();
                    
                    // говорим что виртуально добавили один айтем в массив
                    sizeReqItems++;
                } else {
                    break;
                }
            }
            
            if (sizeReqItems < freeSlots) {
                reqAmount += wantAmount;
                sizeReqItems++;
            }
            
            
            // окончательный массив одной вещи
            ArrayList<String> item = new ArrayList<String>();
            item.add(args[j]);
            item.add(new Integer(reqAmount).toString());
            
            // добавляем в общий массив
            reqItems.add(item);
            
            
            j += 2;
        }
        
        
        
        // json массив который отошлем в запросе
        JSONArray jsonGetItemsArr = new JSONArray();
        
        for (int i = 0; i < reqItems.size(); i++) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("materialId", reqItems.get(i).get(0));
            jsonItem.put("amount", reqItems.get(i).get(1));
            
            jsonGetItemsArr.add(jsonItem);
        }
        
        
        logger.info(jsonGetItemsArr.toJSONString());
        
        
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
        JSONArray jsonStorageItemsArray = (JSONArray) jsonResponse.get("items");
        
        
        //преобразовываем json айтемов в ItemStack[]
        ItemStack[] listItems = jsonArrToItemStack(jsonStorageItemsArray);
        
        for (ItemStack item : listItems) {
            //даем игроку айтем
            inventory.addItem(item);
            // нужно добавить создание массва добавленых айтемов его мы будем отправлять для закрытия шипмента
            //player.sendMessage(item.toString());
        }
        
        //ок, все сделали, закрываем шипмент теперь
        //server/players/{playerId}/storage/shipments/{shipmentId}/close{?secret_key}
        String closeShip = "/server/players/" + player.getDisplayName() + "/storage/shipments/" + shipmentId + "/close?key=" + ApiPlugin.secretKey;
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
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        
        int id = 5;
        int amount = 360;
        
        
        ItemStack preAddItem = new ItemStack(id, amount, (byte) 1);
        
        while (amount > preAddItem.getMaxStackSize()) {
            ItemStack quasiItem = new ItemStack(id, preAddItem.getMaxStackSize(), (byte) 1);
            items.add(quasiItem);
           
            amount -= preAddItem.getMaxStackSize();
            
            logger.info(new Integer(amount).toString());
        }
        
        items.add(new ItemStack(id, amount, (byte) 1));
        
        for (ItemStack item: items) {
            inventory.addItem(item);
        }
        
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
