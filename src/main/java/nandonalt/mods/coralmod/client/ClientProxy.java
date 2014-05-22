package nandonalt.mods.coralmod.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import nandonalt.mods.coralmod.CommonProxy;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

	@Override
	public void clientSetup() {
        final KeyBinding binding = new KeyBinding("coralmod.key.gui", Keyboard.KEY_C, "coralmod.settings.title");
        ClientRegistry.registerKeyBinding(binding);
        final CoralKeyHandler keyHandler = new CoralKeyHandler(binding);
        FMLCommonHandler.instance().bus().register(keyHandler);
        final Ticker ticker = new Ticker(binding);
        FMLCommonHandler.instance().bus().register(ticker);
	}

}