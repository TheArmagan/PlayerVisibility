package dev.xnasuni.playervisibility;

import com.mojang.brigadier.CommandDispatcher;
import dev.xnasuni.playervisibility.commands.VisibilityCommand;
import dev.xnasuni.playervisibility.config.ModConfig;
import dev.xnasuni.playervisibility.types.FilterType;
import dev.xnasuni.playervisibility.util.ArrayListUtil;
import dev.xnasuni.playervisibility.util.ConfigUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlayerVisibility implements ModInitializer {
    public static final String ModID = "player-visibility";
    public static final String DisplayModID = "PlayerVisibility";

    public static final Logger LOGGER = LoggerFactory.getLogger(DisplayModID);
    public static MinecraftClient minecraftClient;

    private static boolean filterEnabled = true;

    private static String[] filteredPlayers;
    private static FilterType filterType;

    private static KeyBinding toggleVisibility;

    public static Path configDirectory;

    public static void setFilterType(FilterType filterType) {
        PlayerVisibility.filterType = filterType;
    }

    @Override
    public void onInitialize() {
        configDirectory = FabricLoader.getInstance().getConfigDir().resolve("player-visibility");
        try {
            Files.createDirectories(configDirectory);
            filteredPlayers = ConfigUtil.getFilteredPlayers();
        } catch (IOException e) {
            filteredPlayers = new String[]{};
            LOGGER.warn("Could not create directory, defaulting to empty list.");
        }

        try {
            filterType = ConfigUtil.getFilterType();
        } catch (IOException e) {
            filterType = FilterType.WHITELIST;
            LOGGER.warn("Could not read filter type, defaulting to whitelist.");
        }

        ModConfig.init();

        minecraftClient = MinecraftClient.getInstance();

        toggleVisibility = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.player-visibility.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.player-visibility.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleVisibility.wasPressed()) {
                toggleFilter();
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try {
                ConfigUtil.save(filteredPlayers, filterType);
            } catch (IOException e) {
                LOGGER.error("Failed to save config.", e);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register(PlayerVisibility::RegisterCommands);

        LOGGER.info("Player Visibility Initialized");
    }

    private static void RegisterCommands(CommandDispatcher<FabricClientCommandSource> Dispatcher, CommandRegistryAccess Registry) {
        VisibilityCommand.Register(Dispatcher);
    }

    public static void toggleFilter() {
        filterEnabled = !filterEnabled;
        minecraftClient.player.sendMessage(Text.of(String.format("§%cPlayer Visibility§f filter is now §f%s§f (§f%s§f)", ModConfig.INSTANCE.MainColor.GetChar(), filterEnabled ? "§aon" : "§coff", filterType.toString())), true);
    }

    public static boolean isFilterEnabled() {
        return filterEnabled;
    }

    public static String[] getFilteredPlayers() {
        return filteredPlayers;
    }

    public static FilterType getFilterType() {
        return filterType;
    }

    public static void addPlayerToFilter(String Username) {
        String CasedName = ArrayListUtil.GetCase(filteredPlayers, Username);

        if (Username.equalsIgnoreCase(minecraftClient.player.getName().getString())) {
            minecraftClient.player.sendMessage(Text.of("§cYou can't filter yourself!"), true);
            return;
        }

        if (ArrayListUtil.ContainsLowercase(filteredPlayers, Username)) {
            minecraftClient.player.sendMessage(Text.of(String.format("§f '%s'§c is already filtered!", CasedName)), true);
            return;
        }

        filteredPlayers = ArrayListUtil.AddStringToArray(filteredPlayers, Username);
        ModConfig.filteredPlayers = PlayerVisibility.getFilteredPlayers();
        minecraftClient.player.sendMessage(Text.of(String.format("§aAdded §f'%s'§a to the filter.", CasedName)), true);
    }

    public static void removePlayerFromFilter(String Username) {
        String CasedName = ArrayListUtil.GetCase(filteredPlayers, Username);

        if (Username.equalsIgnoreCase(minecraftClient.player.getName().getString())) {
            minecraftClient.player.sendMessage(Text.of("§cYou can't unfilter yourself!"), true);
            return;
        }

        if (!ArrayListUtil.ContainsLowercase(filteredPlayers, Username)) {
            minecraftClient.player.sendMessage(Text.of(String.format("§f'%s'§c is not filtered!", CasedName)), true);
            return;
        }

        filteredPlayers = ArrayListUtil.RemoveStringToArray(filteredPlayers, CasedName);
        ModConfig.filteredPlayers = PlayerVisibility.getFilteredPlayers();
        minecraftClient.player.sendMessage(Text.of(String.format("§aRemoved §f'%s'§a from the filter.", CasedName)), true);
    }

    public static void clearFilter() {
        int SizeBeforeClear = filteredPlayers.length;

        if (SizeBeforeClear == 0) {
            minecraftClient.player.sendMessage(Text.of(String.format("§cThe filter is already empty §f(§c%s§f)", SizeBeforeClear)), true);
            return;
        }

        filteredPlayers = new String[]{};
        ModConfig.filteredPlayers = PlayerVisibility.getFilteredPlayers();
        minecraftClient.player.sendMessage(Text.of(String.format("§aCleared the filter §f(§a%s§f)", SizeBeforeClear)), true);

    }

}
