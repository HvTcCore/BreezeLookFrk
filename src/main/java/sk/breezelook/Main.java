package sk.breezelook;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

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

    private static final Gson gson = new Gson();
    private static final File points_config = new File(FabricLoader.getInstance().getConfigDir().toFile(), "breezelook/points.json");

    public static LookDirection oldDirection;

    private int tick;

    @Override
    public void onInitializeClient() {
        ModConfig.register();
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
        if (tick > ModConfig.INSTANCE.confirmDelay) {
            if (client.player != null) {
                client.player.setYaw(ModConfig.INSTANCE.confirmHorizontal);
                client.player.setPitch(ModConfig.INSTANCE.confirmVertical);
                if (ModConfig.INSTANCE.returnCamera && (tick > ModConfig.INSTANCE.confirmDelay + ModConfig.INSTANCE.returnCameraDelay)) {
                    if (ModConfig.INSTANCE.returnCameraToDirection) {
                        client.player.setYaw(ModConfig.INSTANCE.returnCameraHorizontal);
                        client.player.setPitch(ModConfig.INSTANCE.returnCameraVertical);
                    }
                    else {
                        client.player.setYaw(oldDirection.horizontal);
                        client.player.setPitch(oldDirection.vertical);
                    }
                    Command.commandUsed = false;
                    tick = 0;
                }
                else if (!ModConfig.INSTANCE.returnCamera)
                {
                    Command.commandUsed = false;
                    tick = 0;
                }
            }
        }
    }

    public record LookDirection(float horizontal, float vertical) {}
}
