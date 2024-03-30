package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;
import net.botwithus.api.game.hud.inventories.LootInventory;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.SceneObjectUpdateEvent;
import net.botwithus.rs3.events.impl.ServerTickedEvent;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.vars.VarDomainType;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.minimenu.actions.GroundItemAction;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.GroundItemQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.Entity;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.item.GroundItem;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.NativeBoolean;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;
import net.botwithus.rs3.*;


import java.util.*;

public class Vindicta extends LoopingScript {

    private VindictaGraphicsContext script1;
    private BotState botState = BotState.IDLE;
    private boolean killrequired = true;
    private Random random = new Random();
    public long scriptStartTime = System.currentTimeMillis();

    private Npc npc;
    private int thresholdvalue = 0;
    private int instanceregionID = 0;
    private int surgecount =0;
    int vindictadeathcounter = 0;
    private int invokedeathcounter = 0;
    int killperhour = 0;
    int startingx = 0;
    int startingy = 0;
    Coordinate bottomRightCorner = null;
    Coordinate topleftCorner = null;
    Coordinate characterPosition = null;
    /////
    private String[] POTION_NAME = {
            "Elder Overload potion (1)",
            "Elder Overload potion (2)",
            "Elder Overload potion (3)",
            "Elder Overload potion (4)",
            "Elder Overload potion (5)",
            "Elder Overload potion (6)",
            "Elder Overload salve (1)",
            "Elder Overload salve (2)",
            "Elder Overload salve (3)",
            "Elder Overload salve (4)",
            "Elder Overload salve (5)",
            "Elder Overload salve (6)"};


        ////////////////////////
    private boolean useStatBoostingPotion = false;

    private boolean darknessenable = false;
    private boolean invokedeath = false;

    private boolean deathessence = false;
    private boolean essenceoff = false;
    private boolean eatFood = true;

    int presentNumber = 1;
    int numberofkills = 1;
    //NativeInteger presetNumber = new NativeInteger(1);
    private int currentItem = 0;
    String[] prayerRestoreOptions = { "Super Restore", "Prayer Potions" };
    String selectedoption = "None";
        /////////////////

    enum BotState {
        //define your own states here
        IDLE,
        FIGHTING,
        BANKING,
        //...
    }

    public void prayerrestore()
    {
        NativeInteger currentItemNative = new NativeInteger(currentItem);
        if(ImGui.Combo("Prayer Restore options", currentItemNative, prayerRestoreOptions))
        {
            currentItem =currentItemNative.get();
            println("Rune Selected" + prayerRestoreOptions[currentItem]);
        }
    }




