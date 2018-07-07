package adapter;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.tx.Contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 用于BCOS的合约基类<br/>
 * 对合约调用更好的封装（如：对事件的处理）<br/>
 * 解决调用父类的executeTransaction始终无法调用到合约的问题（仿造NodeJS SDK的调用流程修改）
 */
public abstract class BCOSContract extends Contract {
    private static Random _random = new Random(System.currentTimeMillis());

    /**
     * 构造函数
     * @param contractAddress 合约地址
     * @param web3j web3j JDK实例。通过Web3Adapter.getCurrent().getWeb3j()获取。
     * @param credentials 使用合约的账户（包含Private Key和Public Key）
     */
    protected BCOSContract(String contractAddress, Web3j web3j, Credentials credentials) {
        super(contractAddress, web3j, credentials, BigInteger.ZERO, Contract.GAS_LIMIT);
        super.setCredentials(credentials);
    }

    /**
     * 执行无返回值的需要保存数据的合约方法
     * @param funcName 合约方法名称
     * @param inputParams 合约方法的参数
     * @return 交易收据。记录本次交易的Hash、块高、块Hash等信息
     * @throws IOException
     */
    protected TransactionReceipt executeContractApplyTransaction(String funcName, List<Type> inputParams) throws IOException {
        Function func = new Function(
                funcName, inputParams,
                Collections.<TypeReference<?>>emptyList());
        String codeTxData = FunctionEncoder.encode(func);
        return executeTransaction(codeTxData);
    }

    /**
     * 执行有一个返回值的需要保存数据的合约方法。返回值在合约方法中，通过事件的方式返回
     * @param funcName 合约方法名称
     * @param inputParams 合约方法的参数
     * @param <T> 返回的数据类型。只能是Web3j中的Type的派生类
     * @return 合约中返回的数据。来自TransactionReceipt的Log
     * @throws IOException
     */
    protected <T extends Type> T executeContractApplyTransactionWithSingleReply(String funcName
            , List<Type> inputParams) throws IOException {
        List<TypeReference<?>> outputParams = Arrays.<TypeReference<?>>asList(new TypeReference<T>() {});
        List<Type> results = executeContractApplyTransactionWithMultipleReply(funcName, inputParams, outputParams);
        if (results == null || results.size() == 0) {
            return null;
        }

        return (T)results.get(0).getValue();
    }

    /**
     * 执行有多个返回值的需要保存数据的合约方法。返回值在合约方法中，通过事件的方式返回
     * @param funcName 合约方法名称
     * @param inputParams 合约方法的参数
     * @param outputParams 合约方法返回值的类型
     * @return 合约中返回的数据。来自TransactionReceipt的Log
     * @throws IOException
     */
    protected List<Type> executeContractApplyTransactionWithMultipleReply(String funcName
            , List<Type> inputParams, List<TypeReference<?>> outputParams) throws IOException {
        TransactionReceipt receipt = executeContractApplyTransaction(funcName, inputParams);
        List<Log> logs = receipt.getLogs();
        if (logs == null || logs.size() == 0) {
            return null;
        }

        Log log = logs.get(0);
        String value = log.getData();
        List<TypeReference<Type>> outputParamsReal = Utils.convert(outputParams);
        return FunctionReturnDecoder.decode(value, outputParamsReal);
    }

    /**
     * 对数据签名。采用椭圆曲线签名算法
     * @param plain 需要签名的数据
     * @param credentials 签名的账户
     * @return 椭圆曲线签名结果的V、R、C组成的byte数组。其中，V为27或者28（签名过程已加27）
     */
    protected byte[] sign(byte[] plain, Credentials credentials) {
        Sign.SignatureData signResult = org.web3j.crypto.Sign.signMessage(plain, credentials.getEcKeyPair());
        byte[] result = new byte[1 + signResult.getR().length + signResult.getS().length];
        System.arraycopy(new byte[]{signResult.getV()}, 0, result, 0, 1);
        System.arraycopy(signResult.getR(), 0, result, 1, signResult.getR().length);
        System.arraycopy(signResult.getS(), 0, result, 1 + signResult.getR().length, signResult.getS().length);
        return result;
    }

