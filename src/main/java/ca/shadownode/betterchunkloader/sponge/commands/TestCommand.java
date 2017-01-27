package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class TestCommand implements CommandExecutor {
    
    /*This was purely for testing my class for handling the chunks and tickets.*/

    private final BetterChunkLoader plugin;
    
    public TestCommand(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }
  
    public void register() {
        CommandSpec testa = CommandSpec.builder()
            .description(Text.of("Test command"))
            .permission("test.command")
            .executor(this::add)
            .build();
        CommandSpec testr = CommandSpec.builder()
            .description(Text.of("Test command"))
            .permission("test.command")
            .executor(this::rm)
            .build();
        CommandSpec test = CommandSpec.builder()
            .description(Text.of("Test command"))
            .permission("test.command")
            .executor(this)
            .child(testa, "add")
            .child(testr, "rm")
            .build();
        Sponge.getCommandManager().register(plugin, test, "chunks");
    }

    @Override
    public CommandResult execute(CommandSource cs, CommandContext cc) throws CommandException {
        return CommandResult.success();
    }
    
    public CommandResult rm(CommandSource cs, CommandContext cc) throws CommandException {
        /**if(cs instanceof Player) {
            Player player = (Player)cs;
            Location<World> location = player.getLocation();
            List<ChunkLoader> cls = plugin.getChunkManager().getAllLoadersInChunk(player.getWorld().getUniqueId(), location.getChunkPosition());
            cls.forEach((cl) -> {
                plugin.getChunkManager().removeChunkLoader(cl);
            });
        }**/  
        return CommandResult.success();
    }   
    
    public CommandResult add(CommandSource cs, CommandContext cc) throws CommandException {
        /**if(cs instanceof Player) {
            Player player = (Player)cs;
            Location<World> location = player.getLocation();
            ChunkLoader chunkLoader = new ChunkLoader(player.getUniqueId(), player.getWorld().getUniqueId(), location.getBlockPosition(), location.getChunkPosition(), 10);
            plugin.getChunkManager().addChunkLoader(chunkLoader);
        }**/
        return CommandResult.success();
    }    
    
    
}