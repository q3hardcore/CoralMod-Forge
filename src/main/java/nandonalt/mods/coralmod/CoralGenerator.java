package nandonalt.mods.coralmod;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CoralGenerator {

    @SubscribeEvent
    public void populateChunk(PopulateChunkEvent.Post evt) {
        // Check coral generation is enabled
        if(!CoralMod.settingsManager.getBooleanValue("settings", "coralgen")) {
            return;
        }

        // Check dimension
        if(evt.world.getWorldInfo().getVanillaDimension() != 0) {
            if(!CoralMod.settingsManager.getBooleanValue("settings", "alldimensions")) {
                return;
            }
        }

        // Convert to non-chunk positions
        final int posX = evt.chunkX << 4;
        final int posZ = evt.chunkZ << 4;

        // Check biome
        if(CoralMod.settingsManager.getBooleanValue("settings", "oceanonly")) {
            final BiomeGenBase biome = getBiomeGenAt(evt.world, posX, posZ);
            if(biome.biomeName.endsWith("River") || biome.biomeName.startsWith("River")
                    || !BiomeDictionary.isBiomeOfType(biome, Type.WATER)) {
                return;
            }
        } else {
            String biomes = CoralMod.settingsManager.getValue("generation", "biomes");
            String[] biomesArray = biomes.split(",");

            // HAXX (..but why is this necessary?)
            if(biomesArray.length == 1) {
                if(biomesArray[0].isEmpty()) {
                    biomesArray = new String[0];
                }
            }

            List<String> biomesList = Arrays.asList(biomesArray);
            if(!biomesList.isEmpty()) {
                int biomeID = getBiomeGenAt(evt.world, posX, posZ).biomeID;
                if(!biomesList.contains(((Integer)biomeID).toString())) {
                    return;
                }
            }
        }

        generate(evt.rand, posX, posZ, evt.world);
    }

    private BiomeGenBase getBiomeGenAt(World world, int posX, int posZ) {
        return world.getWorldChunkManager().getBiomeGenAt(posX, posZ);
    }

    /**
     * Generate coral reef
     */
    protected static boolean generate(Random random, int posX, int posZ, World world) {
        // Reef generation size
        final int min1, min2, max1, max2;

        final int size = CoralMod.settingsManager.getIntValue("settings", "avgsize");
        if(size == 0) {
            min1 = 15;
            min2 = 10;
            max1 = 40;
            max2 = 20;
        } else if(size == 2) {
            min1 = 45;
            min2 = 30;
            max1 = 70;
            max2 = 45;
        } else {
            min1 = 35;
            min2 = 25;
            max1 = 60;
            max2 = 35;
        }

        IReefGen reefGen;
        int genNum = 0;

        final int baseHeight = CoralMod.settingsManager.getIntValue("generation", "baseheight");
        final int heightOffset = CoralMod.settingsManager.getIntValue("generation", "heightoffset");
        final int iterationFactor = CoralMod.settingsManager.getIntValue("generation", "iterationfactor");
        final int iterations = (heightOffset / 16) * iterationFactor;
        final int radius = CoralMod.settingsManager.getIntValue("generation", "radius");

        final int maxHeight;
        if(heightOffset < 4 || baseHeight + heightOffset > world.getHeight()) {
            CoralMod.log("CoralMod: Unsafe maxHeight", true);
            maxHeight = world.getHeight();
        } else {
            maxHeight = baseHeight + heightOffset;
        }

        for(int i = 0; i < iterations; i++) {
            final int x = posX + random.nextInt(radius);
            final int y = baseHeight + random.nextInt(maxHeight - baseHeight);
            final int z = posZ + random.nextInt(radius);
            final int numberReef = random.nextInt(max1 - min1 + 1) + min1;
            final boolean spiky = CoralMod.settingsManager.getBooleanValue("settings", "spikyenabled");
            reefGen = new ReefGen(CoralMod.coral2, numberReef, spiky);
            reefGen.generate(world, random, x, y, z);
            if(reefGen.isGenerated()) {
                genNum++;
            }
        }

        for(int i = 0; i < iterations; i++) {
            final int x = posX + random.nextInt(radius);
            final int y = baseHeight + random.nextInt(maxHeight - baseHeight);
            final int z = posZ + random.nextInt(radius);
            final int numberReef = random.nextInt(max2 - min2 + 1) + min2;
            reefGen = new ReefGen2(CoralMod.coral3, numberReef);
            reefGen.generate(world, random, x, y, z);
            if(reefGen.isGenerated()) {
                genNum++;
            }
        }

        return genNum > 0;
    }

}