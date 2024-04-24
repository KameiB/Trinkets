package xzeroair.trinkets.traits.elements;

import xzeroair.trinkets.init.Elements;

public class LightElement extends Element {

	public LightElement() {
		super("Light");
	}

	@Override
	public Element[] getStrengths() {
		return new Element[] {
				Elements.DARK
		};
	}

	@Override
	public Element[] getWeaknesses() {
		return new Element[] {
				Elements.DARK
		};
	}

}
