package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Message;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command for the limbo command
 */
public class SubCommandLimbo extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandLimbo(ACommand parent) {
        super(
                parent,
                "limbo",
                new ChatComponentTranslation("quickplay.commands.quickplay.limbo.help").getUnformattedText(),
                "",
                true,
                true,
                -90.0
        );
    }

    @Override
    public void run(String[] args) {
        if(Quickplay.INSTANCE.onHypixel) {
            String currentServer = Quickplay.INSTANCE.instanceWatcher.getCurrentServer();
            if(currentServer == null) currentServer = "null";

            if(currentServer.contains("mini") || currentServer.contains("mega")) {
                //TODO: This doesn't check for lobby protection. Should implement that eventually but it's difficult.
                Quickplay.INSTANCE.chatBuffer.push("/hub");
            }

            if(!currentServer.equals("limbo"))
                Quickplay.INSTANCE.chatBuffer.push("/achat §");
            else
                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.commands.quickplay.limbo.alreadythere").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        } else {
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.commands.quickplay.limbo.offline").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
