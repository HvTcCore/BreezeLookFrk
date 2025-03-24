package sk.breezelook;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static sk.breezelook.Main.savePoints;

public class Command {
    public static boolean commandUsed;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("look")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .then(ClientCommandManager.argument("horizontal", FloatArgumentType.floatArg())
                                        .then(ClientCommandManager.argument("vertical", FloatArgumentType.floatArg())
                                                .executes(Command::addCommand)
                                        ))))
                .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .suggests(new PointsSuggestionProvider())
                                .executes(Command::removeCommand)
                        ))
                .then(ClientCommandManager.literal("warp")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .suggests(new PointsSuggestionProvider())
                                .executes(Command::tpCommand)
                        ))
                .then(ClientCommandManager.literal("reload")
                        .executes(Command::reloadCommand)
                )
        );
    }

    private static int reloadCommand(CommandContext<FabricClientCommandSource> context) {
        Main.config.readConfig();
        savePoints();
        context.getSource().sendFeedback(Text.translatable("breezelook.reload"));
        return 1;
    }

    private static int addCommand(CommandContext<FabricClientCommandSource> context)
    {
        String pointName = StringArgumentType.getString(context, "name");
        float h = FloatArgumentType.getFloat(context, "horizontal");
        float v = FloatArgumentType.getFloat(context, "vertical");
        Main.points.put(pointName, new Main.LookDirection(h,v));
        savePoints();
        context.getSource().sendFeedback(Text.translatable("breezelook.add", pointName));
        return 1;
    }

    private static int removeCommand(CommandContext<FabricClientCommandSource> context)
    {
        String pointName = StringArgumentType.getString(context, "name");
        Main.points.remove(pointName);
        savePoints();
        context.getSource().sendFeedback(Text.translatable("breezelook.remove", pointName));
        return 1;
    }

    private static int tpCommand(CommandContext<FabricClientCommandSource> context)
    {
        String pointName = StringArgumentType.getString(context, "name");
        Main.LookDirection direction = Main.points.get(pointName);
        Main.oldDirection = new Main.LookDirection(context.getSource().getClient().player.getYaw(), context.getSource().getClient().player.getPitch());
        context.getSource().getClient().player.setYaw(direction.horizontal());
        context.getSource().getClient().player.setPitch(direction.vertical());
        commandUsed = true;
        return 1;
    }
}
