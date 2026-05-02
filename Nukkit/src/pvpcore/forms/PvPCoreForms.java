package pvpcore.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.simple.ButtonImage;
import cn.nukkit.form.response.CustomResponse;
import cn.nukkit.form.window.CustomForm;
import cn.nukkit.form.window.Form;
import cn.nukkit.form.window.SimpleForm;
import cn.nukkit.utils.TextFormat;
import pvpcore.PvPCore;
import pvpcore.player.PvPCorePlayer;
import pvpcore.utils.PvPCKnockback;
import pvpcore.utils.Utils;
import pvpcore.worlds.PvPCWorld;
import pvpcore.worlds.areas.PvPCArea;

import java.util.ArrayList;
import java.util.HashMap;

public final class PvPCoreForms {

    private static final ButtonImage DEV_ICON = ButtonImage.Type.PATH.of("textures/ui/dev_glyph_color.png");
    private static final ButtonImage DEBUG_ICON = ButtonImage.Type.PATH.of("textures/ui/debug_glyph_color.png");
    private static final ButtonImage VIEW_ICON = ButtonImage.Type.PATH.of("textures/ui/magnifyingGlass.png");
    private static final ButtonImage ADD_ICON = ButtonImage.Type.PATH.of("textures/ui/color_plus.png");
    private static final ButtonImage DELETE_ICON = ButtonImage.Type.PATH.of("textures/ui/realms_red_x.png");
    private static final ButtonImage NONE_ICON = ButtonImage.Type.PATH.of("textures/ui/redX1.png");
    private static final ButtonImage WORLD_ICON = ButtonImage.Type.PATH.of("textures/ui/op.png");
    private static final ButtonImage AREA_ICON = ButtonImage.Type.PATH.of("textures/ui/deop.png");
    private static final ButtonImage YES_ICON = ButtonImage.Type.PATH.of("textures/ui/check.png");
    private static final ButtonImage NO_ICON = ButtonImage.Type.PATH.of("textures/ui/cancel.png");

    private PvPCoreForms() {
    }

    public static SimpleForm getPvPCoreMenu(Player player) {
        return new SimpleForm(TextFormat.BOLD + "PvPCore Menu", "The menu used to configure the PvPCore plugin.")
                .addButton("Configure Worlds", DEV_ICON, responsePlayer -> getWorldsMenu(responsePlayer).send(responsePlayer))
                .addButton("Configure Areas", DEV_ICON, responsePlayer -> getAreasMenu(responsePlayer).send(responsePlayer));
    }

    public static SimpleForm getWorldsMenu(Player player) {
        return new SimpleForm("Worlds Configuration", "The worlds configuration menu.")
                .addButton("Edit World Knockback Settings", DEBUG_ICON, responsePlayer -> getWorldSelectorForm(responsePlayer, false).send(responsePlayer))
                .addButton("View World Knockback Settings", VIEW_ICON, responsePlayer -> getWorldSelectorForm(responsePlayer, true).send(responsePlayer))
                .addButton("Go Back", responsePlayer -> getPvPCoreMenu(responsePlayer).send(responsePlayer));
    }

    public static SimpleForm getAreasMenu(Player player) {
        return new SimpleForm("Areas Configuration", "The areas configuration menu.")
                .addButton("Edit Area Knockback Settings", DEBUG_ICON, responsePlayer -> getAreaSelectorMenu(responsePlayer, Utils.ACTION_EDIT_AREA).send(responsePlayer))
                .addButton("View Area Knockback Settings", VIEW_ICON, responsePlayer -> getAreaSelectorMenu(responsePlayer, Utils.ACTION_VIEW_AREA).send(responsePlayer))
                .addButton("Create New Area", ADD_ICON, responsePlayer -> getCreateAreaForm(responsePlayer).send(responsePlayer))
                .addButton("Delete Existing Area", DELETE_ICON, responsePlayer -> getAreaSelectorMenu(responsePlayer, Utils.ACTION_DELETE_AREA).send(responsePlayer))
                .addButton("Go Back", responsePlayer -> getPvPCoreMenu(responsePlayer).send(responsePlayer));
    }