    public Vindicta(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);

    }

    @Override
    public boolean initialize()
    {
        //this.loopDelay = 500;
        setActive(false);
        this.sgc = new VindictaGraphicsContext(getConsole(), this);
        this.loopDelay = 500;
        subscribe(ServerTickedEvent.class, ServerTickedEvent -> {

            if(instanceregionID == Client.getLocalPlayer().getCoordinate().getRegionId())
            {

                vandictafight();
            }
            if(Client.getLocalPlayer().getCurrentHealth() <= 5000 && eatFood == true)    //// EAT FOOD
            {
                eatFood();
            }
        });
        killperhour();
        return super.initialize();
    }

    private void eatFood()
    {
        Item food = InventoryItemQuery.newQuery(93).category(58).results().first();
        if (food == null) {
            println("[healHealth] Out of food.");
            return;
        }
        boolean eat = Backpack.interact(food.getSlot(), "Eat");
        if (eat) {
            println("[healHealth] Successfully ate " + food.getName());
            Execution.delay(RandomGenerator.nextInt(250,450));
        } else {
            println("[healHealth] Failed to eat.");
        }
    }

    public void killperhour()
    {
        long currnettime = (System.currentTimeMillis() - scriptStartTime) /1000;
        killperhour = (int) Math.round(3600.0/ currnettime * vindictadeathcounter);
    }

    @Override
    public void onLoop() {
        //Loops every 100ms by default, to change:
        //this.loopDelay = 500;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(3000,7000));
            return;
        }
        switch (botState) {
            case IDLE -> {
                //do nothing
                println("We're idle!");
                Execution.delay(random.nextLong(1000,3000));
            }
            case FIGHTING -> {
                    Execution.delay(handleSkilling(player));
            }
            case BANKING -> {
                //handle your banking logic, etc
            }
        }
    }


    private long handleSkilling(LocalPlayer player) {

        int currentHealth = player.getCurrentHealth();
        int adrenaline = VarManager.getVarValue(VarDomainType.PLAYER, 679);
        int currentprayer = player.getPrayerPoints();
       /* println("Current Health" + currentHealth);
        println("Prayer Points" + currentprayer);
        println("Print Adrenaline" + adrenaline);
        println("Player Max Health:" + player.getMaximumHealth());
*/
        if(player.getCoordinate().getRegionId() == 13214)
        {
            SceneObject BankChest = SceneObjectQuery.newQuery().name("Bank chest").results().nearest();
            SceneObject altarwar = SceneObjectQuery.newQuery().name("Altar of War").results().nearest();
            SceneObject adrenalinecrystal = SceneObjectQuery.newQuery().name("Adrenaline crystal").results().nearest();
            thresholdvalue = 0; // Setting up value to 0 so player can interact with Threshold stupid crap
            surgecount = 0; // Setting up value to 0 everytime player enter's war's area
            //random.nextLong(1200, 1800);
           /* println("Bank Present" + presentNumber);
            println("Stat potion check" + useStatBoostingPotion);
            println("Stat potion check" + eatFood);*/
            if(vindictadeathcounter >=1)
            {
                Execution.delay(RandomGenerator.nextInt(1000,2000));
            }

            if (currentHealth < player.getMaximumHealth())
            {
                if(BankChest != null)
                {
                    println("Bank chest open" + BankChest.interact("Use"));

                    Execution.delayUntil(10000,() -> player.getCurrentHealth() == player.getMaximumHealth());
                    if (Bank.isOpen()) {
                        println("Bank is open");
                        Bank.loadPreset(presentNumber);
                    }
                }
            }
            if(currentprayer < 9000)
            {
                if(altarwar != null)
                {
                    println("Prayer Refill: " + altarwar.interact("Pray"));
                    Execution.delayUntil(10000,() -> player.getPrayerPoints() >= 9600);
                }
            }
            if(adrenaline < 1000)
            {
                if(adrenalinecrystal != null)
                {
                    println("Adrenaline Refill: " + adrenalinecrystal.interact("Channel"));
                    Execution.delayUntil(10000,() -> VarManager.getVarValue(VarDomainType.PLAYER, 679) == 1000);
                }
            }
            if(adrenaline >= 800 && currentprayer >=9000 && currentHealth >= player.getMaximumHealth())
            {

                bossportal(player);
            }


        }

        if(player.getCoordinate().getRegionId() == 12395 || player.getCoordinate().getRegionId() == 12396 || player.getCoordinate().getRegionId() == 12651 || player.getCoordinate().getRegionId() == 12652 )
        {
            SceneObject threshold = SceneObjectQuery.newQuery().name("Threshold").results().nearest();
            if(VarManager.getVarbitValue(30856) >= 400 && GetInstanceTimeLeft() >=1 && GetInstanceTimeLeft() <=60)
            {

                println("Threshold value Outside: " + thresholdvalue);
                if(threshold != null && thresholdvalue == 0)
                {

                    Execution.delay(1000);
                    println("Enter Threshold: " + threshold.interact("Traverse"));
                    Execution.delayUntil(40000,()-> threshold.interact("Traverse"));;
                    thresholdvalue = thresholdvalue +1;
                    Execution.delay(1000);
                    println("Threshold value: " + thresholdvalue);

                }else if(threshold != null && thresholdvalue == 1)
                {
                    println("Threshold value Second Loop: " + thresholdvalue);
                    enterfight(player);
                }

            }else if(killrequired == false)
            {
                if (threshold !=null && thresholdvalue == 0)
                {
                    thresholdvalue = thresholdvalue + 1;
                    Execution.delay(1000);
                    println("Enter Threshold: " + threshold.interact("Traverse"));
                    Execution.delay(1000);
                    println("Threshold value: " + thresholdvalue);

                }else if(threshold !=null && thresholdvalue == 1)
                {
                    println("Threshold value Second Loop: " + thresholdvalue);
                    enterfight(player);
                }
            }
            else
            {
                println(" Not have enough kills to enter the portal");
                killsrequired(player);
            }
        }

        if(instanceregionID == Client.getLocalPlayer().getCoordinate().getRegionId())
        {
            Npc vindictap2 = NpcQuery.newQuery().name("Gorvek and Vindicta").results().nearest();
            if(vindictap2 !=null && vindictap2.getCurrentHealth() == 0)
            grounditem();
        }
        return random.nextLong(500,950);
    }

    private void bossportal(LocalPlayer player)
    {
        SceneObject bossportal = SceneObjectQuery.newQuery().name("Portal (Vindicta & Gorvek)").results().nearest();
        if(bossportal !=null)
        {
            println("Enter Vindicta Portal: " + bossportal.interact("Enter"));
        }
        else
        {
            println("Unable to find the portal");
        }
    }

    private void killsrequired(LocalPlayer player)
    {
        Area specfiicArea = new Area.Rectangular(new Coordinate(3119,6924,1),new Coordinate(3154,6898,1));
        /*EntityResultSet<Npc>  npcsInArea = NpcQuery.newQuery().inside(specfiicArea).results();
        for(Npc npc: npcsInArea)
        {
            println("NPC Name: " +npc.getName() +", Location: " + npc.getCoordinate());
        }
        println("Player Coordinates: " + player.getCoordinate());*/
        Npc demon = NpcQuery.newQuery().name("Abyssal demon").inside(specfiicArea).results().nearest();
        Npc ancientran = NpcQuery.newQuery().name("Ancient ranger").inside(specfiicArea).results().nearest();
        Npc ancientwar = NpcQuery.newQuery().name("Ancient warrior").inside(specfiicArea).results().nearest();
        Npc ancientmage = NpcQuery.newQuery().name("Ancient mage").inside(specfiicArea).results().nearest();
        Npc nechryael = NpcQuery.newQuery().name("Nechryael").inside(specfiicArea).results().nearest();

        if(demon !=null || ancientmage !=null || ancientran !=null || ancientwar != null || nechryael != null)
        {
            if(player.distanceTo(demon) <=30)
            {
                attackNpc(demon);
            }else if (player.distanceTo(ancientmage) <=30)
            {
                attackNpc(ancientmage);
            }else if (player.distanceTo(ancientran) <=30)
            {
                attackNpc(ancientran);
            }else if (player.distanceTo(ancientwar) <=30)
            {
                attackNpc(ancientwar);
            }else if(player.distanceTo(nechryael) <=30)
            {
                attackNpc(nechryael);
            }
        }
        else
        {
            println("can't find enemies");
        }

        /*if(!npcsInArea.isEmpty())
        {


            Npc nearestnpc = npcsInArea.nearestTo(player.getCoordinate());
            if(nearestnpc !=null)
            {
                attackNpc(nearestnpc);
                random.nextLong(750, 1350);
            }
        }*/

    }
    private void enterfight(LocalPlayer player) {

        SceneObject barrier = SceneObjectQuery.newQuery().name("Barrier").results().nearest();
        if (barrier != null) {
            println("Traverse the Barrier: " + barrier.interact("Traverse"));
            Execution.delay(2000);
            MiniMenu.interact(ComponentAction.COMPONENT.getType(), 1, -1, 104267836);  //Region ID 37452 //npc name: Vindicta phase2: Gorvek and Vindicta
            // Vindicta All three Animation: 28253, 28260, 28256

            Execution.delay(3000);

        }
        if (player.getCoordinate().getRegionId() != 12395) {
            SceneObject barrier1 = SceneObjectQuery.newQuery().name("Barrier").results().nearest();
            if (barrier1 != null) {
                println("Traverse the Barrier: " + barrier1.interact("Traverse"));
                Execution.delayUntil(10000, () -> Interfaces.isOpen(1188));
                MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 77856776);
                delay(2000);
                startingx = barrier1.getCoordinate().getX();
                startingy = barrier1.getCoordinate().getY() - 1;

                //bottomRightCorner = new Coordinate(startingx, startingy - 31, Client.getLocalPlayer().getCoordinate().getZ());
                //topleftCorner = new Coordinate(startingx - 22, startingy, Client.getLocalPlayer().getCoordinate().getZ());

                bottomRightCorner = new Coordinate(startingx - 22 , startingy - 31, Client.getLocalPlayer().getCoordinate().getZ());
                topleftCorner = new Coordinate(startingx, startingy, Client.getLocalPlayer().getCoordinate().getZ());

                characterPosition = Client.getLocalPlayer().getCoordinate();

                instanceregionID = player.getCoordinate().getRegionId();

            }
        }
    }

    private void vandictafight()
    {
        if(useStatBoostingPotion && overloadChecked == false)
        {
           useOverload();
        }
        if(darknessenable)
        {
            activateDarkness();
        }



        Npc vindictap1 = NpcQuery.newQuery().name("Vindicta").results().nearest();
        Npc vindictap2 = NpcQuery.newQuery().name("Gorvek and Vindicta").results().nearest();
        Component resonance = ComponentQuery.newQuery(284).spriteId(14222).results().first();
        SceneObjectQuery firewall = SceneObjectQuery.newQuery();
        firewall.id(101908);
        EntityResultSet<SceneObject> results = firewall.results();
        Area.Rectangular rectangularArea = new Area.Rectangular(topleftCorner, bottomRightCorner);

        Coordinate playerPosition = Client.getLocalPlayer().getCoordinate();
        boolean isOnObject = isPlayerstandingonobject(playerPosition);

        if(isOnObject)
        {
            Coordinate safePositon = findsafespotCoorindate(rectangularArea);
            if(safePositon !=null)
            {
                Movement.walkTo(safePositon.getX(),safePositon.getY(),false);
                delay(600);
                println("Player standing on Object: " + isPlayerstandingonobject(playerPosition));
                println("Player moving to safe spot: " + safePositon);
            }
        }
        //println("Region ID inside instance:" + instanceregionID);
        //println("Region ID inside instance Player :" + Client.getLocalPlayer().getCoordinate().getRegionId());
        if(vindictap1 != null)
        {
            meleeprayerswitch();
            //println(" Inside the loop where boss detected");
            println("Animation Boss ID: " + vindictap1.getAnimationId());
            attackNpc(vindictap1);
            DeathEssence();
            essenceoffin();
            if(surgecount ==0)
            {
                ActionBar.useAbility("Surge");
                surgecount = surgecount +1;
            }


            if(vindictap1.getAnimationId() == 28253)
            {
                meleeprayerswitch();

            }else if(vindictap1.getAnimationId() == 28256)
            {
                meleeprayerswitch();

            }else if(vindictap1.getAnimationId() == 28260)
            {
                meleeprayerswitch();

            }
            else {
                println("No extra animation Detected");
            }

            if (!results.isEmpty() && vindictap1.getAnimationId() == 28260)
            {

                for(int x=0; x<=1; x++){
                //Area.Rectangular rectangularArea = new Area.Rectangular(topleftCorner, bottomRightCorner);
                moveTounoccupiedCoorindate(rectangularArea);
            }
            }

        }
        else if(vindictap2 !=null) {

            attackNpc(vindictap2);
            if(invokedeathcounter ==0)
                {invokedeath();}
            DeathEssence();
            essenceoffin();
            println("Animation Boss ID: " + vindictap2.getAnimationId());
            if (vindictap2.getAnimationId() == 28273) {
                meleeprayerswitch();


            } else if (vindictap2.getAnimationId() == 28274) {
                int rescooldown = ActionBar.getCooldown("Resonance");
                if (resonance == null && rescooldown == 0) {
                    ActionBar.useAbility("Resonance");
                } else {
                    rangeprayerswitch();

                }
            } else if (vindictap2.getAnimationId() == 28276) {
                //delay(1200);
                attackNpc(vindictap2);
                //ActionBar.useAbility("Surge");
            }
            else {
                println(" extra animation Detected" + vindictap2.getAnimationId());
            }

            if (!results.isEmpty() && vindictap2.getAnimationId() == 28275)
            {
                delay(1800);
                for(int x=0; x<=1; x++) {
                    //Area.Rectangular rectangularArea = new Area.Rectangular(topleftCorner, bottomRightCorner);
                    moveTounoccupiedCoorindate(rectangularArea);
                }
            }
            if(vindictap2.getCurrentHealth() == 0 || vindictap2.getAnimationId() == 28272)
            {
                surgecount = 0;
                thresholdvalue = 0;
                vindictadeathcounter = vindictadeathcounter +1;
                invokedeathcounter = 0;


                if(VarManager.getVarbitValue(16769) == 1)
                    ActionBar.usePrayer("Deflect Ranged");
                else if(VarManager.getVarbitValue(16770) == 1)
                    ActionBar.usePrayer("Deflect Melee");


                //grounditem();


            }
        }

    }

    private void vindictaphase1()
    {}

    private void vindictaphase2()
    {}
    private static boolean overloadChecked = false;

    private boolean isOverloadActive() {
        boolean overloadActive = VarManager.getVarbitValue(26037) != 0;

        if (!overloadChecked && overloadActive) {
            overloadChecked = true;
        }

        return overloadActive;
    }

    private void useOverload() {

        if (!isOverloadActive()) {

            String[] overloadSalveVariants = new String[]{
                    "Elder overload potion",
                    "Elder overload salve",
                    "Elder overload potion (1)",
                    "Elder overload potion (2)",
                    "Elder overload potion (3)",
                    "Elder overload potion (4)",
                    "Elder overload potion (5)",
                    "Elder overload potion (6)",
                    "Elder overload salve (1)",
                    "Elder overload salve (2)",
                    "Elder overload salve (3)",
                    "Elder overload salve (4)",
                    "Elder overload salve (5)",
                    "Elder overload salve (6)"};

            for (String potionName : overloadSalveVariants) {
                if (ActionBar.containsItem(potionName)) {
                    boolean successfulDrink = ActionBar.useItem(potionName, "Drink");
                    if (successfulDrink) {
                        println("Drank " + potionName + " to activate Overload.");
                        break;
                    }
                }
            }
        }
    }

    private void prayerrestorepotions()
    {
    ResultSet<Item> query = InventoryItemQuery.newQuery(93).results();
    Item potions = query.stream().filter(item ->item.getName() != null && item.getName().toLowerCase().contains("prayer"))
                        .findFirst().orElse(null);

                if(potions != null)
                {
                    println("Drinking " + potions.getName());
                    boolean success = ActionBar.useAbility("Drink");
                    Execution.delay(RandomGenerator.nextInt(600,1200));

                    if(!success)
                    {
                        println("Failed to use " + potions.getName());
                    }else
                    {
                        println(" No Prayer or Restore potion found in the inventory");
                    }

                }

    }
