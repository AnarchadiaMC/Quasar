package org.anarchadia.quasar.api.config;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.client.TickEvent;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.util.LoggingUtil;
import net.minecraft.client.MinecraftClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.stream.Stream;

/**
 * ConfigManager handles saving and loading of module settings to and from an XML file, and manages backups.
 */
public class ConfigManager {
    private final File file;
    private final File mainDirectory;
    private final File backupDirectory;
    private int tickCounter = 0;
    private static final int SAVE_INTERVAL = 600; // Number of ticks between saves (600 ticks = 30 seconds)
    private long lastDailyBackupTime;
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;

    /**
     * Constructs a ConfigManager instance and ensures the configuration file and backup directory exist.
     */
    public ConfigManager() {
        mainDirectory = new File(MinecraftClient.getInstance().runDirectory, "quasar");
        backupDirectory = new File(mainDirectory, "backups");

        if (!mainDirectory.exists()) {
            if (mainDirectory.mkdir()) {
                LoggingUtil.info("Created main directory for config.");
            } else {
                LoggingUtil.logger.error("Failed to create main directory for config!");
            }
        }

        if (!backupDirectory.exists()) {
            if (backupDirectory.mkdir()) {
                LoggingUtil.info("Created backup directory.");
            } else {
                LoggingUtil.logger.error("Failed to create backup directory!");
            }
        }

        file = new File(mainDirectory, "config.xml");

        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    LoggingUtil.info("Created configuration file.");
                } else {
                    LoggingUtil.logger.error("Failed to create configuration file!");
                }
            }
        } catch (Exception e) {
            LoggingUtil.logger.error(e.getMessage(), e);
        }

        lastDailyBackupTime = System.currentTimeMillis();
    }

    /**
     * Handles the tick event to save configuration and handle daily backups periodically.
     *
     * @param event The tick event.
     */
    @Listener
    public void onTick(TickEvent event) {
        tickCounter++;
        long currentTime = System.currentTimeMillis();

        // Save periodically based on ticks
        if (tickCounter >= SAVE_INTERVAL) {
            save();
            tickCounter = 0;
        }

        // Perform daily backup and clean old backups based on millisecond timing
        if (currentTime - lastDailyBackupTime >= ONE_DAY_MILLIS) {
            createDailyBackup();
            cleanupOldBackups();
            lastDailyBackupTime = currentTime;
        }
    }

    /**
     * Gets the configuration file.
     *
     * @return the configuration file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the main directory where configurations are stored.
     *
     * @return the main directory.
     */
    public File getMainDirectory() {
        return mainDirectory;
    }

    /**
     * Saves the current settings of all modules to the XML configuration file.
     */
    public void save() {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element rootElement = doc.createElement("config");
            doc.appendChild(rootElement);

            for (Module module : Quasar.getInstance().getModuleManager().getModules()) {
                Element moduleElement = doc.createElement("module");
                moduleElement.setAttribute("name", module.getName());

                Element enabledElement = doc.createElement("setting");
                enabledElement.setAttribute("name", "enabled");
                enabledElement.setTextContent(String.valueOf(module.isEnabled()));
                moduleElement.appendChild(enabledElement);

                for (Setting<?> setting : module.getSettings()) {
                    Element settingElement = doc.createElement("setting");
                    settingElement.setAttribute("name", setting.getName());
                    settingElement.setTextContent(setting.getValue().toString());
                    moduleElement.appendChild(settingElement);
                }

                rootElement.appendChild(moduleElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);

        } catch (Exception e) {
            LoggingUtil.logger.error("Error while saving config!", e);
        }
    }

    /**
     * Loads the settings from the XML configuration file and applies them to the modules.
     */
    public void load() {
        try (FileInputStream fis = new FileInputStream(file)) {
            LoggingUtil.logger.info("Loading config...");

            // Check if file is empty
            if (file.length() == 0) {
                LoggingUtil.logger.warn("Config file is empty or corrupted. Automatically generating a new one.");
                save(); // Save a default configuration
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fis);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            var moduleNodes = root.getElementsByTagName("module");

            for (int i = 0; i < moduleNodes.getLength(); i++) {
                Element moduleElement = (Element) moduleNodes.item(i);
                String moduleName = moduleElement.getAttribute("name");
                Module module = Quasar.getInstance().getModuleManager().getModule(moduleName);
                if (module != null) {
                    var settingNodes = moduleElement.getElementsByTagName("setting");
                    for (int j = 0; j < settingNodes.getLength(); j++) {
                        Element settingElement = (Element) settingNodes.item(j);
                        String settingName = settingElement.getAttribute("name");
                        String settingValue = settingElement.getTextContent();

                        if ("enabled".equals(settingName)) {
                            module.setEnabled(Boolean.parseBoolean(settingValue));
                        } else {
                            Setting<?> setting = module.getSettingByName(settingName);
                            if (setting != null) {
                                setSettingValue(setting, settingValue);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggingUtil.logger.error("Error while loading config!", e);
            createBackupAndRegenerate(); // Backup and regenerate config file
        }
    }

    /**
     * Sets the value of the specified setting.
     *
     * @param setting the setting whose value to set.
     * @param value   the value to set for the setting.
     */
    private <T> void setSettingValue(Setting<T> setting, String value) {
        try {
            T currentValue = setting.getValue();
            if (currentValue instanceof Boolean) {
                setting.setValue(Boolean.valueOf(value));
            } else if (currentValue instanceof Double) {
                setting.setValue(Double.valueOf(value));
            } else if (currentValue instanceof Float) {
                setting.setValue(Float.valueOf(value));
            } else if (currentValue instanceof Integer) {
                setting.setValue(Integer.valueOf(value));
            } else if (currentValue instanceof Long) {
                setting.setValue(Long.valueOf(value));
            } else if (currentValue instanceof Short) {
                setting.setValue(Short.valueOf(value));
            } else if (currentValue instanceof Byte) {
                setting.setValue(Byte.valueOf(value));
            } else if (currentValue instanceof String) {
                setting.setValue(value);
            } else if (currentValue instanceof Enum<?>) {
                @SuppressWarnings("unchecked") Class<Enum> enumClass = (Class<Enum>) currentValue.getClass();
                setting.setValue(Enum.valueOf(enumClass, value));
            }
        } catch (Exception e) {
            LoggingUtil.logger.error("Failed to set setting value: " + setting.getName(), e);
        }
    }

    /**
     * Creates a backup of the current configuration file and regenerates the configuration.
     */
    private void createBackupAndRegenerate() {
        try {
            File backupFile = new File(mainDirectory, "config_backup.xml");
            if (file.exists()) {
                if (backupFile.exists()) {
                    if (!backupFile.delete()) {
                        LoggingUtil.logger.warn("Failed to delete old backup config file!");
                    }
                }
                if (!file.renameTo(backupFile)) {
                    LoggingUtil.logger.warn("Failed to rename config file to create a backup!");
                }
            }
            save(); // Save a default configuration after backing up
        } catch (Exception e) {
            LoggingUtil.logger.error("Failed to backup and regenerate config file", e);
        }
    }

    /**
     * Creates a daily backup with the current date in the filename.
     */
    private void createDailyBackup() {
        try {
            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            File dailyBackup = new File(backupDirectory, "config_backup_" + formattedDate + ".xml");

            Files.copy(file.toPath(), dailyBackup.toPath());
            LoggingUtil.logger.info("Created daily backup: " + dailyBackup.getName());
        } catch (Exception e) {
            LoggingUtil.logger.error("Failed to create daily backup!", e);
        }
    }

    /**
     * Deletes backups older than one week.
     */
    private void cleanupOldBackups() {
        try (Stream<Path> files = Files.list(Paths.get(backupDirectory.toURI()))) {
            files.filter(path -> path.toFile().isFile())
                    .filter(path -> path.getFileName().toString().startsWith("config_backup_"))
                    .forEach(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            String datePart = fileName.substring("config_backup_".length(), "config_backup_YYYYMMDD".length());
                            LocalDate fileDate = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyyMMdd"));
                            if (fileDate.isBefore(LocalDate.now().minusDays(7))) {
                                Files.delete(path);
                                LoggingUtil.logger.info("Deleted old backup: " + fileName);
                            }
                        } catch (Exception e) {
                            LoggingUtil.logger.error("Failed to cleanup old backup: " + path.getFileName(), e);
                        }
                    });
        } catch (Exception e) {
            LoggingUtil.logger.error("Error while cleaning up old backups!", e);
        }
    }
}