    public static SimpleForm getWorldSelectorForm(Player player, boolean viewInfo) {
        SimpleForm form = new SimpleForm("Select World", "Select the world that you want to view/configure the knockback for.");
        ArrayList<PvPCWorld> worlds = PvPCore.getWorldHandler().getWorlds();
        if (worlds.isEmpty()) {
            form.addButton("None", NONE_ICON);
            return form;
        }

        for (PvPCWorld world : worlds) {
            form.addButton(world.getLevelName(), WORLD_ICON, responsePlayer -> getWorldMenu(responsePlayer, world, viewInfo).send(responsePlayer));
        }
        return form;
    }

    public static SimpleForm getAreaSelectorMenu(Player player, int type) {
        int normalizedType = type % 3;
        String description = switch (normalizedType) {
            case Utils.ACTION_DELETE_AREA -> "Select the area that you want to delete.";
            case Utils.ACTION_EDIT_AREA -> "Select the area that you want to edit.";
            case Utils.ACTION_VIEW_AREA -> "Select the area that you want to view.";
            default -> "Select an area.";
        };

        SimpleForm form = new SimpleForm("Select Area", description);
        ArrayList<PvPCArea> areas = PvPCore.getAreaHandler().getAreas();
        if (areas.isEmpty()) {
            form.addButton("None", NONE_ICON);
            return form;
        }

        for (PvPCArea area : areas) {
            form.addButton(area.getName(), AREA_ICON, responsePlayer -> {
                Form<?> next = switch (normalizedType) {
                    case Utils.ACTION_EDIT_AREA, Utils.ACTION_VIEW_AREA -> getAreaMenu(responsePlayer, area, normalizedType);
                    case Utils.ACTION_DELETE_AREA -> getDeleteMenu(responsePlayer, area);
                    default -> null;
                };
                if (next != null) {
                    next.send(responsePlayer);
                }
            });
        }
        return form;
    }

    public static CustomForm getWorldMenu(Player player, PvPCWorld world, boolean view) {
        String title = view ? "World Information" : "Edit World Configuration";
        String desc = view ? "Displays the knockback information of the world." : "Edit the knockback configuration of the world.";
        PvPCKnockback knockback = world.getKnockback();
        CustomForm form = new CustomForm(title).addLabel(desc);

        if (view) {
            return form.addLabel(TextFormat.WHITE + "World Name: " + world.getLevelName())
                    .addLabel(TextFormat.WHITE + "Knockback-Enabled: " + world.isKBEnabled())
                    .addLabel(TextFormat.WHITE + "Horizontal (X) Knockback: " + knockback.getHorizontalKB())
                    .addLabel(TextFormat.WHITE + "Vertical (Y) Knockback: " + knockback.getVerticalKB())
                    .addLabel(TextFormat.WHITE + "Attack Delay: " + knockback.getAttackDelay())
                    .onSubmit((responsePlayer, response) -> getWorldSelectorForm(responsePlayer, true).send(responsePlayer));
        }

        return form.addLabel(TextFormat.WHITE + "World Name: " + world.getLevelName())
                .addToggle(TextFormat.WHITE + "Knockback-Enabled", world.isKBEnabled())
                .addInput(TextFormat.WHITE + "Horizontal (X) Knockback: ", "Default = 0.4", Float.toString(knockback.getHorizontalKB()))
                .addInput(TextFormat.WHITE + "Vertical (Y) Knockback: ", "Default = 0.4", Float.toString(knockback.getVerticalKB()))
                .addInput(TextFormat.WHITE + "Attack Delay: ", "Default = 10", Integer.toString(knockback.getAttackDelay()))
                .onSubmit((responsePlayer, response) -> updateWorldKnockback(responsePlayer, world, response));
    }

