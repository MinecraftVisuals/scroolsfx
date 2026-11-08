package hotbarsfx;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class HotbarSfxClient implements ClientModInitializer {
    private static final SoundEvent SWITCH_SOUND =
            SoundEvent.of(Identifier.of("hotbarsfx", "hotbar_switch"));

    private int lastSlot = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                lastSlot = -1;
                return;
            }
            int slot = client.player.getInventory().getSelectedSlot();
            if (lastSlot != -1 && slot != lastSlot) {
                client.getSoundManager().play(
                        PositionedSoundInstance.master(SWITCH_SOUND, 1.0f, 1.0f));
            }
            lastSlot = slot;
        });
    }
}
