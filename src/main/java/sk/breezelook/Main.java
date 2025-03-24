package sk.breezelook;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import oshi.jna.platform.mac.SystemB;
import sk.breezelook.config.CommonConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Main implements ClientModInitializer {
    public static final String MOD_ID = "breezelook";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Map<String, LookDirection> points = new HashMap<>();

    public static final CommonConfig config = new CommonConfig();
    private static final Gson gson = new Gson();
    private static final File points_config = new File(FabricLoader.getInstance().getConfigDir().toFile(), "breezelook/points.json");

    public static LookDirection oldDirection;

    private int tick;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(Command::register);
        ClientLifecycleEvents.CLIENT_STARTED.register(this::clientStarted);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void clientStarted(MinecraftClient client) {
        loadPoints();
    }

    public static void loadPoints() {
        if (points_config.exists()) {
            try (FileReader reader = new FileReader(points_config)) {
                Type type = new TypeToken<Map<String, LookDirection>>() {}.getType();
                Map<String, LookDirection> loadedPoints = gson.fromJson(reader, type);
                points = loadedPoints;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void savePoints() {
        try (FileWriter writer = new FileWriter(points_config)) {
            gson.toJson(points, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tick(MinecraftClient client) {
        if (Command.commandUsed) tick++;
        if (tick > config.model.confirmDelay) {
            if (client.player != null) {
                client.player.setYaw(config.model.confirmHorizontal);
                client.player.setPitch(config.model.confirmVertical);
                if (config.model.returnCamera && (tick > config.model.confirmDelay + config.model.returnCameraDelay)) {
                    if (config.model.returnCameraToDirection) {
                        client.player.setYaw(config.model.returnCameraToHorizontal);
                        client.player.setPitch(config.model.returnCameraToVertical);
                    }
                    else {
                        client.player.setYaw(oldDirection.horizontal);
                        client.player.setPitch(oldDirection.vertical);
                    }
                    Command.commandUsed = false;
                    tick = 0;
                }
                else if (!config.model.returnCamera)
                {
                    Command.commandUsed = false;
                    tick = 0;
                }
            }
        }
    }

    public record LookDirection(float horizontal, float vertical) {}
}
