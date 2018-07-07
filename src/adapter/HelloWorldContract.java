package adapter;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionTimeoutException;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * 你好世界合约
 */
public class HelloWorldContract extends BCOSContract {

    /**
     * 构造函数
     * @param web3j web3j JDK实例。通过Web3Adapter.getCurrent().getWeb3j()获取。
     * @param credentials 使用合约的账户（包含Private Key和Public Key）
     * @param address 合约地址
     */
    public HelloWorldContract(Web3j web3j, Credentials credentials, String address) {
        super(address, web3j, credentials);
    }

    /**
     * 设置名字
     * @param newName 设置的新名字
     * @return 返回交易收据。记录本次交易的Hash、块高、块Hash等信息
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TransactionTimeoutException
     * @throws Exception
     */
    public TransactionReceipt setName(String newName) throws IOException, InterruptedException, ExecutionException, TransactionTimeoutException, Exception {
        return super.executeContractApplyTransaction("set", Arrays.<Type>asList(new Utf8String(newName)));
    }

    /**
     * 读取名字
     * @return 返回上一次设置的名字
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public String getName() throws InterruptedException, ExecutionException {
        Utf8String result = executeCallSingleValueReturn(new Function(
                "get", Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {})
        ));
        return result.toString();
    }
}
