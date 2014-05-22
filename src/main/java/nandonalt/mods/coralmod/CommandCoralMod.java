package nandonalt.mods.coralmod;

import cpw.mods.fml.common.Mod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.WHITE;

public class CommandCoralMod extends CommandBase {

    @Override
    public String getCommandName() {
        return "coralmod";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(args.length < 1) {
            final Mod annotation = CoralMod.class.getAnnotation(Mod.class);
            sendChatToPlayer(sender, annotation.name() + ", v" + annotation.version());
            return;
        }

        if(args[0].equals("biomes")) {
            final String s;
            if(CoralMod.settingsManager.getBooleanValue("settings", "oceanonly")) {
                s = "Ocean only";
            } else {
                final String biomes = CoralMod.settingsManager.getValue("generation", "biomes");
                String[] biomesArray = biomes.split(",");

                // HAXX (..but why is this necessary?)
                if(biomesArray.length == 1) {
                    if(biomesArray[0].isEmpty()) {
                        biomesArray = new String[0];
                    }
                }

                if(biomesArray.length == 0) {
                    s = "All";
                } else {
                    s = biomes;
                }
            }
            sendChatToPlayer(sender, "Biomes: " + s);
        } else if(args[0].equals("regen")) {
            final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            final Random random = new Random(player.worldObj.getSeed());
            final int posX = MathHelper.floor_double(player.posX);
            final int posZ = MathHelper.floor_double(player.posZ);
            final int chunkX = posX >> 4; final int chunkZ = posZ >> 4;
            final long i = random.nextLong() / 2L * 2L + 1L;
            final long j = random.nextLong() / 2L * 2L + 1L;
            random.setSeed((long)chunkX * i + (long)chunkZ * j ^ player.worldObj.getSeed());
            if(CoralGenerator.generate(random, posX, posZ, player.worldObj)) {
                sendChatToPlayer(sender, "Re-generated coral at: " + chunkX + ", " + chunkZ);
            } else {
                sendChatToPlayer(sender, "Couldn't generate coral at: " + chunkX + ", " + chunkZ);
            }
        } else if(args[0].equals("settings")) {
            sendChatToPlayer(sender, GREEN + "===CoralMod Settings===");
            for(String setting : CoralMod.settingsManager.getNames("settings")) {
                sendChatToPlayer(sender, GREEN + setting + ": " + WHITE + getSettingsValue(setting));
            }
        } else if(args[0].equals("test")) {
            sendChatToPlayer(sender, GREEN + "===CoralGen Settings===");
            for(String setting : CoralMod.settingsManager.getNames("generation")) {
                final String s = CoralMod.settingsManager.getValue("generation", setting);
                sendChatToPlayer(sender, GREEN + setting + ": " + WHITE + s);
            }
            final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            final ItemStack stack = player.getCurrentEquippedItem();
            if(stack != null) {
                sendChatToPlayer(sender, stack.toString());
            }
        } else {
            final String setting;
            final boolean toggle;
            if(args[0].equals("toggle")) {
                if(args.length < 2) throw new WrongUsageException("commands.coralmod.usage");
                setting = args[1];
                toggle = true;
            } else {
                setting = args[0];
                toggle = false;
            }

            if(!CoralMod.settingsManager.getNames("settings").contains(setting)) {
                throw new WrongUsageException("commands.coralmod.usage");
            }

            final Boolean toggled = toggle ? CoralMod.settingsManager.toggle("settings", setting) : null;
            if(toggled == null) {
                sendChatToPlayer(sender, setting + ": " + getSettingsValue(setting));
                return;
            }

            if(!toggled) {
                sendChatToPlayer(sender, "Couldn't toggle " + setting);
                return;
            }

            sendChatToPlayer(sender, setting + ": " + getSettingsValue(setting) + " (toggled)");
            CoralMod.settingsManager.updateSettings();
        }
    }

    private String getSettingsValue(String s) {
        return CoralMod.settingsManager.getValue("settings", s);
    }

    private void sendChatToPlayer(ICommandSender sender, String msg) {
        sender.addChatMessage(new ChatComponentText(msg));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        final List<String> tabCompletionOptions = new ArrayList<String>();
        tabCompletionOptions.addAll(CoralMod.settingsManager.getNames("settings"));
        Collections.sort(tabCompletionOptions);
        tabCompletionOptions.add(0, "biomes");
        tabCompletionOptions.add(1, "regen");
        tabCompletionOptions.add(2, "settings");
        return args.length == 1 ? getListOfStringsFromIterableMatchingLastWord(args, tabCompletionOptions) : null;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.coralmod.usage";
    }

    @Override
    public int compareTo(Object o) {
        return compareTo((ICommand)o);
    }
}