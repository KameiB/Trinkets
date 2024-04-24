package xzeroair.trinkets.capabilities.Vip;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import xzeroair.trinkets.capabilities.CapabilityBase;
import xzeroair.trinkets.network.NetworkHandler;
import xzeroair.trinkets.network.vip.VipStatusPacket;
import xzeroair.trinkets.util.Reference;
import xzeroair.trinkets.vip.VIPHandler;
import xzeroair.trinkets.vip.VipPackage;
import xzeroair.trinkets.vip.VipUser;

public class VipStatus extends CapabilityBase<VipStatus, EntityPlayer> {

	private EntityPlayer player;
	private int status = 0;
	private boolean checkStatus;
	private List<String> Quotes;

	public VipStatus(EntityPlayer player) {
		super(player);
		this.player = player;
		Quotes = new ArrayList<>();
	}

	@Override
	public NBTTagCompound getTag() {
		NBTTagCompound tag = object.getEntityData();
		if (tag != null) {
			if (!tag.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
				tag.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
			}
			final NBTTagCompound persistentData = tag.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			final String capTag = Reference.MODID + ".VIP";
			if (!persistentData.hasKey(capTag)) {
				persistentData.setTag(capTag, new NBTTagCompound());
			}
			return persistentData.getCompoundTag(capTag);
		}
		return super.getTag();
	}

	@Override
	public void onUpdate() {
		if ((checkStatus != true)) {
			final World world = player.getEntityWorld();
			if (world == null) {
				return;
			}
			if (!world.isRemote) {
				try {
					final TreeMap<String, VipUser> list = VIPHandler.instance.getVips();
					if ((list != null) && !list.isEmpty()) {
						this.confirmedStatus();
						this.sendStatusToPlayer(player);
					}
				} catch (Exception e) {
				}
			}
			checkStatus = true;
		}
	}

	private void confirmedStatus() {
		final String id = player.getUniqueID().toString().replaceAll("-", "");
		if (VIPHandler.instance.getVips().containsKey(id)) {
			VipUser user = VIPHandler.instance.getVips().get(id);
			if (user != null) {
				if (!user.getGroups().isEmpty()) {
					final VipPackage group1 = user.getGroups().get(0);
					if (group1 != null) {
						status = group1.getGroupID();
					}
				}
				Quotes = user.getQuotes();
			}
		}
	}

	public void sendStatusToPlayer(EntityPlayer reciever) {
		final World world = player.getEntityWorld();
		if ((world != null) && !world.isRemote && (reciever instanceof EntityPlayerMP)) {
			try {
				final NBTTagCompound tag = new NBTTagCompound();
				this.saveToNBT(tag);
				NetworkHandler.sendTo(new VipStatusPacket(player, tag), (EntityPlayerMP) reciever);
			} catch (final Exception e) {
			}
		}
	}

	public void syncStatusToTracking() {
		final World world = player.getEntityWorld();
		if ((player instanceof EntityPlayerMP) && (world instanceof WorldServer)) {
			final NBTTagCompound tag = new NBTTagCompound();
			this.saveToNBT(tag);
			NetworkHandler.sendToClients((WorldServer) world, player.getPosition(), new VipStatusPacket(player, tag));
		}
	}

	public List<String> getQuotes() {
		return Quotes;
	}

	public String getRandomQuote() {
		if (!Quotes.isEmpty()) {
			final int rand = Reference.random.nextInt(Quotes.size());
			return Quotes.get(rand);
		} else {
			return "";
		}
	}

	public int getStatus() {
		return status;
	}

	@Override
	public void copyFrom(VipStatus source, boolean wasDeath, boolean keepInv) {
		status = source.status;
	}

	@Override
	public NBTTagCompound saveToNBT(NBTTagCompound compound) {
		compound.setInteger("status", status);
		return compound;
	}

	@Override
	public void loadFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("status")) {
			status = compound.getInteger("status");
		}
	}
}