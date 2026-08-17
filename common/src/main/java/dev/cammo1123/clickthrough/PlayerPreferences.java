package dev.cammo1123.clickthrough;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class PlayerPreferencesData {
	public static final int CURRENT_VERSION = 2;
	public int version = CURRENT_VERSION;
	public Map<UUID, Boolean> disabled = new HashMap<>();
}

public final class PlayerPreferences extends JsonStore<PlayerPreferencesData> {
	private static final String FILE_NAME = "players.json";
	private static final String V1_PLAYERS_FIELD = "players";

	public PlayerPreferences(Path configDir) {
		super(configDir, FILE_NAME, new PlayerPreferencesData());
	}

	public boolean isEnabledFor(UUID playerId) {
		return !data.disabled.getOrDefault(playerId, false);
	}

	public void setEnabled(UUID playerId, boolean enabled) {
		boolean changed;
		if (enabled) {
			changed = data.disabled.remove(playerId) != null;
		} else {
			changed = data.disabled.put(playerId, true) == null;
		}

		if (changed) {
			save();
		}
	}

	@Override
	protected boolean isValid(PlayerPreferencesData data) {
		return data.disabled != null;
	}

	@Override
	protected Type dataType() {
		return PlayerPreferencesData.class;
	}

	@Override
	protected int currentVersion() {
		return PlayerPreferencesData.CURRENT_VERSION;
	}

	@Override
	protected boolean migrate(JsonObject root, int from, int to) {
		if (from == 1) {
			JsonElement v1LegacyPlayers = root.remove(V1_PLAYERS_FIELD);
			if (v1LegacyPlayers == null || !v1LegacyPlayers.isJsonObject()) {
				return true;
			}

			JsonObject disabled = new JsonObject();
			for (Map.Entry<String, JsonElement> entry : v1LegacyPlayers.getAsJsonObject().entrySet()) {
				// v1 stored {"players": {"<uuid>": {"disabled": true}}}; keep only
				// the players who actually opted out.
				JsonElement pref = entry.getValue();
				if (pref != null && pref.isJsonObject()) {
					JsonElement wasDisabled = pref.getAsJsonObject().get("disabled");
					if (wasDisabled != null && wasDisabled.getAsBoolean()) {
						disabled.addProperty(entry.getKey(), true);
					}
				}
			}

			root.add("disabled", disabled);
			return true;
		} else {
			if (ClickThroughServer.LOGGER.isWarnEnabled()) {
				ClickThroughServer.LOGGER.warn(String.format("Unknown config version %d, resetting", from));
			}
			return false;
		}
	}
}
