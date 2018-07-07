package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * 配置类
 */
public class Config {
    private static Config _config = null;

    /**
     * 获取配置类的单例
     * @return 配置类
     */
    public static Config getCurrent() {
        if (_config == null) {
            try {
                StringBuilder sb = new StringBuilder();
                String s = "";
                //这段取filePath的代码不确定在jar包中是否生效。看网上的文章，是还需要处理的，去掉jar包的文件名。调试状态是可用的。
                String filePath = Config.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/resources/config.json";
                BufferedReader rd = new BufferedReader(new FileReader(filePath));
                while ((s = rd.readLine()) != null) {
                    sb.append(s);
                    sb.append("\r\n");
                }
                s = sb.toString();
                //去掉配置文件中的注释。。。。。。注释只能使用/*......*/
                s = Pattern.compile("[/][*][\\s\\S]+?[*][/]").matcher(s).replaceAll("");

                _config = (Config) JSON.parse(s, Config.class);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return _config;
    }

    private Config() {

    }

    public String getChainUrl() {
        return chainUrl;
    }

    private void setChainUrl(String chainUrl) {
        this.chainUrl = chainUrl;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    private void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public String getAddressHelloWorld() {
        return addressHelloWorld;
    }

    private void setAddressHelloWorld(String addressHelloWorld) {
        this.addressHelloWorld = addressHelloWorld;
    }

    public String getAddressFPData() {
        return addressFPData;
    }

    private void setAddressFPData(String addressFPData) {
        this.addressFPData = addressFPData;
    }

    public LinkedHashMap<String, String>[] getAccounts() {
        return accounts;
    }

    private void setAccounts(LinkedHashMap<String, String>[] accounts) {
        this.accounts = accounts;
    }

    private String chainUrl; //链的节点地址
    private String proxyUrl; //代理地址
    private String addressHelloWorld; //你好世界合约地址
    private String addressFPData; //保理准备数据合约地址
    private LinkedHashMap<String, String>[] accounts; //用于demo的账户列表
}
