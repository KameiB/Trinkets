package xzeroair.trinkets.items.trinkets;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xzeroair.trinkets.api.TrinketHelper;
import xzeroair.trinkets.capabilities.Capabilities;
import xzeroair.trinkets.capabilities.Trinket.TrinketProperties;
import xzeroair.trinkets.items.base.AccessoryBase;
import xzeroair.trinkets.util.TrinketsConfig;
import xzeroair.trinkets.util.helpers.AttributeHelper;
import xzeroair.trinkets.util.helpers.TranslationHelper;

public class TrinketPoison extends AccessoryBase {

	public TrinketPoison(String name) {
		super(name);
	}

	private static UUID uuid = UUID.fromString("e86e5b58-1b62-4a54-bba1-6594de844c2e");

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		TranslationHelper.addOtherTooltips(stack, worldIn, TrinketsConfig.SERVER.POISON_STONE.Attributes, tooltip);
	}

	@Override
	public void eventPlayerTick(ItemStack stack, EntityPlayer player) {
		AttributeHelper.handleAttributes(player, TrinketsConfig.SERVER.POISON_STONE.Attributes, uuid);
		if (player.isPotionActive(MobEffects.POISON)) {
			player.removeActivePotionEffect(MobEffects.POISON);
		}
		if (player.isPotionActive(MobEffects.HUNGER)) {
			player.removeActivePotionEffect(MobEffects.HUNGER);
		}
	}

	@Override
	public void eventLivingHurt(LivingHurtEvent event, ItemStack stack, EntityLivingBase player) {
		if (TrinketsConfig.SERVER.POISON_STONE.bonus_damage) {
			if (!event.getEntityLiving().isDead) {
				if (event.getSource().getTrueSource() == player) {
					if (TrinketsConfig.SERVER.POISON_STONE.poison && !event.getEntityLiving().isPotionActive(MobEffects.POISON)) {
						final Random rand = new Random();
						if (rand.nextInt(TrinketsConfig.SERVER.POISON_STONE.poison_chance) == 0) {
							event.getEntityLiving().addPotionEffect(new PotionEffect(MobEffects.POISON, 40, 0, false, true));
						}
					}
					if (event.getEntityLiving().isPotionActive(MobEffects.POISON)) {
						event.setAmount(event.getAmount() * TrinketsConfig.SERVER.POISON_STONE.bonus_damage_amount);
					}
				}
			}
		}
	}

	@Override
	public boolean playerCanEquip(ItemStack stack, EntityLivingBase player) {
		if (TrinketHelper.AccessoryCheck(player, stack.getItem())) {
			return false;
		} else {
			return super.playerCanEquip(stack, player);
		}
	}

	@Override
	public void eventPlayerLogout(ItemStack stack, EntityLivingBase player) {
		AttributeHelper.removeAttributes(player, uuid);
	}

	@Override
	public void playerEquipped(ItemStack stack, EntityLivingBase player) {
		TrinketProperties cap = Capabilities.getTrinketProperties(stack);
		if ((cap != null)) {
			if (!(cap.Slot() == -1)) {
				super.playerEquipped(stack, player);
			} else {
				player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 1.9f);
				AttributeHelper.handleAttributes(player, TrinketsConfig.SERVER.POISON_STONE.Attributes, uuid);
			}
		}
	}

	@Override
	public void playerUnequipped(ItemStack stack, EntityLivingBase player) {
		player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 2f);
		AttributeHelper.removeAttributes(player, uuid);
		super.playerUnequipped(stack, player);
	}

	@Override
	public boolean ItemEnabled() {
		return TrinketsConfig.SERVER.POISON_STONE.enabled;
	}

	@Override
	public boolean hasDiscription(ItemStack stack) {
		return true;
	}
}