package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.elements.ChunksChangeOperatorElement;
import ca.shadownode.betterchunkloader.sponge.elements.LoaderTypeElement;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandManager {

    private final BetterChunkLoader plugin;

    public CommandManager(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        CommandSpec cmdBalance = CommandSpec.builder()
                .arguments(GenericArguments.optional(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                )
                .executor(new CmdBalance(this.plugin))
                .permission("betterchunkloader.balance")
                .build();

        CommandSpec cmdReload = CommandSpec.builder()
                .arguments(
                        GenericArguments.none()
                )
                .executor(new CmdReload(this.plugin))
                .permission("betterchunkloader.reload")
                .build();

        CommandSpec cmdInfo = CommandSpec.builder()
                .arguments(
                        GenericArguments.none()
                )
                .executor(new CmdInfo(this.plugin))
                .permission("betterchunkloader.info")
                .build();

        CommandSpec cmdChunks = CommandSpec.builder()
                .arguments(new CommandElement[] {
                            new ChunksChangeOperatorElement(Text.of("change")),
                            GenericArguments.string(Text.of("player")),
                            new LoaderTypeElement(Text.of("type")),
                            GenericArguments.integer(Text.of("value"))
                        })
                .executor(new CmdChunks(this.plugin))
                .permission("betterchunkloader.chunks")
                .build();

        CommandSpec cmdList = CommandSpec.builder()
                .arguments(
                        GenericArguments.none()
                )
                .executor(new CmdList(this.plugin))
                .permission("betterchunkloader.list")
                .build();

        CommandSpec cmdDelete = CommandSpec.builder()
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("player")))
                )
                .executor(new CmdDelete(this.plugin))
                .permission("betterchunkloader.delete")
                .build();

        CommandSpec cmdPurge = CommandSpec.builder()
                .executor(new CmdPurge(this.plugin))
                .permission("betterchunkloader.purge")
                .build();

        CommandSpec bclCmdSpec = CommandSpec.builder()
                .child(cmdBalance, new String[]{"balance", "bal"})
                .child(cmdInfo, new String[]{"info", "i"})
                .child(cmdList, new String[]{"list", "ls"})
                .child(cmdChunks, new String[]{"chunks", "c"})
                .child(cmdDelete, new String[]{"delete", "d"})
                .child(cmdPurge, new String[]{"purge"})
                .child(cmdReload, new String[]{"reload"})
                .executor(new CmdBCL(this.plugin))
                .build();
        
        Sponge.getCommandManager().register(this.plugin, bclCmdSpec, "betterchunkloader", "bcl");
    }
}
