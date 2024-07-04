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
import java.nio.channels.FileLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ConfigManager {
    private final File file;
    private final File mainDirectory;
    private final AtomicInteger tickCounter = new AtomicInteger(0);
    private static final int SAVE_INTERVAL = 600;
    private static final int RETRY_INTERVAL = 5;
    private final ReentrantLock configLock = new ReentrantLock();

    public ConfigManager() {
        mainDirectory = new File(MinecraftClient.getInstance().runDirectory, "quasar");
        if (!mainDirectory.exists() && !mainDirectory.mkdir()) {
            LoggingUtil.logger.error("Failed to create main directory for config!");
        }

        file = new File(mainDirectory, "config.xml");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    LoggingUtil.logger.error("Failed to create configuration file!");
                }
            } catch (Exception e) {
                LoggingUtil.logger.error("Error creating configuration file", e);
            }
        }
    }

    @Listener
    public void onTick(TickEvent event) {
        if (tickCounter.incrementAndGet() >= SAVE_INTERVAL) {
            save();
            tickCounter.set(0);
        }
    }

    public File getFile() {
        return file;
    }

    public File getMainDirectory() {
        return mainDirectory;
    }

    public void save() {
        configLock.lock();
        try (FileOutputStream fos = new FileOutputStream(file);
             FileLock lock = fos.getChannel().tryLock()) {

            if (lock == null) {
                LoggingUtil.logger.warn("Could not acquire lock for saving config. Retrying later.");
                return;
            }

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
                    settingElement.setTextContent(String.valueOf(setting.getValue()));
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
            LoggingUtil.logger.error("Error while saving config", e);
        } finally {
            configLock.unlock();
        }
    }

    public void load() {
        configLock.lock();
        try (FileInputStream fis = new FileInputStream(file)) {
            LoggingUtil.logger.info("Loading config...");

            if (file.length() == 0) {
                LoggingUtil.logger.warn("Config file is empty. Generating a new one.");
                save();
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
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
                        try {
                            Element settingElement = (Element) settingNodes.item(j);
                            String settingName = settingElement.getAttribute("name");
                            String settingValue = settingElement.getTextContent();

                            if ("enabled".equals(settingName)) {
                                module.setEnabled(Boolean.parseBoolean(settingValue));
                            } else {
                                Setting<?> setting = module.getSettingByName(settingName);
                                if (setting != null) {
                                    setSettingValue(setting, settingValue);
                                } else {
                                    LoggingUtil.logger.warn("Setting not found: " + settingName + " for module: " + moduleName);
                                }
                            }
                        } catch (Exception e) {
                            LoggingUtil.logger.error("Error loading setting for module: " + moduleName, e);
                        }
                    }
                } else {
                    LoggingUtil.logger.warn("Module not found: " + moduleName);
                }
            }
        } catch (Exception e) {
            LoggingUtil.logger.error("Error while loading config", e);
        } finally {
            configLock.unlock();
        }
    }

    private <T> void setSettingValue(Setting<T> setting, String value) {
        try {
            T currentValue = setting.getValue();
            if (currentValue instanceof Boolean) {
                setting.setValue((T) Boolean.valueOf(value));
            } else if (currentValue instanceof Double) {
                setting.setValue((T) Double.valueOf(value));
            } else if (currentValue instanceof Float) {
                setting.setValue((T) Float.valueOf(value));
            } else if (currentValue instanceof Integer) {
                setting.setValue((T) Integer.valueOf(value));
            } else if (currentValue instanceof Long) {
                setting.setValue((T) Long.valueOf(value));
            } else if (currentValue instanceof Short) {
                setting.setValue((T) Short.valueOf(value));
            } else if (currentValue instanceof Byte) {
                setting.setValue((T) Byte.valueOf(value));
            } else if (currentValue instanceof String) {
                setting.setValue((T) value);
            } else if (currentValue instanceof Enum<?>) {
                @SuppressWarnings("unchecked")
                Class<Enum> enumClass = (Class<Enum>) currentValue.getClass();
                setting.setValue((T) Enum.valueOf(enumClass, value));
            } else {
                LoggingUtil.logger.warn("Unknown setting type: " + setting.getName());
            }
        } catch (Exception e) {
            LoggingUtil.logger.error("Failed to set setting value: " + setting.getName(), e);
        }
    }
}