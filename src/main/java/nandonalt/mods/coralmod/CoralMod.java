// CoralReef Mod
// Original author: Nandonalt
// Current maintainer: q3hardcore
// Special thanks to: fry, OvermindDL1

package nandonalt.mods.coralmod;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Mod(modid = "coralmod", name="CoralReef Mod", version="1.7.2")

public class CoralMod {

    /**
     * Mod instance
     */
    @Instance("coralmod")
    public static CoralMod instance;

    private static final String clientProxy = "nandonalt.mods.coralmod.client.ClientProxy";
    private static final String serverProxy = "nandonalt.mods.coralmod.CommonProxy";

    /**
     * Proxy instance
     */
    @SidedProxy(modId = "coralmod", clientSide = clientProxy, serverSide = serverProxy)
    public static CommonProxy proxy;

    /**
     * Directory for storing configuration
     */
    private File configDir;

    public static ModSettings settingsManager;

    /**
     * Block instances
     */
    static Block coral1, coral2, coral3, coral4, coral5;

    /**
     * Pre-initialization
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        // Set configuration directory
        configDir = new File(evt.getModConfigurationDirectory(), "coralreef");

        // Register blocks
        registerBlocks();
    }

    /**
     * Initialization
     */
    @EventHandler
    public void init(FMLInitializationEvent evt) {
        // Experimental settings stuff
        Configuration config = new Configuration(new File(configDir, "settings.cfg"));
        settingsManager = new ModSettings(config);

        final List<ModSettings.LocalProperty> settings = new LinkedList<ModSettings.LocalProperty>();
        settings.add(new ModSettings.BooleanProperty("coralgen", true));
        settings.add(new ModSettings.BooleanProperty("spikyenabled", true));
        settings.add(new ModSettings.BooleanProperty("enablebubbles", true));
        settings.add(new ModSettings.BooleanProperty("enablegrow", false));
        settings.add(new ModSettings.IntProperty("avgsize", 1, 0, 2));
        settings.add(new ModSettings.BooleanProperty("oceanonly", true));
        settings.add(new ModSettings.BooleanProperty("alldimensions", false));
        settings.add(new ModSettings.BooleanProperty("light", true));
        settings.add(new ModSettings.BooleanProperty("air", false));
        settings.add(new ModSettings.BooleanProperty("debug", false));
        settingsManager.register("settings", settings);

        final List<ModSettings.LocalProperty> genSettings = new LinkedList<ModSettings.LocalProperty>();
        genSettings.add(new ModSettings.IntProperty("baseheight", 0, 0, 64));
        genSettings.add(new ModSettings.IntProperty("heightoffset", 128, 4, 256));
        genSettings.add(new ModSettings.IntProperty("iterationfactor", 10, 0, 12));
        genSettings.add(new ModSettings.IntProperty("radius", 16, 0, 16));
        genSettings.add(new ModSettings.StringProperty("biomes", ""));
        genSettings.add(new ModSettings.BooleanProperty("dryseabeds", false));
        settingsManager.register("generation", genSettings);

        settingsManager.loadSettings();

        // Client setup
        proxy.clientSetup();

        // Register world generation hook
        MinecraftForge.EVENT_BUS.register(new CoralGenerator());
    }

    /**
     * Register command
     */
    @EventHandler
    public void severStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new CommandCoralMod());
    }

    private void registerBlocks() {
        // Instantiate blocks
        coral1 = new BlockCoral(1).setHardness(0.2F).setStepSound(Block.soundTypeStone).setBlockName("coralmod.Coral1");
        coral2 = new BlockReef(0).setHardness(0.5F).setStepSound(Block.soundTypeStone).setBlockName("coralmod.Coral2");
        coral3 = new BlockReef(1).setHardness(0.5F).setStepSound(Block.soundTypeStone).setBlockName("coralmod.Coral3");
        coral4 = new BlockCoral(6).setHardness(0.2F).setStepSound(Block.soundTypeStone).setBlockName("coralmod.Coral4");
        coral5 = new BlockCoral(6).setHardness(0.2F).setStepSound(Block.soundTypeStone).setLightLevel(1.0F).setBlockName("coralmod.CoralLightt");

        // Set tab
        coral1.setCreativeTab(CreativeTabs.tabDecorations);

        // Register blocks
        GameRegistry.registerBlock(coral1, ItemCoral.class, "Coral1");
        GameRegistry.registerBlock(coral2, "Coral2");
        GameRegistry.registerBlock(coral3, "Coral3");
        GameRegistry.registerBlock(coral4, ItemCoral.class, "Coral4");
        GameRegistry.registerBlock(coral5, ItemCoral.class, "Coral5");

        // Add recipes
        final Item dye = Items.dye;
        GameRegistry.addRecipe(new ItemStack(dye, 1, 0xE), "B", 'B', new ItemStack(coral1, 1, 0));
        GameRegistry.addRecipe(new ItemStack(dye, 1, 0xA), "B", 'B', new ItemStack(coral1, 1, 1));
        GameRegistry.addRecipe(new ItemStack(dye, 1, 0xD), "B", 'B', new ItemStack(coral1, 1, 2));
        GameRegistry.addRecipe(new ItemStack(dye, 1, 0x9), "B", 'B', new ItemStack(coral4, 1, 3));
        GameRegistry.addRecipe(new ItemStack(dye, 1, 0x3), "B", 'B', new ItemStack(coral1, 1, 4));
        GameRegistry.addRecipe(new ItemStack(dye, 1, 0x6), "B", 'B', new ItemStack(coral5, 1, 5));
    }

    /**
     * Checks if a block is water and if it's stationary
     */
    public static boolean checkWater(Block block, boolean stationary) {
        return checkWater(block) && block.func_149698_L() == stationary;
    }

    /**
     * Checks if a block is water
     */
    public static boolean checkWater(Block block) {
        // if the block is any type of coral, it's not water
        return !(block instanceof BlockCoral) && block.getMaterial() == Material.water;
    }

    public static void log(String s, boolean warning) {
        FMLLog.log(warning ? Level.WARN : Level.INFO, "[CoralMod] " + s);
    }

    public static void log(String s) {
        log(s, false);
    }

    public static void logDebug(String s) {
        if(settingsManager.getBooleanValue("settings", "debug")) {
            log(s);
        }
    }

}
