package com.nibblepoker.commons.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Config {
	private final Logger logger;
	private final String namespace;
	
	private final HashMap<String, String> configStringFields;
	private final HashMap<String, Integer> configIntegerFields;
	private final HashMap<String, Boolean> configBooleanFields;
	
	private boolean isFileBroken;
	
	public Config(Logger logger, String namespace) {
		this.logger = logger;
		this.namespace = namespace;
		
		this.configStringFields = new HashMap<>();
		this.configIntegerFields = new HashMap<>();
		this.configBooleanFields = new HashMap<>();
		
		this.isFileBroken = false;
		
		this.logger.debug("Instantiated config for '{}' with the expected path '{}'", namespace, this.getConfigPath());
	}
	
	public Path getConfigPath() {
		return Paths.get(FabricLoader.getInstance().getConfigDir().toString(), this.namespace + ".json");
	}
	
	public boolean registerStringField(String fieldKey, String defaultValue) {
		return this.registerStringField(fieldKey, defaultValue, true);
	}
	
	public boolean registerStringField(String fieldKey, String defaultValue, boolean overwrite) {
		if (this.configStringFields.containsKey(fieldKey) && !overwrite) {
			return false;
		}
		this.configStringFields.put(fieldKey, defaultValue);
		return true;
	}
	
	public boolean registerIntegerField(String fieldKey, Integer defaultValue) {
		return this.registerIntegerField(fieldKey, defaultValue, true);
	}
	
	public boolean registerIntegerField(String fieldKey, Integer defaultValue, boolean overwrite) {
		if (this.configIntegerFields.containsKey(fieldKey) && !overwrite) {
			return false;
		}
		this.configIntegerFields.put(fieldKey, defaultValue);
		return true;
	}
	
	public boolean registerBooleanField(String fieldKey, Boolean defaultValue) {
		return this.registerBooleanField(fieldKey, defaultValue, true);
	}
	
	public boolean registerBooleanField(String fieldKey, Boolean defaultValue, boolean overwrite) {
		if (this.configBooleanFields.containsKey(fieldKey) && !overwrite) {
			return false;
		}
		this.configBooleanFields.put(fieldKey, defaultValue);
		return true;
	}
	
	public Config load() {
		return this.load(this.getConfigPath(), true);
	}
	
	public Config load(Path configFilePath, boolean saveConfigAfterLoading) {
		// Doing some preliminary checks.
		if (!Files.exists(configFilePath)) {
			// Attempting to create the required directories.
			try {
				Files.createDirectories(configFilePath.getParent());
			} catch(IOException e) {
				this.logger.error("Failed to create the following directories '{}' !", configFilePath.getParent());
				this.isFileBroken = true;
			}
			
			// Attempting to create the default config file if the directory structure exists.
			if (!this.isFileBroken) {
				if (!this.save()) {
					this.logger.warn("Failed to save the default config for '{}' in '{}' !", this.namespace, configFilePath);
					this.isFileBroken = true;
				}
			}
		} else if(Files.isDirectory(configFilePath)) {
			this.isFileBroken = true;
		}
		
		// Attempting to load the config file if possible, otherwise we return the default one.
		if (this.isFileBroken) {
			this.logger.warn("The config file for '{}' is broken, the default config will be used instead !", this.namespace);
		} else {
			this.logger.debug("Attempting to load the config file at '{}'...", configFilePath);
			try {
				// Reading and parsing the file
				JsonObject configJsonObject = new Gson().fromJson(Files.readString(configFilePath), JsonObject.class);
				
				// Reading the fields into the current 'Config' instance.
				for (String configKey : this.configStringFields.keySet()) {
					this.logger.debug("Attempting to read '{}' as a String...", configKey);
					JsonElement expectedStringJsonElement = configJsonObject.get(configKey);
					
					if (expectedStringJsonElement == null) {
						this.logger.warn("The config key '{}' couldn't be found for '{}', using the default value !", configKey, this.namespace);
					} else if (expectedStringJsonElement.isJsonNull()) {
						this.logger.warn("The config key '{}' value was 'null' for '{}', using the default value !", configKey, this.namespace);
					} else if (expectedStringJsonElement.isJsonPrimitive() && expectedStringJsonElement.getAsJsonPrimitive().isString()) {
						this.logger.debug("Reading '{}' with a value of '{}' that will override the default value '{}'...",
								configKey, expectedStringJsonElement.getAsString(), this.configStringFields.get(configKey));
						this.configStringFields.put(configKey, expectedStringJsonElement.getAsString());
					} else {
						this.logger.warn(
								"The config key '{}' for '{}' has a value that isn't considered as a String, using the default value !",
								configKey, this.namespace
						);
					}
				}
				
				for (String configKey : this.configIntegerFields.keySet()) {
					this.logger.debug("Attempting to read '{}' as a Number...", configKey);
					JsonElement expectedNumberJsonElement = configJsonObject.get(configKey);
					
					if (expectedNumberJsonElement == null) {
						this.logger.warn("The config key '{}' couldn't be found for '{}', using the default value !", configKey, this.namespace);
					} else if (expectedNumberJsonElement.isJsonNull()) {
						this.logger.warn("The config key '{}' value was 'null' for '{}', using the default value !", configKey, this.namespace);
					} else if (expectedNumberJsonElement.isJsonPrimitive() && expectedNumberJsonElement.getAsJsonPrimitive().isNumber()) {
						this.logger.debug("Reading '{}' with a value of '{}' that will override the default value '{}'...",
								configKey, expectedNumberJsonElement.getAsInt(), this.configIntegerFields.get(configKey));
						this.configIntegerFields.put(configKey, expectedNumberJsonElement.getAsInt());
					} else {
						this.logger.warn(
								"The config key '{}' for '{}' has a value that isn't considered as a Number, using the default value !",
								configKey, this.namespace
						);
					}
				}
				
				for (String configKey : this.configBooleanFields.keySet()) {
					this.logger.debug("Attempting to read '{}' as a Number...", configKey);
					JsonElement expectedBooleanJsonElement = configJsonObject.get(configKey);
					
					if (expectedBooleanJsonElement == null) {
						this.logger.warn("The config key '{}' couldn't be found for '{}', using the default value !", configKey, this.namespace);
					} else if (expectedBooleanJsonElement.isJsonNull()) {
						this.logger.warn("The config key '{}' value was 'null' for '{}', using the default value !", configKey, this.namespace);
					} else if (expectedBooleanJsonElement.isJsonPrimitive() && expectedBooleanJsonElement.getAsJsonPrimitive().isBoolean()) {
						this.logger.debug("Reading '{}' with a value of '{}' that will override the default value '{}'...",
								configKey, expectedBooleanJsonElement.getAsBoolean(), this.configBooleanFields.get(configKey));
						this.configBooleanFields.put(configKey, expectedBooleanJsonElement.getAsBoolean());
					} else {
						this.logger.warn(
								"The config key '{}' for '{}' has a value that isn't considered as a Boolean, using the default value !",
								configKey, this.namespace
						);
					}
				}
				
			} catch(IOException err) {
				this.logger.error("Failed to load and read the config file at '{}' !", configFilePath);
				this.isFileBroken = true;
			} catch(JsonSyntaxException err) {
				this.logger.error("Failed to parse the content of the config file '{}' !", configFilePath);
				this.logger.error(err.getLocalizedMessage());
				this.isFileBroken = true;
			}
		}
		
		// Saving the final config again if needed.
		if (saveConfigAfterLoading) {
			this.logger.debug("Attempting to save the final config file for '{}' at '{}'...", this.namespace, configFilePath);
			
			if (this.isFileBroken) {
				this.logger.warn("The config file for '{}' is broken, the file at '{}' won't be updated !", this.namespace, configFilePath);
			} else {
				if (!this.save()) {
					this.logger.warn("Failed to save the finale config for '{}' in '{}', marking as broken for safety !", this.namespace, configFilePath);
					this.isFileBroken = true;
				}
			}
		}
		
		return this;
	}
	
	/**
	 * Prepares a JSON version of the current config with its current fields and values.
	 * @return A 'JsonObject' representing the config as described above.
	 */
	public JsonObject asJsonObject() {
		JsonObject json = new JsonObject();
		
		for (HashMap.Entry<String, String> entry : this.configStringFields.entrySet()) {
			json.addProperty(entry.getKey(), entry.getValue());
		}
		
		for (HashMap.Entry<String, Integer> entry : this.configIntegerFields.entrySet()) {
			json.addProperty(entry.getKey(), entry.getValue());
		}
		
		for (HashMap.Entry<String, Boolean> entry : this.configBooleanFields.entrySet()) {
			json.addProperty(entry.getKey(), entry.getValue());
		}
		
		return json;
	}
	
	/**
	 * ???<br>
	 * This function should only be used after calling 'load()' at least once !
	 * @return ???
	 */
	public boolean isBroken() {
		return this.isFileBroken;
	}
	
	public boolean save() {
		return this.save(this.getConfigPath(), true);
	}
	
	public boolean save(Path configFilePath, boolean overwrite) {
		if (this.isFileBroken) {
			this.logger.error("Failed to save the config for '{}' at '{}' since it is considered as broken !", this.namespace, configFilePath);
		}
		
		if (Files.exists(configFilePath)) {
			if (overwrite) {
				try {
					if (!configFilePath.toFile().delete()) {
						throw new IOException("Failed to delete: "+configFilePath);
					}
				} catch(IOException err) {
					this.logger.error("Failed to delete the previous config for '{}' at '{}' !", this.namespace, configFilePath);
					return false;
				}
			} else {
				this.logger.warn("Attempted to save and overwrite config for '{}' while not allowed to overwrite !", this.namespace);
				return false;
			}
		}
		
		try {
			// NOTE: Adding the FileWriter in the 'toJson' call resulted in empty files !
			FileWriter fileWriter = new FileWriter(configFilePath.toFile());
			fileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(this.asJsonObject()));
			fileWriter.close();
		} catch(IOException err) {
			this.logger.error("Failed to save the config for '{}' in '{}' !", this.namespace, configFilePath);
			return false;
		}
		
		return true;
	}
}
