package org.anarchadia.quasar.api.config;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.client.TickEvent;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.setting.settings.BooleanSetting;
import org.anarchadia.quasar.api.setting.settings.ModeSetting;
import org.anarchadia.quasar.api.setting.settings.NumberSetting;
import org.anarchadia.quasar.api.setting.settings.StringSetting;
import org.anarchadia.quasar.api.util.LoggingUtil;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * ConfigManager handles saving and loading of module settings to and from an XML file.
 */
public class ConfigManager {
    private final File file;
    private final File mainDirectory;
    private int tickCounter = 0;
    private static final int SAVE_INTERVAL = 600; // Number of ticks between saves (600 ticks = 30 seconds)

    /**
     * Constructs a ConfigManager instance and ensures the configuration file exists.
     */
    public ConfigManager() {
        mainDirectory = new File(MinecraftClient.getInstance().runDirectory, "quasar");

        if (!mainDirectory.exists()) {
            mainDirectory.mkdir();
        }

        file = new File(mainDirectory, "config.xml");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            LoggingUtil.logger.error(e.getMessage());
        }
    }

    /**
     * Handles the tick event to save configuration periodically.
     *
     * @param event The tick event.
     */
    @Listener
    public void onTick(TickEvent event) {
        tickCounter++;
        if (tickCounter >= SAVE_INTERVAL) {
            save();
            tickCounter = 0;
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
        try {
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

                for (Setting setting : module.settings) {
                    Element settingElement = doc.createElement("setting");
                    settingElement.setAttribute("name", setting.getName());
                    settingElement.setTextContent(getSettingValue(setting));
                    moduleElement.appendChild(settingElement);
                }

                rootElement.appendChild(moduleElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new FileOutputStream(file));
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
                            Setting setting = module.getSettingByName(settingName);
                            if (setting != null) {
                                setSettingValue(setting, settingValue);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggingUtil.logger.error("Error while loading config!", e);
        }
    }

    /**
     * Retrieves the value of the specified setting as a string.
     *
     * @param setting the setting whose value to retrieve.
     * @return the string representation of the setting's value.
     */
    private String getSettingValue(Setting setting) {
        if (setting instanceof BooleanSetting) {
            return String.valueOf(((BooleanSetting) setting).isEnabled());
        } else if (setting instanceof NumberSetting) {
            return String.valueOf(((NumberSetting) setting).getValue());
        } else if (setting instanceof StringSetting) {
            return ((StringSetting) setting).getString();
        } else if (setting instanceof ModeSetting) {
            return ((ModeSetting) setting).getMode();
        }
        return "";
    }

    /**
     * Sets the value of the specified setting.
     *
     * @param setting the setting whose value to set.
     * @param value the value to set for the setting.
     */
    private void setSettingValue(Setting setting, String value) {
        if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).setEnabled(Boolean.parseBoolean(value));
        } else if (setting instanceof NumberSetting) {
            ((NumberSetting) setting).setValue(Double.parseDouble(value));
        } else if (setting instanceof StringSetting) {
            ((StringSetting) setting).setString(value);
        } else if (setting instanceof ModeSetting) {
            ((ModeSetting) setting).setMode(value);
        }
    }

}