    public static CustomForm getAreaMenu(Player player, PvPCArea area, int type) {
        String title = type == Utils.ACTION_VIEW_AREA ? "Area Information" : "Edit Area Configuration";
        String description = type == Utils.ACTION_VIEW_AREA ? "Displays the knockback information of the area." : "Edit the knockback configuration of the area.";
        PvPCKnockback knockback = area.getKnockback();
        CustomForm form = new CustomForm(title).addLabel(description);

        if (type == Utils.ACTION_VIEW_AREA) {
            return form.addLabel(TextFormat.WHITE + "Area Name: " + area.getName())
                    .addLabel(TextFormat.WHITE + "Knockback-Enabled: " + area.isEnabled())
                    .addLabel(TextFormat.WHITE + "Horizontal (X) Knockback: " + knockback.getHorizontalKB())
                    .addLabel(TextFormat.WHITE + "Vertical (Y) Knockback: " + knockback.getVerticalKB())
                    .addLabel(TextFormat.WHITE + "Attack Delay: " + knockback.getAttackDelay())
                    .onSubmit((responsePlayer, response) -> getAreaSelectorMenu(responsePlayer, type).send(responsePlayer));
        }

        return form.addLabel(TextFormat.WHITE + "Area Name: " + area.getName())
                .addToggle(TextFormat.WHITE + "Knockback-Enabled", area.isEnabled())
                .addInput(TextFormat.WHITE + "Horizontal (X) Knockback: ", "Default = 0.4", Float.toString(knockback.getHorizontalKB()))
                .addInput(TextFormat.WHITE + "Vertical (Y) Knockback: ", "Default = 0.4", Float.toString(knockback.getVerticalKB()))
                .addInput(TextFormat.WHITE + "Attack Delay: ", "Default = 10", Integer.toString(knockback.getAttackDelay()))
                .onSubmit((responsePlayer, response) -> updateAreaKnockback(responsePlayer, area, response));
    }

    public static CustomForm getCreateAreaForm(Player player) {
        if (!(player instanceof PvPCorePlayer pvpPlayer)) {
            return getPvPAreaHelpForm(player);
        }

        HashMap<String, Object> areaInfo = pvpPlayer.getAreaInfo();
        if (!areaInfo.containsKey("firstPos") || !areaInfo.containsKey("secondPos")) {
            return getPvPAreaHelpForm(player);
        }

        return new CustomForm("Create New Area")
                .addInput("Provide the name of the area that you want to create: ")
                .onSubmit((responsePlayer, response) -> {
                    if (!(responsePlayer instanceof PvPCorePlayer responsePvpPlayer)) {
                        responsePlayer.sendMessage(Utils.getPrefix() + TextFormat.RED + " Internal plugin error. Please rejoin before creating an area.");
                        return;
                    }

                    String name = response.getInputResponse(0);
                    if (name == null || name.trim().isEmpty()) {
                        responsePlayer.sendMessage(Utils.getPrefix() + TextFormat.RED + " Invalid area name.");
                        return;
                    }
                    responsePvpPlayer.createArea(name.trim());
                });
    }

    public static CustomForm getPvPAreaHelpForm(Player player) {
        CustomForm form = new CustomForm("PvPArea Creation Help")
                .addLabel("To create a new PvPArea, you must provide the following things:\n"
                        + "  => The First Position Boundary of the Area\n"
                        + "  => The Second Position Boundary of the Area\n"
                        + "  => The name of the PvPArea.")
                .addLabel("To set the first position boundary of the area, type: " + TextFormat.AQUA + "/pvparea pos1")
                .addLabel("To set the second position boundary of the area, type: " + TextFormat.AQUA + "/pvparea pos2")
                .addLabel("You provide the name of the PvPArea upon creation in the PvPArea menu.")
                .addLabel("You MUST provide the first position AND second position boundary before you create the PvPArea in the form menu.");

        if (!(player instanceof PvPCorePlayer pvpPlayer)) {
            return form;
        }

        HashMap<String, Object> areaInfo = pvpPlayer.getAreaInfo();
        ArrayList<String> missing = new ArrayList<>();
        if (!areaInfo.containsKey("firstPos")) {
            missing.add(" => The First Position Boundary");
        }
        if (!areaInfo.containsKey("secondPos")) {
            missing.add(" => The Second Position Boundary");
        }

        if (missing.isEmpty()) {
            form.addLabel(TextFormat.GREEN + "You have successfully provided all of the necessary information.");
        } else {
            form.addLabel(TextFormat.RED + "You still need to set the following information before you can create a PvPArea:" + TextFormat.WHITE + "\n" + String.join("\n", missing));
        }
        return form;
    }

