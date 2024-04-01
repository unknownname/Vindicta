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

        if (ImGui.Begin("Vindicta Script Beta v1", ImGuiWindowFlag.None.getValue())) {

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
                if (ImGui.Selectable("Stats", false, 50)) {
                    // Handle instructions selection
                    script.selectedoption = "Stats";
                }
                ImGui.Separator();
                if (ImGui.Selectable("Options", false, 50)) {
                    // Handle debug selection
                    script.selectedoption = "Options";
                }
                ImGui.Separator();
                ImGui.Spacing(5f, 5f);
                ImGui.Text("Number of Kills: " + script.vindictadeathcounter);
                //ImGui.Spacing();
                ImGui.Separator();
                killperhour();
                //ImGui.Text("Kill Per Hour: " + killperhour());
                String displayTimeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                ImGui.Separator();
                ImGui.Text("Time Running:  " + displayTimeRunning);
                ImGui.Separator();
                ImGui.Text("Instance Timer in mins: " + script.GetInstanceTimeLeft());

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
                    script.numberofkills = ImGui.InputInt("Set Health Value", script.numberofkills);
                    script.setEatFood(ImGui.Checkbox("Eat Food",script.isEatFood()));
                    //script.prayerrestore();
                    script.setdarknessenable(ImGui.Checkbox("Darkness",script.isdarknessenable()));
                    script.setinvokedeath(ImGui.Checkbox("Invoke Death",script.isinvokedeath()));
                    script.setdeathessence(ImGui.Checkbox("Death Essence",script.isdeathessence()));
                    script.setessenceoff(ImGui.Checkbox("Essence of Finality",script.isessenceoff()));

                } else if ("Stats".equals(script.selectedoption)) {
                    ImGui.Text("Settings for Option 2");
                    script.setkillrequired(ImGui.Checkbox("Kill Required for Portal", script.iskillrequired()));


                } else if ("Options".equals(script.selectedoption)) {
                    ImGui.Text("Options");
                    script.setExcalibur(ImGui.Checkbox("Excalibur", script.isExcalibur()));
                    script.setscriptures(ImGui.Checkbox("GW3 Scriptures ", script.isscriptures()));

                }

                ImGui.Columns(1, "Mixed", false);
                ImGui.End();
            }
        }
    }


    public void killperhour()
    {
        long currnettime = (System.currentTimeMillis() - scriptstartTime) /1000;
        int killperhour = (int)Math.round(3600.0/currnettime * script.vindictadeathcounter);
        ImGui.Text("Kills Per Hour: " + killperhour);
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
