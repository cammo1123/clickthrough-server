package dev.cammo1123.clickthrough;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerPreferences {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "players.json";
	private static final Type DATA_TYPE = new TypeToken<PlayerPreferencesData>() {
	}.getType();

	public static final int CURRENT_VERSION = 1;

	private static final PlayerPreferencesData data = load();

	private PlayerPreferences() {
	}

	private static void save() {
		Path path = path();

		try {
			Files.createDirectories(path.getParent());

			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(data, DATA_TYPE, writer);
			}
		} catch (IOException e) {
			ClickThroughServer.LOGGER.warn("Failed to write {}", FILE_NAME, e);
		}
	}

	private static Path path() {
		return FabricLoader.getInstance()
				.getConfigDir().resolve(Constants.FOLDER_NAME).resolve(FILE_NAME);
	}

	public static boolean isEnabledFor(UUID playerId) {
		PlayerPreference pref = data.players.get(playerId);
		return pref == null || !pref.disabled;
	}

	public static void setEnabled(UUID playerId, boolean enabled) {
		PlayerPreference pref = data.players.computeIfAbsent(playerId, k -> new PlayerPreference());
		boolean changed = pref.disabled == enabled;

		pref.disabled = !enabled;

		if (changed) {
			save();
		}
	}

	private static PlayerPreferencesData load() {
		Path path = path();

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				PlayerPreferencesData loaded = GSON.fromJson(reader, DATA_TYPE);
				if (loaded != null) {
					if (loaded.version != CURRENT_VERSION) {
						ClickThroughServer.LOGGER.warn(
								"{} is version {}, expected {}; using it as-is, but you may want to check its contents",
								FILE_NAME, loaded.version, CURRENT_VERSION);
						loaded.version = CURRENT_VERSION;
					}

					if (loaded.players != null) {
						return loaded;
					}
				}

			} catch (IOException e) {
				ClickThroughServer.LOGGER.warn("Failed to read {}, starting fresh", FILE_NAME, e);
			}
		}

		return new PlayerPreferencesData();
	}

	private static class PlayerPreferencesData {
		public int version = CURRENT_VERSION;
		public Map<UUID, PlayerPreference> players = new HashMap<>();
	}

	private static class PlayerPreference {
		public boolean disabled = false;
	}
}