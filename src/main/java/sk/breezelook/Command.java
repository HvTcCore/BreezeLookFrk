package sk.breezelook;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import sk.breezelook.utills.PointsSuggestionProvider;
import sk.breezelook.Main.LookDirection;

import static sk.breezelook.Main.*;

public class Command {
    public static boolean commandUsed;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("look")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .then(ClientCommandManager.argument("horizontal", FloatArgumentType.floatArg())
                                        .then(ClientCommandManager.argument("vertical", FloatArgumentType.floatArg())
                                                .executes(Command::addCommand)
                                        )
                                )
                        )
                )
                .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .suggests(new PointsSuggestionProvider())
                                .executes(Command::removeCommand)
                        )
                )
                .then(ClientCommandManager.literal("warp")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .suggests(new PointsSuggestionProvider())
                                .executes(Command::warpCommand)
                        )
                )
                .then(ClientCommandManager.literal("config")
                        .then(ClientCommandManager.literal("reload")
                            .executes(Command::reloadCommand)
                        )
                        .then(ClientCommandManager.literal("addchamber")
                                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .suggests(new PointsSuggestionProvider())
                                        .then(ClientCommandManager.argument("horizontal", FloatArgumentType.floatArg())
                                                .then(ClientCommandManager.argument("vertical", FloatArgumentType.floatArg())
                                                        .executes(Command::addChamberCommand)
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int reloadCommand(CommandContext<FabricClientCommandSource> context) {
        ModConfig.readConfig();
        savePoints();
        context.getSource().sendFeedback(Text.translatable("breezelook.reload"));
        return 1;
    }

    private static int addCommand(CommandContext<FabricClientCommandSource> context)
    {
        String pointName = StringArgumentType.getString(context, "name");
        float h = FloatArgumentType.getFloat(context, "horizontal");
        float v = FloatArgumentType.getFloat(context, "vertical");
        points.put(pointName, new LookDirection(h,v, 0, 0));
        savePoints();
        context.getSource().sendFeedback(Text.translatable("breezelook.add", pointName));
        return 1;
    }

    private static int removeCommand(CommandContext<FabricClientCommandSource> context)
    {
        String pointName = StringArgumentType.getString(context, "name");
        points.remove(pointName);
        savePoints();
        context.getSource().sendFeedback(Text.translatable("breezelook.remove", pointName));
        return 1;
    }

    private static int warpCommand(CommandContext<FabricClientCommandSource> context)
    {
        String pointName = StringArgumentType.getString(context, "name");
        if (!points.containsKey(pointName)) return 1;
        lookDirection = points.get(pointName);
        context.getSource().getClient().player.setYaw(lookDirection.horizontal());
        context.getSource().getClient().player.setPitch(lookDirection.vertical());
        if (ModConfig.INSTANCE.confirm) commandUsed = true;
        return 1;
    }

    private static int addChamberCommand(CommandContext<FabricClientCommandSource> context) {
        String pointName = StringArgumentType.getString(context, "name");
        float h = FloatArgumentType.getFloat(context, "horizontal");
        float v = FloatArgumentType.getFloat(context, "vertical");
        if (!points.containsKey(pointName)) return 1;
        points.put(pointName, new LookDirection(points.get(pointName).horizontal(), points.get(pointName).vertical(), h, v));
        savePoints();
        context.getSource().sendFeedback(Text.translatable("breezelook.add_chamber"));
        return 1;
    }
}