    /**
     * 向链发送eth_SendRawTransaction请求，完成合约方法的调用
     * 为同步方法，定死了超时为15秒。超时只是意味着交易收据未能获取到，不意味着交易失败。需要通过其他手段确认交易的成功与否
     * @param data 请求的业务数据。已编码为16进制字符串
     * @return 交易收据。记录本次交易的Hash、块高、块Hash等信息
     * @throws IOException
     */
    protected TransactionReceipt executeTransaction(String data) throws IOException {
        Credentials credentials = super.getCredentials();
        BCOSRawTransaction trans = new BCOSRawTransaction();
        trans.setData(data);
        trans.setFrom(credentials.getAddress());
        trans.setTo(super.getContractAddress());
        trans.setGas(Contract.GAS_LIMIT);
        trans.setRandomid(BigInteger.valueOf(_random.nextLong()));
        trans.setBlockLimit(super.web3j.ethBlockNumber().send().getBlockNumber().add(BigInteger.valueOf(1000)));

        String signTX = trans.SignTransaction(credentials.getEcKeyPair());

        EthSendTransaction sendResult = super.web3j.ethSendRawTransaction(signTX).send();
        if (sendResult.hasError()) {
            Response.Error error = sendResult.getError();
            throw new IOException(String.format("[%s]%s", error.getCode(), error.getMessage()));
        }

        String transHash = sendResult.getResult();
        long intervalTime = 300;
        int maxCount = 50;
        int count = 0;

        BigInteger filterId = super.web3j.ethNewBlockFilter().send().getFilterId();
        while (count++ < maxCount) {
            try {
                Thread.sleep(intervalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<EthLog.LogResult> logs = super.web3j.ethGetFilterChanges(filterId).send().getLogs();
            if (logs != null && logs.size() > 0) {
                break;
            }
        }

        if (count >= maxCount) {
            throw new IOException("Request timeout.");
        }

        super.web3j.ethUninstallFilter(filterId);
        EthGetTransactionReceipt result = super.web3j.ethGetTransactionReceipt(transHash).send();
        if (result.hasError()) {
            Response.Error error = result.getError();
            throw new IOException(String.format("[%s]%s", error.getCode(), error.getMessage()));
        }

        return result.getResult();
    }

    /**
     * 执行有一个返回值的单纯读取数据的合约方法。该方法直接读取本节点数据，不会在链上广播<br/>
     * 该方法覆盖了父类的相同方法。父类方法没有处理未取到数据的场景，会抛出数组越界的异常
     * @param function 合约的实体，包含合约名称、参数和返回值类型
     * @param <T> 返回的数据类型。只能是Web3j中的Type的派生类
     * @return 合约中返回的数据
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    protected <T extends Type> T executeCallSingleValueReturn(Function function) throws InterruptedException, ExecutionException {
        List<Type> values = super.executeCallMultipleValueReturn(function);
        if (values == null || values.size() == 0)
            throw new InterruptedException("getting without datas");
        return (T)values.get(0);
    }

    /**
     * 向BCOS区块链传递的数据格式
     */
    protected class BCOSRawTransaction {
        private byte[][] raw = new byte[7][];
        private String from;
        private byte[] v;
        private byte[] r;
        private byte[] s;

        public BCOSRawTransaction() {
            for (int i = 0; i < 7; i++) {
                raw[i] = new byte[0];
            }

            v = new byte [1];
            v[0] = 0x1c;
            r = new byte[0];
            s = new byte[0];
        }

        public String getData() {
            return Hex.toHexString(this.raw[6]);
        }

        public void setData(String data) {
            this.raw[6] = Hex.decode(data.substring(2));
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return Hex.toHexString(this.raw[4]);
        }

        public void setTo(String to) {
            String s = to;
            if ("0x".equals(to.substring(0, 2))) {
                s = to.substring(2);
            }
            this.raw[4] = Hex.decode(s);
        }

        public BigInteger getGas() {
            return new BigInteger(1, this.raw[2]);
        }

        public void setGas(BigInteger gas) {
            this.raw[2] = BigIntToBuffer(gas);
        }

        public BigInteger getRandomid() {
            return new BigInteger(1, this.raw[0]);
        }

        public void setRandomid(BigInteger randomid) {
            this.raw[0] = BigIntToBuffer(randomid);
        }

        public BigInteger getBlockLimit() {
            return new BigInteger(1, this.raw[3]);
        }

        public void setBlockLimit(BigInteger blockLimit) {
            this.raw[3] = BigIntToBuffer(blockLimit);
        }

        public byte[] encode() {
            List<RlpType> values = new ArrayList<RlpType>();
            for (int i = 0;  i< 7; i++) {
                values.add(RlpString.create(this.raw[i]));
            }
            return RlpEncoder.encode(new RlpList(values));
        }

        public void sign(ECKeyPair keyPair) {
            Sign.SignatureData result = org.web3j.crypto.Sign.signMessage(encode(), keyPair);
            v[0] = result.getV();
            r = result.getR();
            s = result.getS();
        }

        public String SignTransaction(ECKeyPair keyPair) {
            sign(keyPair);
            List<RlpType> values = new ArrayList<RlpType>();
            for (int i = 0; i < 7; i++)
                values.add(RlpString.create(this.raw[i]));
            values.add(RlpString.create(this.v));
            values.add(RlpString.create(this.r));
            values.add(RlpString.create(this.s));
            return "0x" + Hex.toHexString(RlpEncoder.encode(new RlpList(values)));
        }

        private byte[] BigIntToBuffer(BigInteger value) {
            byte[] ret = value.toByteArray();
            if (ret[0] == 0) {
                byte[] result = new byte[ret.length - 1];
                System.arraycopy(ret, 1, result, 0, result.length);
                ret = result;
            }
            return ret;
        }
    }
}
