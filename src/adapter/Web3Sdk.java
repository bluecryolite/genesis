package adapter;

import com.sun.jndi.toolkit.url.Uri;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import util.Config;

import java.io.IOException;

/**
 * Web3j SDK实例的单例封装
 */
public class Web3Sdk {
    private static Web3j _web3j = null;

    /**
     * Web3j SDK的封装单例
     * @return 返回Web3j SDK的封装实例
     */
    public static Web3j getWeb3j() {
        if (_web3j == null) {
            try {
                HttpClientBuilder client = HttpClients.custom();
                if (Config.getCurrent().getProxyUrl() != null && Config.getCurrent().getProxyUrl().length() > 0) {
                    Uri uri = new Uri(Config.getCurrent().getProxyUrl());
                    client.setProxy(new HttpHost(uri.getHost(), uri.getPort()));
                }
                CloseableHttpClient builder = client.setConnectionManagerShared(true).build();
                _web3j = Web3j.build(new HttpService(Config.getCurrent().getChainUrl(), builder));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return _web3j;
    }
}
