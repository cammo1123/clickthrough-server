package dev.cammo1123.clickthrough;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ClickThroughConfig {
	private static final String FILE_NAME = "config.json";

	public static final int CURRENT_VERSION = 2;

	private static final Map<ClickType, String> V1_FIELDS = Map.of(ClickType.SIGNS, "signs", ClickType.ITEM_FRAMES, "itemFrames", ClickType.GLOW_ITEM_FRAMES, "glowItemFrames", ClickType.PAINTINGS, "paintings");

	private ClickThroughConfig() {
	}

	public int version = CURRENT_VERSION;

	public Map<ClickType, Mode> modes = new EnumMap<>(ClickType.class);

	public Mode modeFor(ClickType type) {
		return modes == null || modes.get(type) == null ? type.defaultMode() : modes.get(type);
	}

	public static ClickThroughConfig load(Path configDir) {
		return new ConfigStore(configDir).data;
	}

	private static final class ConfigStore extends JsonStore<ClickThroughConfig> {
		private ConfigStore(Path configDir) {
			super(configDir, FILE_NAME, new ClickThroughConfig());
		}

		@Override
		protected Type dataType() {
			return ClickThroughConfig.class;
		}

		@Override
		protected int currentVersion() {
			return CURRENT_VERSION;
		}

		@Override
		protected boolean migrate(JsonObject root, int from, int to) {
			if (from == 1) {
				JsonObject modes = new JsonObject();

				for (Map.Entry<ClickType, String> entry : V1_FIELDS.entrySet()) {
					JsonElement legacy = root.get(entry.getValue());

					if (legacy != null && legacy.isJsonPrimitive()) {
						modes.add(entry.getKey().name(), legacy);
					} else {
						modes.addProperty(entry.getKey().name(), entry.getKey().defaultMode().name());
					}

					root.remove(entry.getValue());
				}

				root.add("modes", modes);
				root.remove("configVersion");
				return true;
			} else {
				if (ClickThroughServer.LOGGER.isWarnEnabled()) {
					ClickThroughServer.LOGGER.warn(String.format("Unknown config version %d, resetting", from));
				}
				return false;
			}
		}
	}
}