    public static SimpleForm getDeleteMenu(Player player, PvPCArea area) {
        return new SimpleForm("Delete Area", "Are you sure you want to delete the PvPArea? If you accept, you can't undo this action. Select 'Yes' if you want to delete the area, or 'No' if you don't want to delete the area.")
                .addButton("Yes", YES_ICON, responsePlayer -> {
                    PvPCArea currentArea = PvPCore.getAreaHandler().getArea(area.getName());
                    if (currentArea == null) {
                        responsePlayer.sendMessage(Utils.getPrefix() + TextFormat.RED + " That area no longer exists.");
                        return;
                    }
                    PvPCore.getAreaHandler().deleteArea(currentArea);
                    responsePlayer.sendMessage(Utils.getPrefix() + TextFormat.RED + " You have successfully deleted the area.");
                })
                .addButton("No", NO_ICON, responsePlayer -> getAreaSelectorMenu(responsePlayer, Utils.ACTION_DELETE_AREA).send(responsePlayer));
    }

    private static void updateWorldKnockback(Player player, PvPCWorld world, CustomResponse response) {
        PvPCWorld currentWorld = PvPCore.getWorldHandler().getWorld(world.getLevelName());
        if (currentWorld == null) {
            player.sendMessage(Utils.getPrefix() + TextFormat.RED + " Failed to find that world.");
            return;
        }
        updateKnockback(player, currentWorld.getKnockback(), response, currentWorld.isKBEnabled(), currentWorld::setKBEnabled, "world");
    }

    private static void updateAreaKnockback(Player player, PvPCArea area, CustomResponse response) {
        PvPCArea currentArea = PvPCore.getAreaHandler().getArea(area.getName());
        if (currentArea == null) {
            player.sendMessage(Utils.getPrefix() + TextFormat.RED + " Failed to find that area.");
            return;
        }
        updateKnockback(player, currentArea.getKnockback(), response, currentArea.isEnabled(), currentArea::setEnabled, "area");
    }

    private static void updateKnockback(Player player, PvPCKnockback knockback, CustomResponse response, boolean previousEnabled, BooleanSetter enabledSetter, String targetName) {
        int previousSpeed = knockback.getAttackDelay();
        float previousHorizontal = knockback.getHorizontalKB();
        float previousVertical = knockback.getVerticalKB();

        try {
            enabledSetter.set(response.getToggleResponse(2));
            knockback.update(PvPCKnockback.HORIZONTAL_KB, Float.parseFloat(response.getInputResponse(3)));
            knockback.update(PvPCKnockback.VERTICAL_KB, Float.parseFloat(response.getInputResponse(4)));
            knockback.update(PvPCKnockback.SPEED_KB, Integer.parseInt(response.getInputResponse(5)));
        } catch (RuntimeException exception) {
            enabledSetter.set(previousEnabled);
            knockback.update(PvPCKnockback.HORIZONTAL_KB, previousHorizontal);
            knockback.update(PvPCKnockback.VERTICAL_KB, previousVertical);
            knockback.update(PvPCKnockback.SPEED_KB, previousSpeed);
            player.sendMessage(Utils.getPrefix() + TextFormat.RED + " Failed to update the kb of the " + targetName + ".");
            return;
        }

        player.sendMessage(Utils.getPrefix() + TextFormat.GREEN + " The kb has been successfully updated.");
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(boolean value);
    }
}
