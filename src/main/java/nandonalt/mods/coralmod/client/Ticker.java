package nandonalt.mods.coralmod.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Ticker {

    private final KeyBinding binding;

    Ticker(KeyBinding binding) {
        this.binding = binding;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent evt) {
        if(!Keyboard.isCreated() || !Keyboard.isKeyDown(binding.getKeyCode())) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        if(mc == null || !(mc.currentScreen instanceof GuiOptions)) {
            return;
        }

        mc.displayGuiScreen(new GuiCoralReef(mc.currentScreen));
    }

}
