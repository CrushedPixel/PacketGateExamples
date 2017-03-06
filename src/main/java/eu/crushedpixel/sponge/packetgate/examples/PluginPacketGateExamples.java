package eu.crushedpixel.sponge.packetgate.examples;

import com.google.inject.Inject;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketConnection;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate;
import eu.crushedpixel.sponge.packetgate.examples.listener.SwearWordListener;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;

import static eu.crushedpixel.sponge.packetgate.api.listener.PacketListener.ListenerPriority;

@Plugin(id = PluginPacketGateExamples.ID)
public class PluginPacketGateExamples {

    static final String ID = "pluginpacketgateexamples";

    @Inject
    private Logger logger;

    @Listener
    public void onUserJoin(ClientConnectionEvent.Join event) {
        Sponge.getServiceManager().provide(PacketGate.class).ifPresent(packetGate -> {
            PacketConnection connection = packetGate.connectionByPlayer(event.getTargetEntity()).get();
            packetGate.registerListener(
                    new SwearWordListener(logger),
                    ListenerPriority.DEFAULT, connection,
                    CPacketChatMessage.class, SPacketChat.class);
        });
    }

}
