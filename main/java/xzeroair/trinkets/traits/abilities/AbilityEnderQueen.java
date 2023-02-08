package xzeroair.trinkets.traits.abilities;

import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xzeroair.trinkets.api.TrinketHelper;
import xzeroair.trinkets.capabilities.Capabilities;
import xzeroair.trinkets.capabilities.Vip.VipStatus;
import xzeroair.trinkets.capabilities.magic.MagicStats;
import xzeroair.trinkets.client.keybinds.ModKeyBindings;
import xzeroair.trinkets.init.Abilities;
import xzeroair.trinkets.init.ModItems;
import xzeroair.trinkets.init.TrinketsDamageSource;
import xzeroair.trinkets.traits.abilities.interfaces.IAttackAbility;
import xzeroair.trinkets.traits.abilities.interfaces.IKeyBindInterface;
import xzeroair.trinkets.traits.abilities.interfaces.IPotionAbility;
import xzeroair.trinkets.traits.abilities.interfaces.ITickableAbility;
import xzeroair.trinkets.util.Reference;
import xzeroair.trinkets.util.TrinketsConfig;
import xzeroair.trinkets.util.compat.lycanitesmobs.LycanitesCompat;
import xzeroair.trinkets.util.config.trinkets.ConfigEnderCrown;
import xzeroair.trinkets.util.handlers.Counter;
import xzeroair.trinkets.util.helpers.StringUtils;

public class AbilityEnderQueen extends Ability implements ITickableAbility, IPotionAbility, IAttackAbility, IKeyBindInterface {

	public AbilityEnderQueen() {
		super(Abilities.enderQueen);
	}

	protected static final ConfigEnderCrown serverConfig = TrinketsConfig.SERVER.Items.ENDER_CROWN;

