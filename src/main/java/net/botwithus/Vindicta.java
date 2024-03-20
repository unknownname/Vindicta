package net.botwithus;

import net.botwithus.api.game.hud.inventories.Bank;
import net.botwithus.api.game.hud.inventories.LootInventory;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.SceneObjectUpdateEvent;
import net.botwithus.rs3.events.impl.ServerTickedEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.vars.VarDomainType;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.GroundItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.scene.entities.Entity;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.item.GroundItem;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;


import java.util.Random;

public class Vindicta extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private boolean killrequired = true;
    private Random random = new Random();
    private Npc npc;
    private int thresholdvalue = 0;
    private int instanceregionID = 0;
    private int surgecount =0;

    enum BotState {
        //define your own states here
        IDLE,
        FIGHTING,
        BANKING,
        //...
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

        });
        return super.initialize();
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
                //do some code that handles your skilling
                //Execution.delay(handleSkilling(player));

                    Execution.delay(handleSkilling(player));

            }
            case BANKING -> {
                //handle your banking logic, etc
            }
        }
    }
    private long handlebanking()
    {
        if (Bank.isOpen())
        {
            println("Bank is open");
            Bank.loadPreset(3);
            botState = BotState.FIGHTING;
            return random.nextLong(1000,3000);
        }else
        {
            SceneObject bankChest = SceneObjectQuery.newQuery().name("Bank chest").results().nearest();
            if(bankChest != null)
            {
                println("Interact with War Bank: " + bankChest.interact("Use"));
            }
        }
        return random.nextLong(750, 1650);
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


            if (currentHealth < player.getMaximumHealth())
            {
                if(BankChest != null)
                {
                    println("Bank chest open" + BankChest.interact("Use"));

                    Execution.delayUntil(10000,() -> {
                        return player.getCurrentHealth() == player.getMaximumHealth();
                    });
                    if (Bank.isOpen()) {
                        println("Bank is open");
                        Bank.loadPreset(3);
                    }
                }
            }
            if(currentprayer < 9000)
            {
                if(altarwar != null)
                {
                    println("Prayer Refill: " + altarwar.interact("Pray"));
                    Execution.delayUntil(10000,() -> {
                        return player.getPrayerPoints() >= 9600;
                    });
                }
            }
            if(adrenaline < 1000)
            {
                if(adrenalinecrystal != null)
                {
                    println("Adrenaline Refill: " + adrenalinecrystal.interact("Channel"));
                    Execution.delayUntil(10000,() -> {
                       return VarManager.getVarValue(VarDomainType.PLAYER, 679) ==1000;
                    });
                }
            }
            if(adrenaline >= 800 && currentprayer >=9600 && currentHealth ==player.getMaximumHealth())
            {

                bossportal(player);
            }


        }

        if(player.getCoordinate().getRegionId() == 12395 || player.getCoordinate().getRegionId() == 12396 || player.getCoordinate().getRegionId() == 12651 || player.getCoordinate().getRegionId() == 12652 )
        { SceneObject threshold = SceneObjectQuery.newQuery().name("Threshold").results().nearestTo(player);
            if(VarManager.getVarbitValue(30856) >=400 && killrequired == true)
            {

                println("Threshold value Outside: " + thresholdvalue);
                if(threshold != null && thresholdvalue == 0)
                {
                    thresholdvalue = thresholdvalue + 1;
                    Execution.delay(1000);
                    println("Enter Threshold: " + threshold.interact("Traverse"));
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

       /* if(player.getCoordinate().getRegionId() != 12395 && player.getCoordinate().getRegionId() != 13214 && player.getCoordinate().getRegionId() != 12396 && player.getCoordinate().getRegionId() != 12652 && player.getCoordinate().getRegionId() != 12651 )
        {
            println("Region ID inside instance:" + instanceregionID);
            vandictafight(player);
        }*/

        /*if(instanceregionID == player.getCoordinate().getRegionId())
        {

            vandictafight();
        }
*/




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
        Npc demon = NpcQuery.newQuery().name("Abyssal demon").results().nearest();
        Npc ancientran = NpcQuery.newQuery().name("Ancient ranger").results().nearest();
        Npc ancientwar = NpcQuery.newQuery().name("Ancient warrior").results().nearest();
        Npc ancientmage = NpcQuery.newQuery().name("Ancient mage").results().nearest();
        Npc nechryael = NpcQuery.newQuery().name("Nechryael").results().nearest();

        if(demon !=null || ancientmage !=null || ancientran !=null || ancientwar != null)
        {
            if(player.distanceTo(demon) <10)
            {
                attackNpc(demon);
            }else if (player.distanceTo(ancientmage) <10)
            {
                attackNpc(ancientmage);
            }else if (player.distanceTo(ancientran) <10)
            {
                attackNpc(ancientran);
            }else if (player.distanceTo(ancientwar) <10){
                attackNpc(ancientwar);
            }else if(player.distanceTo(nechryael) <10)
            {
                attackNpc(nechryael);
            }
        }

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

                instanceregionID = player.getCoordinate().getRegionId();

            }
        }
    }

    private void vandictafight()
    {
        Npc vindictap1 = NpcQuery.newQuery().name("Vindicta").results().nearest();
        Npc vindictap2 = NpcQuery.newQuery().name("Gorvek and Vindicta").results().nearest();
        Component resonance = ComponentQuery.newQuery(284).spriteId(14222).results().first();
        SceneObjectQuery firewall = SceneObjectQuery.newQuery();
        firewall.id(101908);
        EntityResultSet<SceneObject> results = firewall.results();


        //println("Region ID inside instance:" + instanceregionID);
        //println("Region ID inside instance Player :" + Client.getLocalPlayer().getCoordinate().getRegionId());
        if(vindictap1 != null)
        {
            meleeprayerswitch();
            println(" Inside the loop where boss detected");
            println("Animation Boss ID: " + vindictap1.getAnimationId());
            attackNpc(vindictap1);
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

            if (!results.isEmpty())
            {
                SceneObject dangerousObject = results.nearest();
                println(" Dangerous object  location P1: " + dangerousObject.getCoordinate());
                println(" Player Coor X location P1: " + Client.getLocalPlayer().getCoordinate());

                //Coordinate safespot = calculateSafeSpot(dangerousObject);
                if (dangerousObject.distanceTo(Client.getLocalPlayer().getCoordinate()) <2) {
                    //Movement.walkTo(player.getCoordinate().getX() +3 , player.getCoordinate().getY() + 2, false);
                   if(calculateSafeSpot(dangerousObject).isReachable() == true)
                    {
                        Movement.walkTo(Client.getLocalPlayer().getCoordinate().getX() +3 , Client.getLocalPlayer().getCoordinate().getY() + 2, false);
                        delay(600);
                        attackNpc(vindictap1);
                        println("Moving to safe spot");
                    }
                    else {
                        Movement.walkTo(Client.getLocalPlayer().getCoordinate().getX() -2  , Client.getLocalPlayer().getCoordinate().getY() -2 , false);
                        delay(600);
                       println("Moving to safe else spot");
                        attackNpc(vindictap1);
                    }

                    //Movement.walkTo(dangerousObject.getCoordinate().getX()+3 , dangerousObject.getCoordinate().getY() +2, false);
                    //println("Moving to safe spot");
                    //attackNpc(vindictap1);

                }
            }

        }
        else if(vindictap2 !=null) {
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
                println(" extra animation Detected");
            }

            if (!results.isEmpty())
            {
                SceneObject dangerousObject = results.nearest();
                println(" Dangerous object location P2 : " + dangerousObject.getCoordinate());
                println(" Client  location P2 : " + Client.getLocalPlayer().getCoordinate());
                //Coordinate safespot = calculateSafeSpot(dangerousObject);
                if (dangerousObject.distanceTo(Client.getLocalPlayer().getCoordinate()) <2) {
                    //Movement.walkTo(player.getCoordinate().getX() +3 , player.getCoordinate().getY() + 2, false);
                    if(calculateSafeSpot(dangerousObject).isReachable() == true)
                    {
                        Movement.walkTo(Client.getLocalPlayer().getCoordinate().getX() +3 , Client.getLocalPlayer().getCoordinate().getY() + 2, false);
                        delay(600);
                        attackNpc(vindictap2);
                        println("Moving to safe spot");
                    }
                    else {
                        Movement.walkTo(Client.getLocalPlayer().getCoordinate().getX() -2  , Client.getLocalPlayer().getCoordinate().getY() -2 , false);
                        delay(600);
                        attackNpc(vindictap2);
                        println("Moving to safe spot");
                    }
                    //Movement.walkTo(dangerousObject.getCoordinate().getX() + 3 , dangerousObject.getCoordinate().getY() +2 , false);
                    //println("Moving to safe spot");
                }
            }
            if(vindictap2.getAnimationId() == 28272 && vindictap2.getCurrentHealth() == 0)
            {
                surgecount = 0;
                thresholdvalue = 0;

                grounditem();



            }
        }

    }

    private Coordinate calculateSafeSpot (SceneObject dangerousObject)
    {
        int currentlocationx = Client.getLocalPlayer().getCoordinate().getX();
        int currentlocationy = Client.getLocalPlayer().getCoordinate().getY();
        return new Coordinate(currentlocationx + 3, currentlocationy + 2, Client.getLocalPlayer().getCoordinate().getZ());
    }

    public void grounditem()
    {
        EntityResultSet<GroundItem> items = GroundItemQuery.newQuery().results();
        if(!items.isEmpty())
        {
            GroundItem loot =  items.random();
            if(loot !=null)
            {
                println("Picked up loot: " + loot.interact("Take"));
                if(LootInventory.isOpen())
                {
                    LootInventory.lootAll();
                }
                else
                {
                    println("Can't find Items to loot");
                }
                Execution.delayUntil(10000,() ->
                {
                    return items.isEmpty();
                });
                ActionBar.useTeleport("War's Retreat Teleport");
            }
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

    public boolean iskillrequired() {
        return killrequired;
    }

    public void setkillrequired(boolean killrequired) {
        this.killrequired = killrequired;
    }
}
