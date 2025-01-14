package org.cloudburstmc.server.command;

import com.nukkitx.math.vector.Vector3f;
import lombok.experimental.UtilityClass;
import org.cloudburstmc.server.CloudServer;
import org.cloudburstmc.server.locale.TextContainer;
import org.cloudburstmc.server.locale.TranslationContainer;
import org.cloudburstmc.server.permission.Permissible;
import org.cloudburstmc.server.utils.TextFormat;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@UtilityClass
public class CommandUtils {
    private static final Pattern RELATIVE_PATTERN = Pattern.compile("(~)?([+\\-]?[0-9]+\\.?[0-9]*)?");

    public static Optional<Vector3f> parseVector3f(String[] args, Vector3f relative) {
        checkNotNull(args, "args");
        if (args.length < 3) {
            return Optional.empty();
        }

        try {
            return Optional.of(Vector3f.from(
                    getPosition(args[0], relative.getX()),
                    getPosition(args[1], relative.getY()),
                    getPosition(args[2], relative.getZ())
            ));
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return Optional.empty();
    }

    public static float getPosition(String pos, float relative) throws IllegalArgumentException {
        Matcher matcher = RELATIVE_PATTERN.matcher(pos);
        checkArgument(matcher.matches(), "Invalid position");
        float position;

        if (matcher.group(2) != null) {
            position = Float.parseFloat(matcher.group(2));
        } else {
            position = 0;
        }

        if (matcher.group(1) != null) {
            position += relative;
        }
        return position;
    }

    public static void broadcastCommandMessage(CommandSender source, String message) {
        broadcastCommandMessage(source, message, true);
    }

    public static void broadcastCommandMessage(CommandSender source, String message, boolean sendToSource) {
        Set<Permissible> users = source.getServer().getPermissionManager().getPermissionSubscriptions(CloudServer.BROADCAST_CHANNEL_ADMINISTRATIVE);

        TranslationContainer result = new TranslationContainer("chat.type.admin", source.getName(), message);

        TranslationContainer colored = new TranslationContainer(TextFormat.GRAY + "" + TextFormat.ITALIC + "%chat.type.admin", source.getName(), message);

        if (sendToSource && !(source instanceof ConsoleCommandSender)) {
            source.sendMessage(message);
        }

        for (Permissible user : users) {
            if (user instanceof CommandSender) {
                if (user instanceof ConsoleCommandSender) {
                    ((ConsoleCommandSender) user).sendMessage(result);
                } else if (!user.equals(source)) {
                    ((CommandSender) user).sendMessage(colored);
                }
            }
        }
    }

    public static void broadcastCommandMessage(CommandSender source, TextContainer message) {
        broadcastCommandMessage(source, message, true);
    }

    public static void broadcastCommandMessage(CommandSender source, TextContainer message, boolean sendToSource) {
        TextContainer m = message.clone();
        String resultStr = "[" + source.getName() + ": " + m.getText() + "]";

        Set<Permissible> users = source.getServer().getPermissionManager().getPermissionSubscriptions(CloudServer.BROADCAST_CHANNEL_ADMINISTRATIVE);

        String coloredStr = TextFormat.GRAY + "" + TextFormat.ITALIC + resultStr;

        m.setText(resultStr);
        TextContainer result = m.clone();
        m.setText(coloredStr);
        TextContainer colored = m.clone();

        if (sendToSource && !(source instanceof ConsoleCommandSender)) {
            source.sendMessage(message);
        }

        for (Permissible user : users) {
            if (user instanceof CommandSender) {
                if (user instanceof ConsoleCommandSender) {
                    ((ConsoleCommandSender) user).sendMessage(result);
                } else if (!user.equals(source)) {
                    ((CommandSender) user).sendMessage(colored);
                }
            }
        }
    }

}
