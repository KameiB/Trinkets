package xzeroair.trinkets.traits.elements;

import xzeroair.trinkets.init.Elements;

public class EarthElement extends Element {

	public EarthElement() {
		super("Earth");
	}

	@Override
	public Element[] getStrengths() {
		return new Element[] {
				Elements.LIGHTNING
		};
	}

}
