package hotbarsfx;

import hotbarsfx.mixin.HandledScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class HotbarSfxClient implements ClientModInitializer {
    private static final SoundEvent SWITCH_SOUND =
            SoundEvent.of(Identifier.of("hotbarsfx", "hotbar_switch"));

    // true = only play when hovering a slot that has an item
    // false = play on every slot, even empty ones
    private static final boolean ONLY_ITEMS = true;

    // minimum ms between sounds so fast mouse swipes don't stack
    private static final long COOLDOWN_MS = 30;

    private int lastSlot = -1;
    private Slot lastHovered = null;
    private long lastPlay = 0;

    private void playTick() {
        long now = System.currentTimeMillis();
        if (now - lastPlay < COOLDOWN_MS) return;
        lastPlay = now;
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SWITCH_SOUND, 1.0f, 1.0f));
    }

    @Override
    public void onInitializeClient() {
        // hotbar switching
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                lastSlot = -1;
                return;
            }
            int slot = client.player.getInventory().getSelectedSlot();
            if (lastSlot != -1 && slot != lastSlot) {
                playTick();
            }
            lastSlot = slot;
        });

        // hovering slots in inventories / chests / etc.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen<?>) {
                lastHovered = null;
                ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) -> {
                    Slot hovered = ((HandledScreenAccessor) s).hotbarsfx$getFocusedSlot();
                    if (hovered == null) {
                        lastHovered = null;
                        return;
                    }
                    if (hovered != lastHovered) {
                        lastHovered = hovered;
                        if (!ONLY_ITEMS || hovered.hasStack()) {
                            playTick();
                        }
                    }
                });
            }
        });
    }
}
