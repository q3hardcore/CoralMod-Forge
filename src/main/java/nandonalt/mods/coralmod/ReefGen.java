package nandonalt.mods.coralmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class ReefGen implements IReefGen {

    private final Block coralBlock;
    private final int numberOfBlocks;
    private final boolean spikyEnabled;

    private boolean generated = false;

    public ReefGen(Block coralBlock, int numberOfBlocks, boolean spikyEnabled) {
        this.coralBlock = coralBlock;
        this.numberOfBlocks = numberOfBlocks;
        this.spikyEnabled = spikyEnabled;
    }

    // TODO: improve efficiency
    @Override
    public boolean generate(World world, Random random, int x, int y, int z) {
        final float f = random.nextFloat() * (float)Math.PI;
        final double d1 = x + 8 + MathHelper.sin(f) * numberOfBlocks / 8.0f;
        final double d2 = x + 8 - MathHelper.sin(f) * numberOfBlocks / 8.0f;
        final double d3 = z + 8 + MathHelper.cos(f) * numberOfBlocks / 8.0f;
        final double d4 = z + 8 - MathHelper.cos(f) * numberOfBlocks / 8.0f;
        final double d5 = y + random.nextInt(3) + 2;
        final double d6 = y + random.nextInt(3) + 2;

        for(int i = 0; i <= numberOfBlocks; i++) {
            final double d7 = d1 + (d2 - d1) * i / numberOfBlocks;
            final double d8 = d5 + (d6 - d5) * i / numberOfBlocks;
            final double d9 = d3 + (d4 - d3) * i / numberOfBlocks;
            final double d10 = random.nextDouble() * numberOfBlocks / 16.0;
            final double d11 = (MathHelper.sin(i * (float)Math.PI / numberOfBlocks) + 1.0f) * d10 + 1.0;
            final double d12 = (MathHelper.sin(i * (float)Math.PI / numberOfBlocks) + 1.0f) * d10 + 1.0;

            for(int j = (int)(d7 - d11 / 2.0); j <= (int)(d7 + d11 / 2.0); j++) {
                for(int k = (int)(d8 - d12 / 2.0); k <= (int)(d8 + d12 / 2.0); k++) {
                    for(int m = (int)(d9 - d11 / 2.0); m <= (int)(d9 + d11 / 2.0); m++) {
                        final double d13 = (j + 0.5 - d7) / (d11 / 2.0);
                        final double d14 = (k + 0.5 - d8) / (d12 / 2.0);
                        final double d15 = (m + 0.5 - d9) / (d11 / 2.0);
                        final Block block = world.getBlock(j, k, m);
                        if(d13 * d13 + d14 * d14 + d15 * d15 < 1.0D
                                && (block.getMaterial() == Material.sand || block.getMaterial() == Material.ground)
                                && CoralMod.checkWater(world.getBlock(j, k + 1, m)) && CoralMod.checkWater(world.getBlock(j, k + 2, m))
                                && CoralMod.checkWater(world.getBlock(j, k + 3, m)) && CoralMod.checkWater(world.getBlock(j, k + 4, m))
                                && CoralMod.checkWater(world.getBlock(j, k + 5, m)) && CoralMod.checkWater(world.getBlock(j, k + 6, m))
                                && CoralMod.checkWater(world.getBlock(j, k + 7, m))) {
                            world.setBlock(j, k, m, coralBlock, 0, 2);
                            world.setBlock(j + 1, k, m, coralBlock, 0, 2);
                            world.setBlock(j, k, m + 1, coralBlock, 0, 2);
                            world.setBlock(j + 1, k, m + 1, coralBlock, 0, 2);
                            generated = true;

                            for(int loop = 0; loop <= 3; loop++) {
                                final int p1, p2, p3;
                                if(loop == 0) {
                                    p1 = j;
                                    p2 = k;
                                    p3 = m;
                                } else if(loop == 1) {
                                    p1 = j + 1;
                                    p2 = k;
                                    p3 = m;
                                } else if(loop == 2) {
                                    p1 = j;
                                    p2 = k;
                                    p3 = m + 1;
                                } else if(loop == 3) {
                                    p1 = j + 1;
                                    p2 = k;
                                    p3 = m + 1;
                                } else {
                                    p1 = j;
                                    p2 = k;
                                    p3 = m;
                                }

                                if(CoralMod.checkWater(world.getBlock(p1, p2 + 1, p3)) && random.nextInt(2) == 0) {
                                    int rand = random.nextInt(3);
                                    world.setBlock(p1, p2 + 1, p3, CoralMod.coral1, rand, 2);
                                    if(random.nextInt(20) == 0) {
                                        rand = 0;
                                        world.setBlock(p1, p2 + 1, p3, CoralMod.coral5, 5, 2);
                                    }

                                    if(random.nextInt(5) == 0) {
                                        rand = 0;
                                        world.setBlock(p1, p2 + 1, p3, CoralMod.coral4, 3, 2);
                                    }

                                    if(random.nextInt(2) == 0 && rand == 1) {
                                        world.setBlock(p1, p2 + 2, p3, CoralMod.coral1, 1, 2);
                                        if(random.nextInt(4) == 0) {
                                            world.setBlock(p1, p2 + 3, p3, CoralMod.coral1, 1, 2);
                                            if(random.nextInt(8) == 0) {
                                                world.setBlock(p1, p2 + 4, p3, CoralMod.coral1, 1, 2);
                                                if(random.nextInt(8) == 0) {
                                                    world.setBlock(p1, p2 + 5, p3, CoralMod.coral1, 1, 2);
                                                }
                                            }
                                        }
                                    }
                                }

                                if(spikyEnabled && random.nextInt(30) == 0 && CoralMod.checkWater(world.getBlock(p1, p2 + 1, p3))) {
                                    world.setBlock(p1, p2 + 1, p3, CoralMod.coral1, 4, 2);
                                    if(random.nextInt(2) == 0) {
                                        world.setBlock(p1, p2 + 2, p3, CoralMod.coral1, 4, 2);
                                        if(random.nextInt(4) == 0) {
                                            world.setBlock(p1, p2 + 3, p3, CoralMod.coral1, 4, 2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean isGenerated() {
        return generated;
    }

}
