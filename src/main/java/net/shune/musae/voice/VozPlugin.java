package net.shune.musae.voice;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.shune.musae.auth.AutorizacaoServidor;

import java.util.UUID;

@ForgeVoicechatPlugin
public class VozPlugin implements VoicechatPlugin {

    public static VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return "musae_voz";
    }

    @Override
    public void initialize(VoicechatApi api) {
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (!AutorizacaoServidor.isAutorizado()) return;
        if (event.getSenderConnection() == null) return;
        if (event.getSenderConnection().getPlayer() == null) return;

        UUID senderUUID = event.getSenderConnection().getPlayer().getUuid();

        if (!MuteManager.canPlayerSpeak(senderUUID)) {
            event.cancel();
        }
    }
}
