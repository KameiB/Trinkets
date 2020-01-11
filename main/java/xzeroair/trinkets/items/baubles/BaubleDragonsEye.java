package xzeroair.trinkets.items.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import xzeroair.trinkets.init.ModItems;
import xzeroair.trinkets.items.trinkets.TrinketDragonsEye;

public class BaubleDragonsEye extends TrinketDragonsEye implements IBauble {

	public BaubleDragonsEye(String name) {
		super(name);
		if(ModItems.trinkets.ITEMS.contains(this)) {
			ModItems.trinkets.ITEMS.remove(this);
		}
		ModItems.baubles.ITEMS.add(this);
	}
	@Override
	public BaubleType getBaubleType(ItemStack itemstack) {
		return BaubleType.HEAD;
	}
	@Override
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
		if(player instanceof EntityPlayer) {
			return super.playerCanEquip(itemstack, (EntityPlayer) player);
		}
		return true;
	}
	@Override
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
		if(player instanceof EntityPlayer) {
			return super.playerCanUnequip(itemstack, (EntityPlayer)player);
		}
		return true;
	}
	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase player) {
		if(player instanceof EntityPlayer) {
			super.playerEquipped(stack, (EntityPlayer)player);
		}
	}
	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		if(player instanceof EntityPlayer) {
			super.playerUnequipped(stack, (EntityPlayer)player);
		}
	}
}