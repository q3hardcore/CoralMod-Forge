package nandonalt.mods.coralmod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockCoral extends Block {

    @SideOnly(Side.CLIENT)
    private IIcon[] iconBuffer;

    private final int type;

    public static final String[] types = new String[] {"coral1", "coral2", "coral3", "coral4", "coral5", "coral6"};

    BlockCoral(int type) {
        super(Material.water);
        this.type = type;

        if(type == 1) {
            final float f = 0.375f;
            setBlockBounds(0.5f - f, 0.0f, 0.5f - f, 0.5f + f, 1.0f, 0.5f + f);
        } else if (type == 6) {
            final float f = 0.5f;
            setBlockBounds(0.5f - f, 0.0f, 0.5f - f, 0.5f + f, 0.25f, 0.5f + f);
        }

        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        super.updateTick(world, x, y, z, random);

        if(!CoralMod.settingsManager.getBooleanValue("settings", "enablegrow")) {
            return;
        }

        final int metadata = world.getBlockMetadata(x, y, z);
        if(metadata == 1 || metadata == 4) {
            int offset = 1;
            while(world.getBlock(x, y - offset, z) == this)
                offset++;

            if(offset > 3) return;

            final int rand = random.nextInt(100);
            final Block aboveBlock = world.getBlock(x, y + 1, z);
            final Block aboveBlock2 = world.getBlock(x, y + 2, z);
            if(rand == 0 && CoralMod.checkWater(aboveBlock) && CoralMod.checkWater(aboveBlock2)) {
                world.setBlock(x, y + 1, z, this, metadata, 3); // with notify
            }
        }

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        if(!CoralMod.settingsManager.getBooleanValue("settings", "enablebubbles")) {
            return;
        }

        final Block aboveBlock = world.getBlock(x, y + 1, z);

        if(aboveBlock.getMaterial() != Material.water || random.nextInt(2) != 0) {
            return;
        }

        spawnBubbles(world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    private void spawnBubbles(World world, int x, int y, int z) {
        final Random rand = world.rand;
        final double offset = 0.0625;

        for(int i = 0; i < 6; i++) {
            final double x2;
            if(i == 4 && !world.getBlock(x + 1, y, z).isOpaqueCube()) {
                x2 = x + offset + 1;
            } else if(i == 5 && !world.getBlock(x - 1, y, z).isOpaqueCube()) {
                x2 = x - offset;
            } else {
                x2 = x + rand.nextFloat();
            }

            final double y2;
            if(i == 0 && !world.getBlock(x, y + 1, z).isOpaqueCube()) {
                y2 = y + offset + 1;
            } else if(i == 1 && !world.getBlock(x, y - 1, z).isOpaqueCube()) {
                y2 = y - offset;
            } else {
                y2 = y + rand.nextFloat();
            }

            final double z2;
            if(i == 2 && !world.getBlock(x, y, z + 1).isOpaqueCube()) {
                z2 = z + offset + 1;
            } else if(i == 3 && !world.getBlock(x, y, z - 1).isOpaqueCube()) {
                z2 = z - offset;
            } else {
                z2 = z + rand.nextFloat();
            }

            if(x2 < x || x2 > x + 1 || y2 < 0.0 || y2 > y + 1 || z2 < z || z2 > z + 1) {
                world.spawnParticle("bubble", x2, y2, z2, 0.0, 0.0, 0.0);
            }
        }
    }

    // metahack
    @Override
    public int damageDropped(int metadata) {
        return metadata < 8 ? metadata : metadata - 8;
    }

    @Override
    public boolean canReplace(World world, int x, int y, int z, int side, ItemStack stack) {
        // allow stacking green coral
        final int meta = (stack == null) ? -1 : stack.getItemDamage();
        if(this == CoralMod.coral1 && (meta == 1 || meta == 4)) {
            final int belowMeta = world.getBlockMetadata(x, y - 1, z);
            if(world.getBlock(x, y - 1, z) == CoralMod.coral1 && belowMeta == meta) {
                if(CoralMod.checkWater(world.getBlock(x, y + 1, z), true)) {
                    return true;
                }
            }
        }

        return canPlaceBlockAt(world, x, y, z) && canBlockStay(world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int metadata) {
        final IIcon sprite;
        if(metadata < 0 || metadata >= iconBuffer.length) {
            sprite = iconBuffer[0];
        } else {
            sprite = iconBuffer[metadata];
        }

        return sprite;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return type;
    }

    // TODO: sanitize
    // TODO: check surrounding blocks aren't air (?)
    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        // below block is reef, above block is water
        final Block belowBlock = world.getBlock(x, y - 1, z);
        final Block aboveBlock = world.getBlock(x, y + 1, z);
        if(belowBlock instanceof BlockReef && CoralMod.checkWater(aboveBlock, true)) {
            return true;
        }

        // metadata is green or spiky
        final int currentMeta = world.getBlockMetadata(x, y, z);
        if(currentMeta != 1 && currentMeta != 4) {
            return false;
        }

        // below metadata
        final int belowMeta = world.getBlockMetadata(x, y - 1, z);

        // current block is same
        final Block currentBlock = world.getBlock(x, y, z); // probably could use 'this' instead
        if(belowBlock != currentBlock && belowMeta != currentMeta) {
            return belowBlock instanceof BlockReef;
        }

        // above block is water
        if(CoralMod.checkWater(aboveBlock, true)) {
            return true;
        }

        // above metadata
        final int aboveMeta = world.getBlockMetadata(x, y + 1, z);

        // above block is same
        if(aboveBlock == currentBlock && aboveMeta == currentMeta) {
            return true;
        }

        // above block is air, but second above block is water
        final Block aboveBlock2 = world.getBlock(x, y + 2, z);
        System.err.println("aboveBlock2 " + aboveBlock2.getUnlocalizedName());
        return aboveBlock == Blocks.air && CoralMod.checkWater(aboveBlock2, true);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if(!canBlockStay(world, x, y, z)) world.func_147480_a(x, y, z, true);
    }

    // new methods

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int silkTouch) {
        world.setBlock(x, y, z, Blocks.water);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        if(CoralMod.settingsManager.getBooleanValue("settings", "light")) {
            return super.getLightValue(world, x, y, z);
        } else {
            return 0;
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        final int metadata = world.getBlockMetadata(x, y, z);
        if(!(entity instanceof EntityWaterMob) && metadata == 4) {
            entity.attackEntityFrom(DamageSource.cactus, 2);
        }

        if(entity instanceof EntityPlayer) {
            if(CoralMod.settingsManager.getBooleanValue("settings", "air")) {
                final int air = entity.getAir();
                if(air < 300) {
                    entity.setAir(air + 1);
                }
            }
        }
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconBuffer = new IIcon[types.length];

        for (int i = 0; i < types.length; i++) {
            iconBuffer[i] = iconRegister.registerIcon("coralmod:" + types[i]);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(net.minecraft.item.Item item, CreativeTabs par2CreativeTabs, List par3List) {
        // don't ask.
        par3List.add(new ItemStack(CoralMod.coral1, 1, 0));
        par3List.add(new ItemStack(CoralMod.coral1, 1, 1));
        par3List.add(new ItemStack(CoralMod.coral1, 1, 2));
        par3List.add(new ItemStack(CoralMod.coral4, 1, 3));
        par3List.add(new ItemStack(CoralMod.coral1, 1, 4));
        par3List.add(new ItemStack(CoralMod.coral5, 1, 5));
    }

}
