package eu.crushedpixel.sponge.packetgate.examples.listener;

import eu.crushedpixel.sponge.packetgate.api.event.PacketEvent;
import eu.crushedpixel.sponge.packetgate.api.listener.PacketListenerAdapter;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketConnection;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

/**
 * An example PacketListener that filters all swear words sent
 * by clients until it is globally disabled.
 *
 * This also replaces all messages sent out to clients containing
 * swear words, e.g. plugin messages the server owner has no control over.
 *
 * This is obviously just an example, and disabling the filter
 * should be handled by a command that requires permissions
 * in a real-world scenario.
 */
public class SwearWordListener extends PacketListenerAdapter {

    private static final String[] FORBIDDEN_PHRASES = { "shit", "fuck", "ez", "rekt" };

    private final Logger logger;

    public SwearWordListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onPacketRead(PacketEvent event, PacketConnection connection) {
        if (!(event.getPacket() instanceof CPacketChatMessage)) return;
        CPacketChatMessage packet = (CPacketChatMessage)event.getPacket();

        // check if the chat message contains swear words
        boolean swearing = false;
        for (String phrase : FORBIDDEN_PHRASES) {
            if (packet.getMessage().contains(phrase)) {
                swearing = true;
                break;
            }
        }

        if (swearing) {
            // cancel the event so the packet is never processed by the server
            event.setCancelled(true);

            // send a nice message to the connection that sent this packet
            connection.sendPacket(new SPacketChat(new TextComponentString("Please don't swear!")));

        } else if (packet.getMessage().equalsIgnoreCase("Disable swear filter")) {
            event.setCancelled(true);
            connection.sendPacket(new SPacketChat(new TextComponentString("Swear filter has been globally disabled.")));

            // unregister this listener to globally disable it
            Sponge.getServiceManager().provide(PacketGate.class).get().unregisterListener(this);
        }
    }

    @Override
    public void onPacketWrite(PacketEvent event, PacketConnection connection) {
        if (!(event.getPacket() instanceof SPacketChat)) return;
        SPacketChat packet = (SPacketChat)event.getPacket();

        // Note that SPacketChat.chatComponent has been made
        // publicly accessible using access transformers
        // - you may have to do this for your plugin as well
        String text = packet.chatComponent.getUnformattedText();

        boolean swearing = false;
        for (String phrase : FORBIDDEN_PHRASES) {
            if (text.contains(phrase)) {
                swearing = true;
                break;
            }
        }

        if (swearing) {
            // replace the outgoing packet with a censored chat packet
            // instead of cancelling the event
            event.setPacket(new SPacketChat(new TextComponentString("<Censored Plugin Message>")));

            logger.info("An outgoing swear word has been intercepted!", text);
        }
    }

}
