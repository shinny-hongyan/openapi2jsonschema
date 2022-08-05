import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiGateway {

    private Map<String, Object> config;

    static public class ProxyRuleItem {
        public String Host;
        public String QuerySchema;
        public String BodySchema;
    };

    public ApiGateway(String logLevel, int logVerbose, String logDir) {
        config = new HashMap<>();
        Map<String, Object> globalConf = new HashMap<>();
        globalConf.put("LogLevel", logLevel);
        globalConf.put("LogVerbose", logVerbose);
        globalConf.put("LogDir", logDir);
        config.put("global", globalConf);
    }

    public void setProxyAddr(String nodeName, String addr) {
        Map<String, Object> nodeMap = (Map<String, Object>) config.get(nodeName);
        if (nodeMap == null) {
            nodeMap = new HashMap<>();
            config.put(nodeName, nodeMap);
        }
        nodeMap.put("ProxyAddr", addr);
    }

    public void setJWSPublicRSA(String nodeName, String public_key) {
        Map<String, Object> nodeMap = (Map<String, Object>) config.get(nodeName);
        if (nodeMap == null) {
            nodeMap = new HashMap<>();
            config.put(nodeName, nodeMap);
        }
        nodeMap.put("JWSPublicRSA", public_key);
    }

    public void addRoute(String nodeName, String path, String method, ProxyRuleItem item) {
        Map<String, Object> nodeMap = (Map<String, Object>) config.get(nodeName);
        if (nodeMap == null) {
            nodeMap = new HashMap<>();
            nodeMap.put("NodeId", nodeName);
            config.put(nodeName, nodeMap);
        }
        Map<String, Object> proxyRule = (Map<String, Object>) nodeMap.get("ProxyRule");
        if (proxyRule == null) {
            proxyRule = new HashMap<>();
            nodeMap.put("ProxyRule", proxyRule);
        }
        Map<String, Object> proxyRuleItem = (Map<String, Object>) proxyRule.get(path);
        if (proxyRuleItem == null) {
            proxyRuleItem = new HashMap<>();
            proxyRule.put(path, proxyRuleItem);
        }
        List<ProxyRuleItem> items = new ArrayList<ProxyRuleItem>();
        items.add(item);
        proxyRuleItem.put(method, items);
    }

    public Map<String, Object> getConfig(){
        return this.config;
    }
}
