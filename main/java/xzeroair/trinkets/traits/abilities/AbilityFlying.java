package xzeroair.trinkets.traits.abilities;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import xzeroair.trinkets.capabilities.Capabilities;
import xzeroair.trinkets.capabilities.magic.MagicStats;
import xzeroair.trinkets.init.Abilities;
import xzeroair.trinkets.traits.abilities.interfaces.IMiningAbility;
import xzeroair.trinkets.traits.abilities.interfaces.IPotionAbility;
import xzeroair.trinkets.traits.abilities.interfaces.ITickableAbility;
import xzeroair.trinkets.util.handlers.Counter;
import xzeroair.trinkets.util.helpers.PotionHelper;

public class AbilityFlying extends Ability implements ITickableAbility, IPotionAbility, IMiningAbility {

	protected boolean flightEnabled = false;
	protected boolean speedModified = false;
	protected float speed = 0.05F;
	protected float currentSpeed = 0.05F;
	protected float cost = 0F;
	protected boolean selfAdded = false;
	protected boolean selfSpeedModified = false;

	public AbilityFlying() {
		super(Abilities.creativeFlight);
	}

	public AbilityFlying(boolean enabled, boolean speedmodified, float speed, float cost) {
		this();
		flightEnabled = enabled;
		speedModified = speedmodified;
		this.speed = speed;
		this.cost = cost;
	}

	public AbilityFlying setFlightEnabled(boolean enabled) {
		if (flightEnabled != enabled) {
			flightEnabled = enabled;
		}
		return this;
	}

	public AbilityFlying setSpeedEnabled(boolean enabled) {
		if (speedModified != enabled) {
			speedModified = enabled;
		}
		return this;
	}

	public AbilityFlying setFlightSpeed(float speed) {
		if (this.speed != speed) {
			this.speed = speed;
		}
		return this;
	}

	public AbilityFlying setFlightCost(float cost) {
		if (this.cost != cost) {
			this.cost = cost;
		}
		return this;
	}

	@Override
	public void tickAbility(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			if (!this.isCreativePlayer(player)) {
				// TODO Fix this so it's not reliant on the Race capability
				final boolean flag = Capabilities.getEntityProperties(player, true, (prop, canFly) -> prop.getRaceHandler().canFly());
				if (flightEnabled && flag) {
					this.addFlyingAbility(player);
				} else {
					this.removeCreativeFlight(player);
				}
			}
			this.handleSpeed(player);
		}
	}

	@Override
	public boolean potionApplied(EntityLivingBase entity, PotionEffect effect, boolean cancel) {
		if (effect.getPotion().getRegistryName().toString().contentEquals("minecraft:levitation")) {
			return true;
		}
		return cancel;
	}

	@Override
	public float breakingBlock(EntityLivingBase entity, IBlockState state, BlockPos pos, float originalSpeed, float newSpeed) {
		if (!entity.isInsideOfMaterial(Material.WATER)) {
			float speed = originalSpeed;
			if (!entity.onGround) {
				speed *= 5F;
			}
			if (newSpeed < speed) {
				return speed;
			}
		}
		return newSpeed;
	}

	protected boolean setFlyingSpeed(EntityPlayer player, float flightSpeed) {
		if (player.world.isRemote) {
			float flySpeed = player.capabilities.getFlySpeed();
			if ((flySpeed != flightSpeed) && !Float.isNaN(flightSpeed)) {
				player.capabilities.setFlySpeed(flightSpeed);
				//				player.sendPlayerAbilities();
				return true;
			}
		}
		return false;
	}

	protected void handleSpeed(EntityPlayer player) {
		if (!player.world.isRemote) {
			return;
		}
		boolean flying = player.capabilities.isFlying;
		final float defaultSpeed = 0.05F;
		if (!flying) {
			if (selfSpeedModified) {
				if (this.setFlyingSpeed(player, defaultSpeed)) {
					selfSpeedModified = false;
					//					player.sendPlayerAbilities();
				}
			}
			return;
		}
		final float flySpeed = player.capabilities.getFlySpeed();
		if (!selfSpeedModified || (currentSpeed != flySpeed)) {
			float newSpeed = Math.max((flySpeed - defaultSpeed) + speed, 0.0F);
			if (this.setFlyingSpeed(player, newSpeed)) {
				currentSpeed = newSpeed;
				if (!selfSpeedModified) {
					//					player.sendPlayerAbilities();
					selfSpeedModified = true;
				}
			}
		}
	}

	protected void addFlyingAbility(EntityPlayer player) {
		if (this.isCreativePlayer(player)) {
			return;
		}
		if (cost <= 0) {
			this.giveCreativeFlight(player);
			if (selfAdded && player.capabilities.isFlying) {
				player.fallDistance = 0F;
			}
		} else {
			final MagicStats magic = Capabilities.getMagicStats(player);
			if (magic != null) {
				final float mp = magic.getMana();
				if (mp >= cost) {
					this.giveCreativeFlight(player);
				} else {
					if (selfAdded) {
						this.removeCreativeFlight(player);
					}
				}
				if (PotionHelper.isModPotionActive(player, "potioncore", "flight")) {
					return;
				}
				if (selfAdded && player.capabilities.isFlying) {
					player.fallDistance = 0F;
					if (!player.isRiding()) {
						final Counter counter = tickHandler.getCounter("fly_timer", 20, true, true, true, true);
						if ((counter != null) && counter.Tick()) {
							if (!magic.spendMana(cost)) {
								this.removeCreativeFlight(player);
								//							} else {
								//		magic.setManaRegenTimeout(TrinketsConfig.SERVER.mana.mana_regen_timeout * 3);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onAbilityRemoved(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			final EntityPlayer player = (EntityPlayer) entity;
			if (!this.isCreativePlayer(player)) {
				this.removeCreativeFlight(player);
			}
			this.setFlyingSpeed(player, 0.05F);
		}
		tickHandler.removeCounter("fly_timer");
	}

	private void removeCreativeFlight(EntityPlayer player) {
		if (selfAdded) {
			selfAdded = false;
		}
		if (PotionHelper.isModPotionActive(player, "potioncore", "flight")) {
			return;
		}
		if (player.capabilities.allowFlying) {
			player.capabilities.allowFlying = false;
			if (player.capabilities.isFlying) {
				player.fallDistance = 0F;
				player.capabilities.isFlying = false;
			}
			if (player instanceof EntityPlayerMP) {
				player.sendPlayerAbilities();
			}
		}
	}

	private void giveCreativeFlight(EntityPlayer player) {
		if (!player.capabilities.allowFlying) {
			player.capabilities.allowFlying = true;
			selfAdded = true;
			if (player instanceof EntityPlayerMP) {
				player.sendPlayerAbilities();
			}
		}
	}
}
