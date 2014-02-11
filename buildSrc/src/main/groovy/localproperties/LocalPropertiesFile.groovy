package localproperties

class LocalPropertiesFile {
    Properties properties;

    LocalPropertiesFile(File propertiesFile) {
        def Properties props = new Properties()
        if (propertiesFile.canRead()){
            props.load(new FileInputStream(propertiesFile))
            properties = props
        } else {
            println 'The file '+propertiesFile.absolutePath+' was not found.'
            properties = null
        }

    }

    def boolean isValid() {
        return properties != null
    }

    def String getValue(String propKey, String defaultValue) {
        if (!isValid())
            return defaultValue

        def String value = properties[propKey]
        if (utils.Utils.isEmpty(value))
            value = defaultValue

        return value
    }

    def String getValue(String propKey) {
        return getValue(propKey, null)
    }

    def String getValueWithConsoleFallback(String propKey, String propInputMessage) {
        return getValueWithConsoleFallback(propKey, null, propInputMessage)
    }

    def String getValueWithConsoleFallback(String propKey, String defaultValue, String propInputMessage) {
        if (!isValid())
            return defaultValue

        def propValue = getValue(propKey)
        if (utils.Utils.isEmpty(propValue)) {
            def con = System.console();
            if (con != null){
                propValue = con.readLine("\n"+propInputMessage+": ")
            }
        }

        if (utils.Utils.isEmpty(propValue))
            propValue = defaultValue

        return propValue
    }
}
