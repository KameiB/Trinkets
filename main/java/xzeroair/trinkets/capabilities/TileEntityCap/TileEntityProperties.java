package xzeroair.trinkets.capabilities.TileEntityCap;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xzeroair.trinkets.capabilities.Capabilities;
import xzeroair.trinkets.capabilities.CapabilityBase;
import xzeroair.trinkets.util.Reference;
import xzeroair.trinkets.util.TrinketsConfig;
import xzeroair.trinkets.util.handlers.Counter;
import xzeroair.trinkets.util.helpers.BlockHelperUtil;

public class TileEntityProperties extends CapabilityBase<TileEntityProperties, TileEntity> {

	protected boolean hasEssence = false;
	protected int essence = -1;

	public TileEntityProperties(TileEntity te) {
		super(te);
	}

	@Override
	public NBTTagCompound getTag() {
		NBTTagCompound teTag = object.getTileData();
		if (teTag != null) {
			final String capTag = Reference.MODID + ".TrinketTE";
			if (!teTag.hasKey(capTag)) {
				teTag.setTag(capTag, new NBTTagCompound());
			}
			tag = teTag.getCompoundTag(capTag);
		}
		return super.getTag();
	}

	public TileEntityProperties setHasEssence(boolean has) {
		return this.setHasEssence(has, TrinketsConfig.SERVER.mana.essence_amount);
	}

	public TileEntityProperties setHasEssence(boolean has, int essence) {
		hasEssence = has;
		this.essence = essence;
		return this;
	}

	@Override
	public void onUpdate() {
		if (this.hasEssence()) {
			this.updateEssence();
		} else {
			object.getWorld().setBlockToAir(object.getPos());
		}
	}

	private void updateEssence() {
		final World world = object.getWorld();
		final BlockPos tePos = object.getPos();
		if ((world == null) || world.isRemote) {
			return;
		}

		final int teEssence = this.getEssence();
		final double range = 5.0;
		final double rangeY = 2.0;
		final BlockPos pos1 = object.getPos().add(-range, -rangeY, -range);
		final BlockPos pos2 = object.getPos().add(range, rangeY, range);

		final boolean skip = BlockHelperUtil.isBlockNearby(world, new AxisAlignedBB(pos1, pos2), (state, pos) -> {
			final Block block = state.getBlock();
			final TileEntity te = world.getTileEntity(pos);
			final boolean isSelf = tePos.equals(pos);
			if ((te == null) || isSelf || (te == object)) {
				return false;
			}
			return Capabilities.getTEProperties(te, false, (prop, matches) -> prop.hasEssence());
		});
		if (skip) {
			return;
		}
		final List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(object.getPos()).grow(4));
		if (!entities.isEmpty()) {
			for (final EntityLivingBase e : entities) {
				Capabilities.getMagicStats(
						e, prop -> {
							if (e.isSneaking()) {
								final Counter counter = this.getTickHandler().getCounter("absorb.cooldown", TrinketsConfig.SERVER.mana.essence_cooldown, false, true, true, false);
								if (counter.Tick()) {
									final double currentBonus = prop.getBonusMana();
									double addedAmount = 1;
									prop.setBonusMana(currentBonus + addedAmount);
									this.setEssence(teEssence - 1);
									Random rand = Reference.random;
									world.playSound((EntityPlayer) null, tePos, SoundEvents.ENTITY_ILLAGER_CAST_SPELL, SoundCategory.BLOCKS, 0.4F, (rand.nextFloat() * 0.6F) + 0.4F);
								}
							}
						}
				);
			}
		}
		if (teEssence != this.getEssence()) {
			this.saveToNBT(this.getTag());
			object.markDirty();
		}
	}

	public boolean hasEssence() {
		return hasEssence && (this.getEssence() > 0);
	}

	public int getEssence() {
		return essence;
	}

	public void setEssence(int essence) {
		if (this.essence != essence) {
			this.essence = essence;
		}
	}

	@Override
	public NBTTagCompound saveToNBT(NBTTagCompound compound) {
		compound.setBoolean("provides", hasEssence);
		compound.setInteger("essence", essence);
		return compound;
	}

	@Override
	public void loadFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("provides")) {
			hasEssence = compound.getBoolean("provides");
		}
		if (compound.hasKey("essence")) {
			essence = compound.getInteger("essence");
		}
	}

}