/////

    private boolean darknessChecked = false;

    public boolean isDarknessActive() {
        Component darkness = ComponentQuery.newQuery(284).spriteId(30122).results().first();
        boolean darknessActive = darkness != null;

        if (!darknessChecked && darknessActive) {
            darknessChecked = true; // Ensure we only log this once
        }

        return darknessActive;
    }

    private void activateDarkness() {
        if (!isDarknessActive()) {
            boolean success = ActionBar.useAbility("Darkness");
            if (success) {
                println("Activated Darkness via ActionBar.");
            } else {
                println("Failed to activate Darkness via ActionBar.");
            }
        }
    }
////

    private void DeathEssence() { //55480 sprite iD
        if (deathessence) {
            if (Client.getLocalPlayer() != null) {

                if (Client.getLocalPlayer().getAdrenaline() >= 350 && Client.getLocalPlayer().getFollowing() != null && Client.getLocalPlayer().getFollowing().getCurrentHealth() >= 500 && ComponentQuery.newQuery(291).spriteId(55480).results().isEmpty() && Client.getLocalPlayer().hasTarget()) {
                    println("Used Death Essence: " + ActionBar.useAbility("Weapon Special Attack"));
                    //Execution.delayUntil(RandomGenerator.nextInt(1820, 1850), () -> ComponentQuery.newQuery(291).spriteId(55480).results().isEmpty());
                }
            }
        }
    }

    private void essenceoffin() {  //55480 sprite iD
        if (essenceoff) {
            if (Client.getLocalPlayer() != null) {

                if (Client.getLocalPlayer().getAdrenaline() >= 350 && Client.getLocalPlayer().getFollowing() != null && Client.getLocalPlayer().getFollowing().getCurrentHealth() >= 500 && ComponentQuery.newQuery(291).spriteId(55524).results().isEmpty() && Client.getLocalPlayer().hasTarget()) {
                    println("Used Essence of Finality: " + ActionBar.useAbility("Essence of Finality"));
                    //Execution.delayUntil(RandomGenerator.nextInt(1820, 1850), () -> ComponentQuery.newQuery(291).spriteId(55524).results().isEmpty());
                }
            }
        }
    }

    private void invokedeath() { //55480 sprite iD
        if (invokedeath) {
            if (Client.getLocalPlayer() != null) {
                int currenthealth = Client.getLocalPlayer().getFollowing().getCurrentHealth();
                int maxthealth = Client.getLocalPlayer().getFollowing().getMaximumHealth();
                double healthpercentage = (double) currenthealth/maxthealth;
                if (Client.getLocalPlayer().getFollowing() != null && healthpercentage <= 20 &&  Client.getLocalPlayer().hasTarget()) {


                    if(VarManager.getVarbitValue(53247) == 0)
                        println("Invoke Death : " + ActionBar.useAbility("Invoke Death"));
                        invokedeathcounter = invokedeathcounter +1;


                }
            }
        }
    }

    //
    public int GetInstanceTimeLeft()
    {

        int minutes = (VarManager.getVarValue(VarDomainType.PLAYER, 9925) - VarManager.getVarValue(VarDomainType.PLAYER, 11489) - 1);
        int seconds = (59 - (VarManager.getVarc(6930) / 50));
        String timeFormatted = String.format("%d:%02d", minutes, seconds);
        //println(timeFormatted);
        // return 0;
        return  minutes;
    }


    //
    private boolean isPlayerstandingonobject(Coordinate playerpostion)
    {
        SceneObjectQuery firewall = SceneObjectQuery.newQuery();
        firewall.id(101908);
        EntityResultSet<SceneObject> objectsAround = firewall.results();

        for (SceneObject object: objectsAround)
        {
            if(object.getCoordinate().equals(playerpostion))
               return true;
        }
        return false;
    }

    public Coordinate findsafespotCoorindate(Area.Rectangular rectangularArea) {
        List<Coordinate> allcoordinates = getAllCoordinatesInRectangularArea(rectangularArea);
        List<Coordinate> sceneobjectCoordinates = getSceneObjectCoordinates(rectangularArea);


        Coordinate currentplayerPostion = Client.getLocalPlayer().getCoordinate();

        Coordinate closestCoordinate = allcoordinates.stream().filter(coord -> !sceneobjectCoordinates.contains(coord))
                .filter(coord -> Distance.between(currentplayerPostion, coord) >= 2)
                .min(Comparator.comparingDouble(coord -> Distance.between(currentplayerPostion, coord)))
                .orElse(null);

            return closestCoordinate;
    }

        public void moveTounoccupiedCoorindate(Area.Rectangular rectangularArea)
    {
        List<Coordinate> allcoordinates = getAllCoordinatesInRectangularArea(rectangularArea);
        List<Coordinate> sceneobjectCoordinates = getSceneObjectCoordinates(rectangularArea);


        Coordinate currentplayerPostion = Client.getLocalPlayer().getCoordinate();

        Coordinate closestCoordinate = allcoordinates.stream().filter(coord -> !sceneobjectCoordinates.contains(coord))
                .filter(coord -> Distance.between(currentplayerPostion,coord) >=3.5)
                .min(Comparator.comparingDouble(coord -> Distance.between(currentplayerPostion,coord)))
                .orElse(null);

        if(closestCoordinate !=null){
            Movement.walkTo( closestCoordinate.getX(), closestCoordinate.getY(), false);
            delay(600);}
    }

    public List<Coordinate>getAllCoordinatesInRectangularArea (Area.Rectangular rectangularArea)
    {
        List<Coordinate> coordinateswithinArea = new ArrayList<>();
        Coordinate bottomLeft  = rectangularArea.getBottomLeft();
        Coordinate topRight  = rectangularArea.getTopRight();

        for (int x = bottomLeft.getX(); x <= topRight.getX(); x++) {
            for (int y = bottomLeft.getY(); y <= topRight.getY(); y++) {
                // Assuming the plane (z-axis) is the same for the entire area
                coordinateswithinArea.add(new Coordinate(x, y, bottomLeft.getZ()));
            }
        }

        return coordinateswithinArea;

    }



    public List<Coordinate> getSceneObjectCoordinates(Area.Rectangular rectangularArea)
    {
        SceneObjectQuery query = SceneObjectQuery.newQuery();
        List<SceneObject> sceneObjects = query.id(101908).results().stream().toList();

        List<Coordinate> coordinates = new ArrayList<>();
        //Set<Coordinate> coordinates = new HashSet<>();
        for (SceneObject object:sceneObjects)
        {
            Coordinate objectCoordinate = object.getCoordinate();
            coordinates.add(objectCoordinate);
        }
        return coordinates;
    }

    public void grounditem()
    {
        try {
            ResultSet<GroundItem> items = GroundItemQuery.newQuery().results();
                for (GroundItem item : items) {
                    if (item.interact("Take")) {
                        println("Picking up: " + item.getName());
                        Execution.delayUntil(10000,() -> Interfaces.isOpen(1622));
                        MiniMenu.interact(ComponentAction.COMPONENT.getType(),1,-1,106299414);
                        long waittime = RandomGenerator.nextInt(1800, 3000);
                        Thread.sleep(waittime);

                        if(numberofkills == numberofkills) {
                            ActionBar.useTeleport("War's Retreat Teleport");
                            println("Teleporting to War's Retreat");
                        }
                        }
                    }
           Thread.sleep(600);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }

    private void attackNpc(Npc npc)
    {
        npc.interact("Attack");
        Execution.delay(1000);
    }

    private void meleeprayerswitch()
    {
        println("Detecting Melee switch, attempting...");
        if(VarManager.getVarbitValue(16770) == 0)
            ActionBar.usePrayer("Deflect Melee");
        else {
            println("Varbit 16798 was" + VarManager.getVarbitValue(16770));
        }
    }

    private void rangeprayerswitch()
    {
        println("Detecting Range switch, attempting...");
        if(VarManager.getVarbitValue(16769) == 0)
            ActionBar.usePrayer("Deflect Ranged");
        else {
            println("Varbit 16769 was" + VarManager.getVarbitValue(16769));
        }
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isuseStatBoostingPotion() {
        return useStatBoostingPotion;
    }

    public void setuseStatBoostingPotion(boolean useStatBoostingPotion) {
        this.useStatBoostingPotion = useStatBoostingPotion;
    }

    public boolean iskillrequired() {
        return killrequired;
    }

    public void setkillrequired(boolean killrequired) {
        this.killrequired = killrequired;
    }

    public boolean isEatFood() {
        return eatFood;
    }

    public void setEatFood(boolean eatFood) {
        this.eatFood = eatFood;
    }
    public boolean isdarknessenable () {
        return darknessenable ;
    }

    public void setdarknessenable (boolean darknessenable ) {
        this.darknessenable  = darknessenable ;
    }
    public boolean isinvokedeath () {
        return invokedeath;
    }

    public void setinvokedeath (boolean invokedeath ) {
        this.invokedeath = invokedeath ;
    }
    public boolean isdeathessence () {
        return deathessence ;
    }

    public void setdeathessence (boolean deathessence ) {
        this.deathessence  = deathessence ;
    }
    public boolean isessenceoff () {
        return essenceoff ;
    }

    public void setessenceoff (boolean essenceoff ) {
        this.essenceoff  = essenceoff ;
    }
}
