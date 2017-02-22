package ca.shadownode.betterchunkloader.sponge.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Utilities {

    public static Text parseMessage(String message, String... args) {
        for (int i = 0; i < args.length; i++) {
            String param = args[i];
            message = message.replace("{" + i + "}", param);
        }

        Text textMessage = TextSerializers.FORMATTING_CODE.deserialize(message);
        List<String> urls = extractUrls(message);

        if (urls.isEmpty()) {
            return textMessage;
        }

        Iterator<String> iterator = urls.iterator();
        while (iterator.hasNext()) {
            String url = iterator.next();
            String msgBefore = StringUtils.substringBefore(message, url);
            String msgAfter = StringUtils.substringAfter(message, url);
            if (msgBefore == null) {
                msgBefore = "";
            } else if (msgAfter == null) {
                msgAfter = "";
            }
            try {
                textMessage = Text.of(
                        TextSerializers.FORMATTING_CODE.deserialize(msgBefore),
                        TextActions.openUrl(new URL(url)),
                        Text.of(TextColors.GREEN, url),
                        TextSerializers.FORMATTING_CODE.deserialize(msgAfter));
            } catch (MalformedURLException e) {
                return Text.of(message);
            }

            iterator.remove();
        }
        return textMessage;
    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher;
        try {
            urlMatcher = pattern.matcher(text);
        } catch (Throwable t) {
            return containedUrls;
        }
        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    public static String getReadableLocation(Location<World> location) {
        StringBuilder builder = new StringBuilder();
        builder.append(location.getExtent().getName());
        builder.append(" X: ").append(location.getX());
        builder.append(" Y: ").append(location.getY());
        builder.append(" Z: ").append(location.getZ());
        return builder.toString();
    }

    public static long getPlayerDataLastModified(UUID playerUUID) {
        String name = Sponge.getServer().getDefaultWorldName();
        Path path = Sponge.getGame().getGameDirectory().resolve(name);
        File playerData = new File(path.toString(), "playerdata" + File.separator + playerUUID.toString() + ".dat");
        if (playerData.exists()) {
            return playerData.lastModified();
        }
        return 0;
    }
    
    public static Optional<User> getOfflinePlayer(UUID uuid) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(uuid);
    }
    
    public static Optional<User> getOfflinePlayer(String username) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(username);
    }

    // Test Code to check which chunks are in use.
    /*
     * for(Chunk c :chunks) {
     * 
     * world.getLocation(c.getBlockMax()).setBlockType(BlockTypes.BEDROCK,
     * Cause.source(Sponge.getPluginManager().fromInstance(this).get()).
     * build()); } log.info(chunks.size()+"");
     */
}