	@Override
	public void tickAbility(EntityLivingBase entity) {
		if (entity.world.isRemote) {
			return;
		}
		LycanitesCompat.removeInstability(entity);
		if (serverConfig.water_hurts) {
			final Counter counter = tickHandler.getCounter("water_hurt", 20, true, true, true, true);
			if (counter.Tick()) {
				if ((entity.isInWater() || entity.isWet())) {
					final MagicStats magic = Capabilities.getMagicStats(entity);
					if ((magic != null) && magic.spendMana(5F)) {
						magic.setManaRegenTimeout(TrinketsConfig.SERVER.mana.mana_regen_timeout * 2);
					} else {
						if (TrinketHelper.AccessoryCheck(entity, ModItems.trinkets.TrinketDragonsEye)) {
							entity.attackEntityFrom(TrinketsDamageSource.water.setDamageBypassesArmor().setMagicDamage(), 4);
						} else {
							entity.attackEntityFrom(TrinketsDamageSource.water.setDamageBypassesArmor().setMagicDamage(), 2);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean potionApplied(EntityLivingBase entity, PotionEffect effect, boolean cancel) {
		final String e = effect.getPotion().getRegistryName().toString();
		final Potion instability = LycanitesCompat.getPotionEffectByName("instability");
		if ((instability != null) && e.contentEquals(instability.getRegistryName().toString())) {
			return true;
		}
		return cancel;
	}

	@Override
	public boolean attacked(EntityLivingBase attacked, DamageSource source, float dmg, boolean cancel) {
		final boolean client = attacked.getEntityWorld().isRemote;
		final Entity attacker = source.getTrueSource();
		if ((attacker instanceof EntityLivingBase) && (attacker != attacked) && (dmg > 0)) {
			if ((serverConfig.dmgChance || serverConfig.spawnChance)) {
				final int chanceNum = serverConfig.chance;
				if (chanceNum > 0) {
					final int chance = random.nextInt(chanceNum);
					if ((chance == 0)) {
						String string = "The void protects me!";
						final String string2 = "Go, my loyal subject!";
						if (TrinketsConfig.SERVER.misc.retrieveVIP) {
							final VipStatus vip = Capabilities.getVipStatus(attacked);
							if (vip != null) {
								final String quote = vip.getRandomQuote();
								if (!quote.isEmpty()) {
									string = quote;
								}
							}
						}
						if (serverConfig.spawnChance) {
							if (client) {
								StringUtils.sendMessageToPlayer(attacked, TextFormatting.BOLD + "" + TextFormatting.GOLD + string2, false);
							}
							if (!client) {
								try {
									final EntityEnderman knight = new EntityEnderman(attacked.getEntityWorld());
									final double x = attacked.getPosition().getX();
									final double y = attacked.getPosition().getY();
									final double z = attacked.getPosition().getZ();
									knight.setPosition(x, y, z);
									knight.getEntityData().setBoolean("xat:summoned", true);
									knight.setCanPickUpLoot(false);
									attacked.getEntityWorld().spawnEntity(knight);
									knight.setAttackTarget((EntityLivingBase) attacker);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						if (serverConfig.dmgChance) {
							cancel = true;
							if (client) {
								StringUtils.sendMessageToPlayer(attacked, TextFormatting.BOLD + "" + TextFormatting.GOLD + string, false);
							}
						}
					}
				}
			}
			if (serverConfig.teleportOnHurt && (source instanceof EntityDamageSourceIndirect)) {
				final int chanceNum = serverConfig.teleportChance; // Check the chance to make sure it properly works and doesn't crash if someone stupid decides to set the value to 0
				if (chanceNum > 0) {
					final int chance = random.nextInt(chanceNum);
					if ((chance == 0)) {
						final boolean isBoss = (source.getTrueSource() != null) && !source.getTrueSource().isNonBoss() ? true : false;
						final MagicStats magic = Capabilities.getMagicStats(attacked);
						if (!isBoss && !attacked.isActiveItemStackBlocking()) {
							if ((magic != null) && (magic.getMana() >= (magic.getMaxMana() * 0.5F))) {
								for (int i = 0; i < 32; ++i) {
									if (this.teleportRandomly(attacked) && magic.spendMana(magic.getMaxMana() * 0.5F)) {
										cancel = true;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		return cancel;
	}

	@Override
	public void targetedByEnemy(EntityLivingBase enemy) {
		if (!serverConfig.attackBack && (enemy instanceof EntityEnderman)) {
			if (enemy instanceof EntityLiving) {
				EntityLivingBase target = ((EntityLiving) enemy).getAttackTarget();
				if (target != null) {
					((EntityLiving) enemy).setAttackTarget(null);
				}
			}
		}
	}

	@Override
	public int killedEntityExpDrop(EntityLivingBase target, int originalExp, int droppedExp) {
		if (!serverConfig.expDrop && (target instanceof EntityEnderman)) {
			return 0;
		}
		return droppedExp;
	}

	@Override
	public void killedEntityItemDrops(EntityLivingBase target, DamageSource source, int lootingLevel, List<EntityItem> drops) {
		if (!serverConfig.itemDrop && (target instanceof EntityEnderman)) {
			if (!drops.isEmpty()) {
				drops.clear();
			}
		}
	}

	@Override
	public float damageEntity(EntityLivingBase target, DamageSource source, float dmg) {
		if ((target instanceof EntityEnderman) && (dmg > 0)) {
			try {
				final EntityEnderman enderman = (EntityEnderman) target;
				NBTTagCompound data = enderman.getEntityData();
				data.setBoolean("xat:summoned", true);
			} catch (Exception e) {
			}
		}
		return dmg;
	}

	protected boolean teleportRandomly(EntityLivingBase entity) {
		final double d0 = entity.posX + ((Reference.random.nextDouble() - 0.5D) * 32.0D);
		final double d1 = entity.posY + (Reference.random.nextInt(16) - 8);
		final double d2 = entity.posZ + ((Reference.random.nextDouble() - 0.5D) * 32.0D);
		final boolean teleport = this.teleportTo(entity, d0, d1, d2);
		return teleport;
	}

	private boolean teleportTo(EntityLivingBase entity, double x, double y, double z) {
		final net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(entity, x, y, z, 0);
		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
			return false;
		}
		if (entity instanceof EntityPlayer) {
			if (entity instanceof EntityPlayerMP) {
				if (entity.isRiding()) {
					entity.dismountRidingEntity();
				}
				if (entity.attemptTeleport(x, y, z)) {

					if (entity.fallDistance > 0.0F) {
						entity.fallDistance = 0.0F;
					}
					entity.lastTickPosX = entity.posX;
					entity.lastTickPosY = entity.posY;
					entity.lastTickPosZ = entity.posZ;
					entity.world.playSound(
							null, x, y, z,
							SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f
					);
					return true;
				}
			}
			//			} else {
			return false;
		} else {
			final boolean flag = this.attemptTeleport(entity, x, y, z);
			if (flag) {
				entity.world.playSound(null, entity.prevPosX, entity.prevPosY, entity.prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, entity.getSoundCategory(), 1.0F, 1.0F);
				entity.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
			}

			return flag;
		}
	}

	protected boolean teleportToEntity(EntityLivingBase entity, Entity target) {
		Vec3d vec3d = new Vec3d(entity.posX - target.posX, ((entity.getEntityBoundingBox().minY + (entity.height / 2.0F)) - target.posY) + target.getEyeHeight(), entity.posZ - target.posZ);
		vec3d = vec3d.normalize();
		final double d0 = 16.0D;
		final double d1 = (entity.posX + ((Reference.random.nextDouble() - 0.5D) * 8.0D)) - (vec3d.x * d0);
		final double d2 = (entity.posY + (Reference.random.nextInt(16) - 8)) - (vec3d.y * 16.0D);
		final double d3 = (entity.posZ + ((Reference.random.nextDouble() - 0.5D) * 8.0D)) - (vec3d.z * d0);
		return this.teleportTo(entity, d1, d2, d3);
	}

	protected boolean attemptTeleport(EntityLivingBase entity, double x, double y, double z) {
		final double d0 = entity.posX;
		final double d1 = entity.posY;
		final double d2 = entity.posZ;
		entity.posX = x;
		entity.posY = y;
		entity.posZ = z;
		boolean flag = false;
		BlockPos blockpos = new BlockPos(entity);
		final World world = entity.world;
		final Random random = this.random;

		if (world.isBlockLoaded(blockpos)) {
			boolean flag1 = false;

			while (!flag1 && (blockpos.getY() > 0)) {
				final BlockPos blockpos1 = blockpos.down();
				final IBlockState iblockstate = world.getBlockState(blockpos1);

				if (iblockstate.getMaterial().blocksMovement()) {
					flag1 = true;
				} else {
					--entity.posY;
					blockpos = blockpos1;
				}
			}

			if (flag1) {
				//				entity.setPositionAndUpdate(d0, d1, d2);
				entity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);

				if (world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && !world.containsAnyLiquid(entity.getEntityBoundingBox())) {
					flag = true;
				}
			}
		}

		if (!flag) {
			entity.setPositionAndUpdate(d0, d1, d2);
			return false;
		} else {
			final int i = 128;

			for (int j = 0; j < 128; ++j) {
				final double d6 = j / 127.0D;
				final float f = (random.nextFloat() - 0.5F) * 0.2F;
				final float f1 = (random.nextFloat() - 0.5F) * 0.2F;
				final float f2 = (random.nextFloat() - 0.5F) * 0.2F;
				final double d3 = d0 + ((entity.posX - d0) * d6) + ((random.nextDouble() - 0.5D) * entity.width * 2.0D);
				final double d4 = d1 + ((entity.posY - d1) * d6) + (random.nextDouble() * entity.height);
				final double d5 = d2 + ((entity.posZ - d2) * d6) + ((random.nextDouble() - 0.5D) * entity.width * 2.0D);
				world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, f, f1, f2);
			}

			if (entity instanceof EntityCreature) {
				((EntityCreature) entity).getNavigator().clearPath();
			}

			return true;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getKey() {
		return ModKeyBindings.RACE_ABILITY.getDisplayName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getAuxKey() {
		return ModKeyBindings.AUX_KEY.getDisplayName();
	}

	@Override
	public boolean onKeyPress(Entity entity, boolean Aux) {
		final World world = entity.getEntityWorld();
		//		return Capabilities.getMagicStats(entity, true, (magic, allow) -> {
		//			boolean test = TrinketsConfig.SERVER.Items.ENDER_CROWN.attackBack;
		//			final float cost = (float) StringUtils.getAccurateDouble((magic.getMaxMana() * 0.1));
		//			if ((magic.getMana() >= cost)) {
		//				for (int i = 0; i < 64; ++i) {
		//					if (this.teleportRandomly((EntityLivingBase) entity)) {
		//						if (magic.spendMana(cost)) {
		//						}
		//						break;
		//					}
		//				}
		//			}
		//			return allow;
		//		});
		//		if ((entity instanceof EntityPlayer) && !world.isRemote) {
		//			EntityPlayer player = (EntityPlayer) entity;
		//			InventoryEnderChest inventoryenderchest = player.getInventoryEnderChest();
		//			player.displayGUIChest(inventoryenderchest);
		//			player.addStat(StatList.ENDERCHEST_OPENED);
		//
		//		}
		return true;
	}
}
