package dev.cammo1123.clickthrough;

public enum Mode {
	NORMAL, NORMAL_WHEN_SNEAKING, CLICKTHROUGH_WHEN_SNEAKING;

	public boolean shouldClickThrough(boolean sneaking) {
		return switch (this) {
			case NORMAL -> false;
			case NORMAL_WHEN_SNEAKING -> !sneaking;
			case CLICKTHROUGH_WHEN_SNEAKING -> sneaking;
		};
	}
}
