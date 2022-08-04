package com.nibblepoker.expandedironbundles.enums;

public enum EBundleUpgrades {
	POWERED_LAUNCHER("powered_launcher"),
	COMPOSTING("composting");
	
	// TODO: Add the food storage here ?
	
	private final String nbtKey;
	
	EBundleUpgrades(final String nbtKey) {
		this.nbtKey = nbtKey;
	}
	
	public String getNbtKey() {
		return nbtKey;
	}
}
