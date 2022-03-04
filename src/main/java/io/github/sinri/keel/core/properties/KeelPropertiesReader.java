package io.github.sinri.keel.core.properties;

import io.github.sinri.keel.core.KeelHelper;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import java.util.logging.Logger; // ->   (程序包 java.util.logging 已在模块 java.logging 中声明, 但模块 com.fasterxml.jackson.core 未读取它)

/**
 * Reader for Properties Files used in Keel Project
 *
 * @deprecated let us use YAML as configuration as of 1.12!
 */
@Deprecated
public class KeelPropertiesReader {

    protected final Properties properties;

    public KeelPropertiesReader() {
        properties = new Properties();
    }

    /**
     * @param properties the raw Properties instance
     * @since 1.10
     */
    public KeelPropertiesReader(Properties properties) {
        this.properties = properties;
    }

    public static KeelPropertiesReader loadReaderWithFile(String propertiesFileName) {
        KeelPropertiesReader reader = new KeelPropertiesReader();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            File configPropertiesFile = new File(propertiesFileName);
            reader.properties.load(new FileReader(configPropertiesFile));
        } catch (IOException e) {
            System.err.println("Cannot find the file config.properties. Use the embedded one.");
            try {
                reader.properties.load(KeelPropertiesReader.class.getClassLoader().getResourceAsStream(propertiesFileName));
            } catch (IOException ex) {
                throw new RuntimeException("Cannot find the embedded file config.properties.", ex);
            }
        }
        return reader;
    }

    public void appendPropertiesFromFile(String propertiesFileName) {
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            File configPropertiesFile = new File(propertiesFileName);
            properties.load(new FileReader(configPropertiesFile));
        } catch (IOException e) {
            System.err.println("Cannot find the file config.properties. Use the embedded one.");
            try {
                properties.load(KeelPropertiesReader.class.getClassLoader().getResourceAsStream(propertiesFileName));
            } catch (IOException ex) {
                throw new RuntimeException("Cannot find the embedded file config.properties.", ex);
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getProperty(List<String> keys) {
        StringBuilder key = new StringBuilder();
        for (String x : keys) {
            key.append(".").append(x);
        }
        key.deleteCharAt(0);
        return properties.getProperty(key.toString());
    }

    public String getProperty(List<String> keys, String defaultValue) {
        StringBuilder key = new StringBuilder();
        for (String x : keys) {
            key.append(".").append(x);
        }
        key.deleteCharAt(0);
        return properties.getProperty(key.toString(), defaultValue);
    }

    /**
     * @param keyPrefix Prefix of key to filter out, do not add dot to tail
     * @return such as C=X, C.DEF=Y
     * @since 1.10
     * For A.B.C=X, A.B.C.DEF=Y
     * Prefix as A.B
     * Result as C=X, C.DEF=Y
     */
    public KeelPropertiesReader filter(String keyPrefix) {
        Properties x = new Properties();
        properties.forEach((key, value) -> {
            if (key.toString().startsWith(keyPrefix + ".")) {
                // System.out.println("FILTER "+key+" -> "+value);
                x.put(key.toString().substring(keyPrefix.length() + 1), value);
            }
        });
        return new KeelPropertiesReader(x);
    }

    /**
     * @param regex REGEX, do not add dot to tail (`[a-z]+\.[0-9]+` would be changed to `^[a-z]+\.[0-9]+`, the dot amongst regex should be escaped)
     * @return Result sub reader
     * For A.B.C=X, A.B.C.DEF=Y
     * Filter Regex = `A\.[A-Z]`
     * Result C=X, C.DEF=Y
     */
    public KeelPropertiesReader filterUsingRegex(String regex) {
        Properties x = new Properties();
        Pattern pattern = Pattern.compile("^" + regex + "\\.");
        properties.forEach((key, value) -> {
            Matcher matcher = pattern.matcher(String.valueOf(key));
            if (matcher.find()) {
                // System.out.println("FILTER "+key+" -> "+value);
                x.put(key.toString().substring(matcher.group().length() + 1), value);
            }
        });
        return new KeelPropertiesReader(x);
    }

    /**
     * @return the key set
     * @since 1.10
     */
    public Set<String> getPlainKeySet() {
        Set<String> keys = new HashSet<>();
        properties.forEach((key, value) -> {
            keys.add(key.toString());
        });
        return keys;
    }

    /**
     * @return Json Object for the properties
     * @since 1.11
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        Set<String> plainKeySet = getPlainKeySet();
        for (var plainKey : plainKeySet) {
            String[] components = plainKey.split("\\.");
            List<String> keychain = Arrays.asList(components);
            KeelHelper.writeIntoJsonObject(jsonObject, keychain, getProperty(plainKey));
        }
        return jsonObject;
    }

    /**
     * @param classOfT class of the target class of subclass of KeelOptions
     * @param <T>      the target class of subclass of KeelOptions
     * @return the generated instance of the target class of subclass of KeelOptions
     * @since 1.11
     */
    public <T extends KeelOptions> T toConfiguration(Class<T> classOfT) {
        try {
            return classOfT.getConstructor(JsonObject.class).newInstance(this.toJsonObject());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Node computeKeyTree() {
        Set<String> longKeySet = getPlainKeySet();
        Node tree = new Node();
        tree.setNodeName("ROOT");

        Pattern arraySuffixPattern = Pattern.compile("^([^\\[]+)\\[(\\d+)]$");

        longKeySet.forEach(longKey -> {
            String[] components = longKey.split("\\.");

            Node parent = tree;
            for (String component : components) {
                Matcher matcher = arraySuffixPattern.matcher(component);
                String nodeName = component;
                boolean isArray = matcher.find();
                String index = nodeName;

                Node node = null;
                if (isArray) {
                    nodeName = matcher.group(1);
                    index = matcher.group(2);

                    var arrayNode = parent.getChildren().get(nodeName);
                    if (arrayNode != null) {
                        node = arrayNode.getChildren().get(index);
                    } else {
                        arrayNode = new Node();
                        arrayNode.setNodeName(nodeName);
                        arrayNode.setArray(true);
                        parent.addChildAt(nodeName, arrayNode);
                    }
                    if (node == null) {
                        node = new Node();
                        node.setNodeName(index);
                        parent.getChildren().get(nodeName).addChildAt(index, node);
                    }
                } else {
                    node = parent.getChildren().get(nodeName);
                    if (node == null) {
                        node = new Node();
                        node.setNodeName(index);
                        parent.addChildAt(index, node);
                    }
                }

                parent = node;
            }
        });

        return tree;
    }

    public static class Node {
        protected boolean isArray;
        protected String nodeName;
        protected Map<String, Node> children = new TreeMap<>();
        protected Node parent = null;

        public void addChildAt(String index, Node childNode) {
            this.getChildren().put(index, childNode);
            childNode.setParent(this);
        }

        public JsonObject toJsonObject(KeelPropertiesReader reader) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.put("name", getNodeName());
            jsonObject.put("key", propertiesLongKey());
            if (!getChildren().isEmpty()) {
                JsonObject x = new JsonObject();
                getChildren().forEach((index, node) -> {
                    x.put(String.valueOf(index), node.toJsonObject(reader));
                });
                jsonObject.put("children", x);
                jsonObject.put("is_array", isArray());
            } else {
                jsonObject.put("value", this.readPropertiesValue(reader));
            }
            return jsonObject;
        }

        public boolean isArray() {
            return isArray;
        }

        public Node setArray(boolean array) {
            isArray = array;
            return this;
        }

        public String getNodeName() {
            return nodeName;
        }

        public Node setNodeName(String nodeName) {
            this.nodeName = nodeName;
            return this;
        }

        public Map<String, Node> getChildren() {
            return children;
        }

        public Node setChildren(Map<String, Node> children) {
            this.children = children;
            return this;
        }

        public Node getParent() {
            return parent;
        }

        public Node setParent(Node parent) {
            this.parent = parent;
            return this;
        }

        public String propertiesLongKey() {
            List<Node> nodeList = new ArrayList<>();
            nodeList.add(this);

            while (nodeList.get(nodeList.size() - 1).parent != null) {
                nodeList.add(nodeList.get(nodeList.size() - 1).parent);
            }

            StringBuilder sb = new StringBuilder();
            boolean nextIsArrayIndex = false;
            for (var i = nodeList.size() - 2; i >= 0; i--) {
                var x = nodeList.get(i);
                if (nextIsArrayIndex) {
                    sb.append("[").append(x.nodeName).append("]");
                } else {
                    if (sb.length() > 0) {
                        sb.append(".");
                    }
                    sb.append(x.nodeName);
                }
                nextIsArrayIndex = x.isArray;
            }

            return sb.toString();
        }

        public String readPropertiesValue(KeelPropertiesReader reader) {
            return reader.getProperty(propertiesLongKey());
        }
    }
}
