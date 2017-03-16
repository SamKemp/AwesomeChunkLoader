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
                .permission("betterchunkloader.commands.balance")
                .build();

        CommandSpec cmdReload = CommandSpec.builder()
                .arguments(
                        GenericArguments.optional(new ReloadTypeElement(Text.of("type")))
                )
                .executor(new CmdReload(this.plugin))
                .permission("betterchunkloader.commands.reload")
                .build();

        CommandSpec cmdInfo = CommandSpec.builder()
                .arguments(
                        GenericArguments.none()
                )
                .executor(new CmdInfo(this.plugin))
                .permission("betterchunkloader.commands.info")
                .build();

        CommandSpec cmdChunks = CommandSpec.builder()
                .arguments(
                        new ChunksChangeOperatorElement(Text.of("change")),
                        GenericArguments.string(Text.of("player")),
                        new LoaderTypeElement(Text.of("type")),
                        GenericArguments.integer(Text.of("value"))
                )
                .executor(new CmdChunks(this.plugin))
                .permission("betterchunkloader.commands.chunks")
                .build();
        CommandSpec cmdList = CommandSpec.builder()
                .arguments(
                        new LoaderTypeElement(Text.of("type")),
                        GenericArguments.optional(GenericArguments.string(Text.of("player")))
                )
                .executor(new CmdList(this.plugin))
                .permission("betterchunkloader.commands.list")
                .build();
        CommandSpec cmdDelete = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("player")),
                        GenericArguments.optional(new LoaderTypeElement(Text.of("type")))
                )
                .executor(new CmdDelete(this.plugin))
                .permission("betterchunkloader.commands.delete")
                .build();

        CommandSpec cmdPurge = CommandSpec.builder()
                .executor(new CmdPurge(this.plugin))
                .permission("betterchunkloader.commands.purge")
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
