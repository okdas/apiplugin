package net.flydev.apiplugin;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
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
                    player.sendMessage("список вещей...");
                    
                    /* /storage list */
                    return list();
                }
                
                case "get": {
                    String[] params = new String[args.length - 1];
                    System.arraycopy(args, 1, params, 0, args.length - 1);
                    
                    player.sendMessage("получение...");
                    
                    /* /storage get 5 16 8 20 */
                    return get(params);
                }
                
                case "getall": {
                    player.sendMessage("получить все...");
                    
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
        String urlAllItems = "/api/v1/server/storage/" + player.getDisplayName() + "/list?key=" + ApiPlugin.secretKey;

        
        
        // все доступные айтемы
        String httpAllItems = HttpRequest.get(urlAllItems);

        if (httpAllItems == null) {
            //player.sendMessage("hm...something wrong");
            return false;
        }
        
        // строку в объект превращаем
        JSONObject jsonItemsObj = (JSONObject) JSONValue.parse(httpAllItems);
        
        // массив самих вещей { items: [...] }
        JSONArray jsonItems = (JSONArray) jsonItemsObj.get("items");

        
        
        for (int i = 0; i < jsonItems.size(); i++) {
            JSONObject jsonItem = (JSONObject) jsonItems.get(i);
            
            ItemStack item = jsonItemToItemStack(jsonItem);

            //пишем игроку
            player.sendMessage(ChatColor.RED + jsonItem.get("id").toString() + ": " + ChatColor.GREEN + item.getItemMeta().getDisplayName() + " x " + item.getAmount());
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
        /*
         * 1. скачиваем весь список доступных айтемов, список имеет id, энчаты
         *    и material (самое главное)
         * 2. берем по id нужные нам айтемы
         * 3. а дальше высчитываем amount подходящий и потом все как обычно
         * 4. отсылаем id и amount, материал не нужен
         */
        String urlAllItems = "/api/v1/server/storage/" + player.getDisplayName() + "/list?key=" + ApiPlugin.secretKey;
        String urlOpenShipment = "/api/v1/server/storage/" + player.getDisplayName() + "/shipments/open?key=" + ApiPlugin.secretKey;
        
        
        
        // все ли верно указано...
        if (args.length % 2 != 0) {
            return false;
        }
        
        
        
        // запрашиваем все айтемы которые есть, содержит id и материалы
        String httpAllItems =  HttpRequest.get(urlAllItems);
        if (httpAllItems == null) {
            //player.sendMessage("hm...something wrong");
            return false;
        }
        
        // строку в объект превращаем
        JSONObject jsonItemsObj = (JSONObject) JSONValue.parse(httpAllItems);
        
        // { items: [...] }
        JSONArray jsonItems = (JSONArray) jsonItemsObj.get("items");
        

        
        // узнаем сколько слотов свободно
        PlayerInventory inventory = player.getInventory();
        
        int freeSlots = 0;
        for(ItemStack i : inventory.getContents()) {
            if(i == null) {
                freeSlots++;
            } else if(i.getType() == Material.AIR) {
                freeSlots++;
            }
        }
        
        if (freeSlots == 0) {
            player.sendMessage(ChatColor.RED + "нет места");
            return false;
        }
        

        
        // теперь надо выдрать те айтемы которые нужны
        // это массив айтемов с id, material, amount
        ArrayList<String[]> queryItems = new ArrayList<String[]>();
        
        // args содержит id, amount
        for (int i = 0, j = 0; i < (args.length / 2); i++) {
            int id = Integer.parseInt(args[j]);
            
            
            for (int k = 0; k < jsonItems.size(); k++) {
                JSONObject jsonItem = (JSONObject) jsonItems.get(k);
                

                // если это то что нам нужно, раскладываем его нормально
                if (new Integer(jsonItem.get("id").toString()) == id) {
                    // id, material, amount
                    String[] itemWithOptions = new String[3];
                    
                    itemWithOptions[0] = jsonItem.get("id").toString();
                    itemWithOptions[1] = jsonItem.get("material").toString();
                    if (Integer.parseInt(args[j + 1]) > Integer.parseInt(jsonItem.get("amount").toString())) {
                        itemWithOptions[2] = jsonItem.get("amount").toString();
                    } else {
                        itemWithOptions[2] = args[j + 1];
                    }
                    
                    queryItems.add(itemWithOptions);
                }
            }
            
            
            j += 2;
        }
        

        
        // список и количесво вещей которые мы запросим у сервера
        ArrayList<String[]> sendItems = new ArrayList<String[]>();
        
        // цикл проходит по вещам из массива queryItems
        for (String[] queryItem : queryItems) {
            // количество айтемов, нужно для сравнения со свободными слотами
            int sizeReqItems = sendItems.size();
            

            
            String[] materialArr = queryItem[1].split(":");

            // id - число для определения айтема в игре
            int materialId = Integer.parseInt(materialArr[0]);
            
            // модификатор определяющий текстуру айтема (есть не у всех)  
            int materialData = materialArr.length > 1 ? Integer.parseInt(materialArr[1]) : 0;
            
            // количество которое хотим получить
            int wantAmount = new Integer(queryItem[2]);
            
            // создаем предварительный айтем, для того что бы узнать размер стека
            ItemStack preAddItem = new ItemStack(materialId, wantAmount, (byte) materialData);
            
            
            
            // количество вещей которые мы добавили уже в массив
            // и то количество, которое отошлем на сервер
            int sendAmount = 0;
            
            // если количество запрошенных больше чем стек
            while (wantAmount > preAddItem.getMaxStackSize()) {
                
                // проверяем есть ли еще свободное место
                if (sizeReqItems < freeSlots) {
                    sendAmount += preAddItem.getMaxStackSize();
                    wantAmount -= preAddItem.getMaxStackSize();
                    
                    // говорим что виртуально добавили один айтем в массив
                    sizeReqItems++;
                } else {
                    break;
                }
            }
            
            if (sizeReqItems < freeSlots) {
                sendAmount += wantAmount;
                sizeReqItems++;
            }
            
            
            
            // окончательный массив одной вещи
            String[] sendItem = new String[2];
            sendItem[0] = queryItem[0];
            sendItem[1] = new Integer(sendAmount).toString();
            
            // добавляем в общий массив
            sendItems.add(sendItem);
        }
        
        
        
        /*
         * json массив который отошлем в запросе
         * [
         *     {
         *          id: '5',
         *          amount: 10
         *      }, {
         *          id: '7',
         *          amount: '2'
         *      }
         *  ]
         */
        JSONArray jsonGetItemsArr = new JSONArray();
        
        for (int i = 0; i < sendItems.size(); i++) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("id", sendItems.get(i)[0]);
            jsonItem.put("amount", sendItems.get(i)[1]);
            
            jsonGetItemsArr.add(jsonItem);
        }
        
        
        
        /*
         * отправляем json с предметами и открываем шипмент на сервере
         * получаем ответ, который содержит айтемы и их количество которое нам
         * доступно из запршиваемого списка
         * и придет
         * [
         *     {
         *          id: 5,
         *          amount: 10
         *      }, {
         *          id: 7,
         *          amount: 2
         *      }
         *  ]
         */
        String httpGetItems =  HttpRequest.post(urlOpenShipment, jsonGetItemsArr.toJSONString());
        if (httpGetItems == null) {
            //logger.warning("hm...something wrong");
            return false;
        }
        
        logger.info(httpGetItems);
        
        JSONObject jsonResShipment = (JSONObject) JSONValue.parse(httpGetItems);
        
        //идентификатор шипмента
        String shipmentId = jsonResShipment.get("id").toString();

        //массив айтемов которые есть на складе и которые мы дадим игроку
        JSONArray jsonShipmentItems = (JSONArray) jsonResShipment.get("items");
        
        
        // проходимся по входящему массиву
        for (int i = 0; i < jsonShipmentItems.size(); i++) {
            // айтем шипмета
            JSONObject jsonShipmentItem = (JSONObject) jsonShipmentItems.get(i);
            
            for (int j = 0; j < jsonItems.size(); j++) {
                // айтем из общего списка
                JSONObject jsonItem = (JSONObject) jsonItems.get(j);
                
                
                
                // ищем подходящий в общем массиве и даем игроку
                if (Integer.parseInt(jsonItem.get("id").toString()) == Integer.parseInt(jsonShipmentItem.get("id").toString())) {
                    JSONObject jsonRightItem = jsonItem;
                    jsonRightItem.put("amount", jsonShipmentItem.get("amount").toString());
                    
                    ItemStack item = jsonItemToItemStack(jsonRightItem);

                    
                    //даем игроку
                    player.sendMessage(ChatColor.RED + jsonItem.get("id").toString() + ": " + ChatColor.GREEN + item.getItemMeta().getDisplayName() + " x " + item.getAmount());
                    
                    inventory.addItem(item);
                }
            }
        }
        
        
        
        //ок, все сделали, закрываем шипмент теперь
        String closeShip = "/api/v1/server/storage/" + player.getDisplayName() + "/shipments/" + shipmentId + "/close?key=" + ApiPlugin.secretKey;
        HttpRequest.get(closeShip);
        
        
        return true;
    }
    
    
    
    
    
    /**
     * Выдача всех айтемов доступных на складе.
     * 
     * @return возвращает состояние выполенения команды
     */
    private boolean getall() {
        String urlAllItems = "/api/v1/server/storage/" + player.getDisplayName() + "/list?key=" + ApiPlugin.secretKey;
        String urlCloseShipment = "/server/players/" + player.getDisplayName() + "/storage/items?secret_key=" + ApiPlugin.secretKey;
        
        
        // запрашиваем все айтемы которые есть
        String httpAllItems =  HttpRequest.get(urlAllItems);
        
        logger.info(httpAllItems);
        
        /*PlayerInventory inventory = player.getInventory();
        
        
        String responseItemsArray = HttpRequest.get(urlCloseShipment);
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
        }*/
        
        return true;
    }


    
    
    
    private boolean test() {
        int id = 276;
        int amount = 360;
        
        
        ItemStack item = new ItemStack(id, amount);
        
        player.sendMessage(new Integer(item.getMaxStackSize()).toString());
        
        return false;
    }
    
    
    
    
    
    /**
     * Преобразование {@link JSONObject} в {@link ItemStack}.
     * @param jsonItem объект состоящий из id, количества, имени, зачарований элемента
     * @return возвращает объект {@link ItemStack}
     */
    private ItemStack jsonItemToItemStack(JSONObject jsonItem) {
        String[] materialArr = jsonItem.get("material").toString().split(":");
        
        //id число для определения айтема
        int material = Integer.parseInt(materialArr[0]);
        
        //модификатор определяющий текстуру айтема (есть не у всех)  
        int materialData = materialArr.length > 1 ? Integer.parseInt(materialArr[1]) : 0;

        //количество айтемов
        int amount = Integer.parseInt(jsonItem.get("amount").toString());
        
        

        //создаем айтем, путем получения сначала материала
        //ItemStack item = new ItemStack(materialId,amount,(byte) materialData);
        ItemStack item = new ItemStack(Material.getMaterial(material), amount, (byte) materialData);
        
        
        
        //может быть другое имя айтема, устанавливаем его
        String nameString = (String) jsonItem.get("title");
        if (nameString != null) {
            ItemMeta itemMeta = item.getItemMeta();
            
            itemMeta.setDisplayName(nameString);
            item.setItemMeta(itemMeta);
        }
        
        
        
        //зачарования для айтема
        JSONArray enchantments = (JSONArray) jsonItem.get("enchantments");
        if (enchantments.size() > 0) {
            for (int j = 0; j < enchantments.size(); j = j + 1) {
                JSONObject jsonEnchantment = (JSONObject) enchantments.get(j);
                
                //id зачарования и его уровень
                int id = Integer.parseInt(jsonEnchantment.get("enchantmentId").toString());
                int level = Integer.parseInt(jsonEnchantment.get("level").toString());

                //создаем зачарование
                Enchantment enchantment = Enchantment.getById(id);
                
                //небезопасно добавляем его к предмету
                item.addUnsafeEnchantment(enchantment, level);
            }
        }
        
        return item;
    }
    
    
    
    
  
    private static JSONObject itemStackToJsonObject(ItemStack item) {
        JSONObject jsonItemsArray = new JSONObject();
        return jsonItemsArray;
    }
}
