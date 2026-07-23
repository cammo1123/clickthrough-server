package dev.cammo1123.clickthrough;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Per-object-type click-through behaviour, persisted as JSON at
 * config/clickthrough-server/config.json. Each category (signs, item frames,
 * glow item frames, paintings) can independently be set to one of the
 * {@link Mode} values below.
 */
public class ClickThroughConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "config.json";

	public static final int CURRENT_VERSION = 1;
	public int configVersion = CURRENT_VERSION;

	public enum Mode {
		NORMAL,
		NORMAL_WHEN_SNEAKING,
		CLICKTHROUGH_WHEN_SNEAKING;

		public boolean shouldClickThrough(boolean sneaking) {
			return switch (this) {
				case NORMAL -> false;
				case NORMAL_WHEN_SNEAKING -> !sneaking;
				case CLICKTHROUGH_WHEN_SNEAKING -> sneaking;
			};
		}
	}

	public Mode signs = Mode.NORMAL_WHEN_SNEAKING;
	public Mode itemFrames = Mode.NORMAL_WHEN_SNEAKING;
	public Mode glowItemFrames = Mode.NORMAL_WHEN_SNEAKING;
	public Mode paintings = Mode.NORMAL_WHEN_SNEAKING;

	public static ClickThroughConfig loadOrCreate() {
		Path path = configPath();

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				ClickThroughConfig loaded = GSON.fromJson(reader, ClickThroughConfig.class);

				if (loaded != null) {
					if (loaded.configVersion != CURRENT_VERSION) {
						ClickThroughServer.LOGGER.warn(
								"{} is version {}, expected {}; using it as-is, but you may want to check its contents",
								FILE_NAME, loaded.configVersion, CURRENT_VERSION);
						loaded.configVersion = CURRENT_VERSION;
					}

					return loaded;
				}
			} catch (IOException e) {
				ClickThroughServer.LOGGER.warn("Failed to read {}, falling back to defaults", FILE_NAME, e);
			}
		}

		ClickThroughConfig defaults = new ClickThroughConfig();
		defaults.save();
		return defaults;
	}

	public void save() {
		Path path = configPath();

		try {
			Files.createDirectories(path.getParent());

			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			ClickThroughServer.LOGGER.warn("Failed to write {}", FILE_NAME, e);
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(Constants.FOLDER_NAME).resolve(FILE_NAME);
	}
}
