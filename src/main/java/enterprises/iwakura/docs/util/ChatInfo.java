package enterprises.iwakura.docs.util;

import java.awt.Color;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;

import io.github.insideranh.talemessage.TaleMessage;
import lombok.Getter;

@Getter
public enum ChatInfo {
    SUCCESS("<b>>></b>", new Color(190, 237, 210), new Color(190, 237, 210)),
    INFO("<b>[>]</b>", new Color(201, 201, 201), new Color(255, 255, 255)),
    WARN("<b>[!]</b>", new Color(230, 232, 118), new Color(255, 255, 255)),
    ERROR("<b>[!!]</b>", new Color(235, 120, 120), new Color(235, 120, 120)),
    DEBUG("<b>[D]</b>", new Color(169, 108, 235), new Color(169, 108, 235));

    private final String prefix;
    private final Color color;
    private final Color textColor;
    private final String colorHex;
    private final String textColorHex;

    ChatInfo(String prefix, Color color, Color textColor) {
        this.prefix = prefix;
        this.color = color;
        this.textColor = textColor;
        this.colorHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        this.textColorHex = String.format("#%02x%02x%02x", textColor.getRed(), textColor.getGreen(), textColor.getBlue());
    }

    /**
     * Creates a formatted message with the appropriate prefix and colors. Replaces <code>{c}</code> with the prefix
     * color and <code>{t}</code> with the text color.
     * Use <code>{/c}</code> and <code>{/t}</code> to close the color tags.
     *
     * @param message The message content.
     *
     * @return A Message object with formatting.
     */
    public Message of(String message) {
        return TaleMessage.parse("{c}%s{/c} {t}%s{/t}".formatted(prefix, message)
            .replaceAll("\\{c}", "<%s>".formatted(colorHex))
            .replaceAll("\\{/c}", "</%s>".formatted(colorHex))
            .replaceAll("\\{t}", "<%s>".formatted(textColorHex))
            .replaceAll("\\{/t}", "<%s>".formatted(textColorHex))
        );
    }

    /**
     * Sends a formatted message to the specified message receiver. See {@link #of(String)} for formatting details.
     *
     * @param messageReceiver The receiver of the message.
     * @param message         The message content.
     */
    public void send(IMessageReceiver messageReceiver, String message) {
        messageReceiver.sendMessage(of(message));
    }

    /**
     * Sends a formatted message to the command context's sender. See {@link #of(String)} for formatting details.
     *
     * @param ctx     The command context.
     * @param message The message content.
     */
    public void send(CommandContext ctx, String message) {
        ctx.sendMessage(of(message));
    }
}
