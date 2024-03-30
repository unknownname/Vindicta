package net.botwithus;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class VindictaGraphicsContext extends ScriptGraphicsContext {

    private Vindicta script;
    private long scriptstartTime;

    int myValue = 0;

    public VindictaGraphicsContext(ScriptConsole scriptConsole, Vindicta script) {
        super(scriptConsole);
        this.script = script;
        this.scriptstartTime = System.currentTimeMillis();
    }

    @Override
    public void drawSettings() {

        ImGui.SetWindowSize(600f,400f);

        if (ImGui.Begin("Vindicta Script", ImGuiWindowFlag.None.getValue())) {

            long elapsedTimeMillis = System.currentTimeMillis() - scriptstartTime;
            long elapsedSeconds = elapsedTimeMillis / 1000;
            long hours = elapsedSeconds / 3600;
            long minutes = (elapsedSeconds % 3600) / 60;
            long seconds = elapsedSeconds % 60;


            ImGui.Columns(2, "Mixed", false);
            ImGui.SetColumnWidth(0, 150);
            // Sidebar
            if (ImGui.BeginChild("Sidebar", 150, -1, true, ImGuiWindowFlag.None.getValue())) {
                if (ImGui.Selectable("Setting", false, 50)) {
                    script.selectedoption = "Setting";

                }
                ImGui.Separator();
                if (ImGui.Selectable("Stats", true, 50)) {
                    // Handle instructions selection
                    script.selectedoption = "Stats";
                }
                ImGui.Separator();
                if (ImGui.Selectable("Debug", false, 50)) {
                    // Handle debug selection
                    script.selectedoption = "Debug";
                }
                ImGui.Separator();
                ImGui.Spacing(5f, 5f);
                ImGui.Text("Number of Kills: " + script.vindictadeathcounter);
                //ImGui.Spacing();
                ImGui.Separator();
                ImGui.Text("Kill Per Hour: " + script.killperhour);
                String displayTimeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                ImGui.Separator();
                ImGui.Text("Time Running:  " + displayTimeRunning);
                ImGui.Spacing(5f, 5f);
                ImGui.Spacing(5f, 5f);
                ImGui.Spacing(5f, 5f);
                ImGui.Spacing(5f, 5f);
                ImGui.Separator();
                ImGui.Text("Status: " + script.getBotState());

                ImGui.EndChild();


                ImGui.NextColumn();
                if ("Setting".equals(script.selectedoption)) {
                    if (ImGui.Button("Start")) {
                        script.setBotState(Vindicta.BotState.FIGHTING);
                    }
                    ImGui.SameLine();
                    if (ImGui.Button("Stop")) {
                        script.setBotState(Vindicta.BotState.IDLE);
                    }

                    script.setuseStatBoostingPotion(ImGui.Checkbox("Use Stat Boosting Potion", script.isuseStatBoostingPotion()));

                    script.presentNumber = ImGui.InputInt("Preset Number", script.presentNumber);  //numberofkills
                    script.numberofkills = ImGui.InputInt("Kills before Banking", script.numberofkills);
                    script.setEatFood(ImGui.Checkbox("Eat Food",script.isEatFood()));
                    //script.prayerrestore();
                    script.setdarknessenable(ImGui.Checkbox("Darkness",script.isdarknessenable()));
                    script.setinvokedeath(ImGui.Checkbox("Invoke Death",script.isinvokedeath()));
                    script.setdeathessence(ImGui.Checkbox("Death Essence",script.isdeathessence()));
                    script.setessenceoff(ImGui.Checkbox("Essence of Finality",script.isessenceoff()));

                } else if ("Stats".equals(script.selectedoption)) {
                    ImGui.Text("Settings for Option 2");
                    script.setkillrequired(ImGui.Checkbox("Kill Required for Portal", script.iskillrequired()));


                } else if ("Debug".equals(script.selectedoption)) {
                    ImGui.Text("Debug");
                }

                ImGui.Columns(1, "Mixed", false);
        /*if (ImGui.Begin("Vindicta", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Welcome to Vindicta Script");
                    ImGui.Text("My scripts state is: " + script.getBotState());
                    if (ImGui.Button("Start")) {
                        //button has been clicked
                        script.setBotState(Vindicta.BotState.FIGHTING);
                    }
                    ImGui.SameLine();
                    if (ImGui.Button("Stop")) {
                        //has been clicked
                        script.setBotState(Vindicta.BotState.IDLE);
                    }
                    ImGui.EndTabItem();
                }
                if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                    script.setkillrequired(ImGui.Checkbox("Kill Required for Portal", script.iskillrequired()));
                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }*/
                ImGui.End();
            }
        }
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
