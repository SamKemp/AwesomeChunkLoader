package ca.shadownode.betterchunkloader.sponge.elements;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ReloadTypeElement extends CommandElement {

    public ReloadTypeElement(Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource commandSource, CommandArgs commandArgs) throws ArgumentParseException {
        String arg = commandArgs.next();
        if (arg.equalsIgnoreCase("core")) {
            return arg;
        }
        if (arg.equalsIgnoreCase("messages")) {
            return arg;
        }
        if (arg.equalsIgnoreCase("datastore")) {
            return arg;
        }
        throw commandArgs.createError(Text.of(new Object[]{TextColors.RED, arg, " is not a valid argument!"}));
    }

    @Override
    public List<String> complete(CommandSource commandSource, CommandArgs commandArgs, CommandContext commandContext) {
        List<String> list = new ArrayList();
        list.add("core");
        list.add("messages");
        list.add("datastore");
        return list;
    }

}
