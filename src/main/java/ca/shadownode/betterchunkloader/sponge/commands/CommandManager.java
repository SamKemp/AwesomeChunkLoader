package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.elements.ChunksChangeOperatorElement;
import ca.shadownode.betterchunkloader.sponge.elements.LoaderTypeElement;
import ca.shadownode.betterchunkloader.sponge.elements.ReloadTypeElement;
import org.spongepowered.api.Sponge;
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
                .permission("betterchunkloader.commands.balance.base")
                .build();

        CommandSpec cmdReload = CommandSpec.builder()
                .arguments(
                        GenericArguments.optional(new ReloadTypeElement(Text.of("type")))
                )
                .executor(new CmdReload(this.plugin))
                .permission("betterchunkloader.commands.reload.base")
                .build();

        CommandSpec cmdInfo = CommandSpec.builder()
                .arguments(
                        GenericArguments.none()
                )
                .executor(new CmdInfo(this.plugin))
                .permission("betterchunkloader.commands.info.base")
                .build();

        CommandSpec cmdChunks = CommandSpec.builder()
                .arguments(
                        new ChunksChangeOperatorElement(Text.of("change")),
                        GenericArguments.string(Text.of("player")),
                        new LoaderTypeElement(Text.of("type")),
                        GenericArguments.integer(Text.of("value"))
                )
                .executor(new CmdChunks(this.plugin))
                .permission("betterchunkloader.commands.chunks.base")
                .build();
        CommandSpec cmdList = CommandSpec.builder()
                .arguments(
                        new LoaderTypeElement(Text.of("type")),
                        GenericArguments.optional(GenericArguments.string(Text.of("player")))
                )
                .executor(new CmdList(this.plugin))
                .permission("betterchunkloader.commands.list.base")
                .build();
        CommandSpec cmdDelete = CommandSpec.builder()
                .arguments(
                        new LoaderTypeElement(Text.of("type")),
                        GenericArguments.optional(GenericArguments.string(Text.of("player")))
                )
                .executor(new CmdDelete(this.plugin))
                .permission("betterchunkloader.commands.delete.base")
                .build();

        CommandSpec cmdPurge = CommandSpec.builder()
                .executor(new CmdPurge(this.plugin))
                .permission("betterchunkloader.commands.purge.base")
                .build();

        CommandSpec bclCmdSpec = CommandSpec.builder()
                .child(cmdBalance, "balance", "bal")
                .child(cmdInfo, "info", "i")
                .child(cmdList, "list", "ls")
                .child(cmdChunks, "chunks", "c")
                .child(cmdDelete, "delete", "d")
                .child(cmdPurge, "purge")
                .child(cmdReload, "reload")
                .executor(new CmdBCL(this.plugin))
                .build();

        Sponge.getCommandManager().register(this.plugin, bclCmdSpec, "betterchunkloader", "bcl");
    }
}
