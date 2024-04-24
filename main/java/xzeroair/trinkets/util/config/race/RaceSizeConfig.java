package xzeroair.trinkets.util.config.race;

import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;

public class RaceSizeConfig {

	public RaceSizeConfig(int height, int width) {
		this.height = height;
		this.width = width;
	}

	@Name("Height")
	@LangKey("xat.config.client.magic.hud.height")
	public int height;
	@Name("Width")
	@LangKey("xat.config.client.magic.hud.width")
	public int width;

}
