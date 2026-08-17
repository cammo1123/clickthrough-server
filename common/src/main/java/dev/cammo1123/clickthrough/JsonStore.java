package dev.cammo1123.clickthrough;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads and saves a single JSON document under
	 * {@code config/clickthrough_server/}. Subclasses supply the document type,
	 * its current version, and an optional {@link #migrate(JsonObject, int, int)}
 * transform for older schemas; everything else (load-or-create, version
 * checks, migrations, pretty printing, atomic writes) is shared so new
 * persisted features only add a small subclass.
 */
abstract class JsonStore<T> {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String VERSION_FIELD = "version";

	private final Path path;
	protected final T data;

	protected JsonStore(Path configDir, String fileName, T defaults) {
		this.path = configDir.resolve(Constants.FOLDER_NAME).resolve(fileName);
		this.data = loadOrCreate(defaults);
	}

	protected final void save() {
		save(GSON.toJsonTree(data, dataType()).getAsJsonObject());
	}

	private T loadOrCreate(T defaults) {
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				JsonObject root = GSON.fromJson(reader, JsonObject.class);

				int from = versionOf(root);
				if (from != currentVersion()) {
					ClickThroughServer.LOGGER.warn(
							"{} is version {}, expected {}; migrating it in place",
							path.getFileName(), from, currentVersion());
					if (!migrate(root, from, currentVersion())) {
						ClickThroughServer.LOGGER.warn("Unsupported version for {}, resetting to defaults", path.getFileName());
						save(GSON.toJsonTree(defaults, dataType()).getAsJsonObject());
						return defaults;
					}

					root.addProperty(VERSION_FIELD, currentVersion());
					save(root);
				}

				T loaded = GSON.fromJson(root, dataType());
				if (loaded != null && isValid(loaded)) {
					return loaded;
				}
			} catch (IOException | RuntimeException e) {
				ClickThroughServer.LOGGER.warn("Failed to read {}, falling back to defaults", path.getFileName(), e);
			}
		}

		save(GSON.toJsonTree(defaults, dataType()).getAsJsonObject());
		return defaults;
	}

	private static int versionOf(JsonObject root) {
		JsonElement version = root.get(VERSION_FIELD);
		if (version == null) {
			version = root.get("configVersion");
		}
		return version != null && version.isJsonPrimitive() ? version.getAsInt() : 0;
	}

	/**
	 * Transforms a stored document in place from an older schema before it is
	 * bound to the current one. The default is a no-op (a pure version bump);
	 * subclasses override it when their document format changes. Return false
	 * when the source version is unsupported. Implementations
	 * must not touch the document's {@code version} field, as it is stamped by
	 * the caller.
	 */
	protected boolean migrate(JsonObject root, int from, int to) {
		return true;
	}

	private void save(JsonObject value) {
		try {
			Files.createDirectories(path.getParent());

			Path temp = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
			try {
				try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
					GSON.toJson(value, writer);
				}
				Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
			} finally {
				Files.deleteIfExists(temp);
			}
		} catch (IOException e) {
			ClickThroughServer.LOGGER.warn("Failed to write {}", path.getFileName(), e);
		}
	}

	protected abstract Type dataType();

	protected abstract int currentVersion();

	/**
	 * @return false to reject a loaded document (e.g. missing required fields)
	 *         and fall back to defaults.
	 */
	protected boolean isValid(T data) {
		return true;
	}
}
