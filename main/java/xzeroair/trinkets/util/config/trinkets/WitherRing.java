package xzeroair.trinkets.util.config.trinkets;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import xzeroair.trinkets.util.Reference;
import xzeroair.trinkets.util.config.trinkets.shared.Attribs;
import xzeroair.trinkets.util.config.trinkets.shared.BaubleCompat;

public class WitherRing {

	private final String name = "wither_ring";
	private final String PREFIX = Reference.MODID + ".config." + name;
	private final String registry = Reference.MODID + ".config.registry";

	@Config.Comment("If an Enemy is has Wither, Should you be able to leech health")
	@Name("01. Health Leech")
	@LangKey(PREFIX + ".leech")
	public boolean leech = true;

	@Config.Comment("How much damage should you leech per hit in half hearts")
	@Name("02. Health Leech amount")
	@LangKey(PREFIX + ".leech.amount")
	public float leech_amount = 2f;

	@Name("03. Can Wither")
	@LangKey(PREFIX + ".wither")
	public boolean wither = true;

	@Config.Comment("1 in X chance to Wither an Enemy on Attack for 2 seconds")
	@Name("04. Wither Chance")
	@LangKey(PREFIX + ".wither.chance")
	public int wither_chance = 5;

	@Config.RequiresMcRestart
	@Config.Comment("Should this Item Be Registered")
	@Name("98. Item Enabled")
	@LangKey(registry + ".enabled")
	public boolean enabled = true;

	@Name("Compatability Settings")
	@LangKey(Reference.MODID + ".config.compatability")
	public Compatability compat = new Compatability();
	public class Compatability {

		@Name("Tough as Nails Compatability")
		@LangKey(Reference.MODID + ".config.toughasnails")
		private TANCompat tan = new TANCompat();
		public class TANCompat {

		}

		@Name("Baubles Compatability")
		@Config.Comment({
			"If the mod Baubles is installed what bauble slot should it use",
			"Available Types:",
			"Trinket, Any, All",
			"Amulet, Necklace, Pendant",
			"Ring, Rings",
			"Belt",
			"Head, Hat",
			"Body, Chest",
			"Charm"
		})
		@LangKey(Reference.MODID + ".config.baubles")
		public BaubleCompat baubles = new BaubleCompat("ring");
	}

	private final boolean 	armor = false;
	private final double 	armorAmount = 0;
	private final int		armorOperation = 0;
	private final boolean 	attackSpeed = false;
	private final double 	attackSpeedAmount = 0;
	private final int		attackSpeedOperation = 0;
	private final boolean 	damage = false;
	private final double 	damageAmount = 0;
	private final int		damageOperation = 0;
	private final boolean 	health = false;
	private final double 	healthAmount = 0;
	private final int		healthOperation = 0;
	private final boolean 	knockback = false;
	private final double 	knockbackAmount = 0;
	private final int		knockbackOperation = 0;
	private final boolean 	speed = false;
	private final double 	speedAmount = 0;
	private final int		speedOperation = 0;
	private final boolean 	swimSpeed = false;
	private final double 	swimSpeedAmount = 0;
	private final int		swimSpeedOperation = 0;
	private final boolean 	toughness = false;
	private final double 	toughnessAmount = 0;
	private final int		toughnessOperation = 0;
	private final boolean	luck = false;
	private final double	luckAmount = 0;
	private final int		luckOperation = 0;
	private final boolean	reach = false;
	private final double	reachAmount = 0;
	private final int		reachOperation = 0;


	@Config.Comment({"For Mor Information on Attributes", "https://minecraft.gamepedia.com/Attribute"})
	@Name("Attributes")
	@LangKey(Reference.MODID + ".config.attributes")
	public Attribs Attributes = new Attribs(
			armor, 			armorAmount, 		armorOperation,
			attackSpeed, 	attackSpeedAmount, 	attackSpeedOperation,
			damage, 		damageAmount, 		damageOperation,
			health, 		healthAmount, 		healthOperation,
			knockback, 		knockbackAmount, 	knockbackOperation,
			speed, 			speedAmount, 		speedOperation,
			swimSpeed, 		swimSpeedAmount, 	swimSpeedOperation,
			toughness, 		toughnessAmount, 	toughnessOperation,
			luck,			luckAmount,			luckOperation,
			reach,			reachAmount,		reachOperation
			);

}
