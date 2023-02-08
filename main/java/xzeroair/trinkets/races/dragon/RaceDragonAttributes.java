package xzeroair.trinkets.races.dragon;

import xzeroair.trinkets.races.RaceAttributesWrapper;
import xzeroair.trinkets.races.dragon.config.DragonConfig;
import xzeroair.trinkets.util.TrinketsConfig;

public class RaceDragonAttributes extends RaceAttributesWrapper {

	public static final DragonConfig serverConfig = TrinketsConfig.SERVER.races.dragon;

	public RaceDragonAttributes() {
		size = 120;
		width = size;
		height = size;
		color1 = 3289650;
		color2 = 9509561;
		color3 = color1;
		opacity = 1f;
		trait_opacity = 1F;
		attributes = serverConfig.attributes;
	}
}
