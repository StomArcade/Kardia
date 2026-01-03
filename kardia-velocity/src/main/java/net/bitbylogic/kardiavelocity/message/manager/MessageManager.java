package net.bitbylogic.kardiavelocity.message.manager;

import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.message.MessageGroup;
import net.bitbylogic.kardiavelocity.message.MessageKey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class MessageManager {

    private static final File MESSAGES_FOLDER = new File("messages");

    private static final Map<String, MessageKey> REGISTRY = new LinkedHashMap<>();
    private static final List<MessageGroup> GROUP_REGISTRY = new ArrayList<>();

    private static final Locale[] SUPPORTED_LOCALES = { Locale.ENGLISH };

    public MessageManager() {
        try {
            if(!MESSAGES_FOLDER.exists()) {
                MESSAGES_FOLDER.mkdirs();
            }

            reload();
        } catch (IOException e) {
            KardiaVelocity.getInstance().getLogger().error("Failed to reload messages", e);
        }
    }

    public MessageKey register(String path, String defaultValue) {
        MessageKey key = new MessageKey(path, defaultValue);
        REGISTRY.put(path, key);

        try {
            loadKey(key);
        } catch (IOException e) {
            KardiaVelocity.getInstance().getLogger().error("Failed to load message key", e);
        }

        return key;
    }

    public MessageKey register(String path, List<String> defaultValue) {
        MessageKey key = new MessageKey(path, defaultValue);
        REGISTRY.put(path, key);

        try {
            loadKey(key);
        } catch (IOException e) {
            KardiaVelocity.getInstance().getLogger().error("Failed to load message key", e);
        }

        return key;
    }

    public void registerGroup(@NotNull MessageGroup... groups) {
        for (MessageGroup group : groups) {
            GROUP_REGISTRY.add(group);

            group.register();
        }
    }

    public Collection<MessageKey> all() {
        return REGISTRY.values();
    }

    public MessageKey getByPath(String path) {
        return REGISTRY.get(path);
    }

    public void reload() throws IOException {
        for (MessageKey key : REGISTRY.values()) {
            loadKey(key);
        }
    }

    private void loadKey(@NotNull MessageKey key) throws IOException {
        for (Locale locale : SUPPORTED_LOCALES) {
            File localeConfigFile = new File(MESSAGES_FOLDER, locale.toLanguageTag() + ".yml");

            if (!localeConfigFile.exists()) {
                localeConfigFile.getParentFile().mkdirs();
                localeConfigFile.createNewFile();
            }

            ConfigurationLoader<CommentedConfigurationNode> loader =
                    YamlConfigurationLoader.builder()
                            .file(localeConfigFile)
                            .build();

            CommentedConfigurationNode config = loader.load();

            if (config.node((Object[]) key.path().split("\\.")).virtual()) {
                List<String> valuesToSave = key.values(locale);

                if (valuesToSave.isEmpty()) {
                    continue;
                }

                if (valuesToSave.size() == 1) {
                    config.node((Object[]) key.path().split("\\.")).set(valuesToSave.get(0));
                } else {
                    config.node((Object[]) key.path().split("\\.")).set(valuesToSave);
                }

                loader.save(config);
                continue;
            }

            key.values().remove(locale);

            CommentedConfigurationNode node = config.node((Object[]) key.path().split("\\."));

            if (node.isList()) {
                List<String> valueList = new ArrayList<>();
                node.getList(String.class).forEach(valueList::add);

                key.values(locale).addAll(valueList);
                continue;
            }

            String value = node.getString();
            if (value != null) {
                key.values(locale).add(value);
            }
        }
    }

}
