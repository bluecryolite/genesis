package adapter;

import models.FactoringPrepare;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.exceptions.TransactionTimeoutException;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 保理准备数据合约
 */
public class FactoringPrepareDataContract extends BCOSContract {

    /**
     * 构造函数
     * @param web3j web3j JDK实例。通过Web3Adapter.getCurrent().getWeb3j()获取。
     * @param credentials 使用合约的账户（包含Private Key和Public Key）
     * @param address 合约地址
     */
    public FactoringPrepareDataContract(Web3j web3j, Credentials credentials, String address) {
        super(address, web3j, credentials);
    }

    /**
     * 创建保理准备
     * @param seller 供应商账户
     * @param factor 保理商账户
     * @param contractContent 保理准备合同内容
     * @param expireTime 保理合同到期日
     * @return 返回保理准备ID
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TransactionTimeoutException
     * @throws Exception
     */
    public BigInteger create(Address seller, Address factor, String contractContent, String expireTime)
            throws IOException, InterruptedException, ExecutionException, TransactionTimeoutException, Exception {
        byte[] signature = super.sign(contractContent.concat(expireTime).getBytes(), super.getCredentials());
        List<Type> result = super.executeContractApplyTransactionWithMultipleReply("create"
                , Arrays.<Type>asList(seller, factor, new Utf8String(contractContent), new Utf8String(expireTime), new DynamicBytes(signature))
                , Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() { }, new TypeReference<Utf8String>() { }, new TypeReference<Uint256>() { })
        );
        if (result == null || result.size() != 3)
            throw new IOException("create failed. unknown reason.");

        if (((Uint8)result.get(0)).getValue().intValue() != 0)
            throw new IOException(((Utf8String)result.get(1)).getValue());

        return ((Uint256)result.get(2)).getValue().subtract(BigInteger.ONE);
    }

    /**
     * 获取保理准备详情
     * @param tid 保理准备ID
     * @return 返回保理准备详情，包括：保理准备合同内容、到期日、签名
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public FactoringPrepare getContract(Integer tid)
            throws InterruptedException, ExecutionException {
        byte[] bytes = ByteBuffer.allocate(32).putInt(28,  tid).array();
        byte[] signature = super.sign(bytes, super.getCredentials());

        List<Type> resultContent = super.executeCallMultipleValueReturn(new Function(
                "getContractContent", Arrays.<Type>asList(new DynamicBytes(bytes), new DynamicBytes(signature)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() { }, new TypeReference<Utf8String>() { }
                , new TypeReference<Uint256>() { }, new TypeReference<Address>() { }, new TypeReference<Address>() { }, new TypeReference<Address>() { }
                , new TypeReference<Utf8String>() { }, new TypeReference<Utf8String>() { })
        ));

        if (resultContent == null || resultContent.size() != 8)
            throw new InterruptedException("getting failed. unknown reason.");

        if (((Uint8)resultContent.get(0)).getValue().intValue() != 0)
            throw new InterruptedException(((Utf8String)resultContent.get(1)).getValue());

        List<Type> resultSignature = super.executeCallMultipleValueReturn(new Function(
                "getContractSignature", Arrays.<Type>asList(new DynamicBytes(bytes), new DynamicBytes(signature)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() { }, new TypeReference<Utf8String>() { }
                        , new TypeReference<Uint256>() { }, new TypeReference<DynamicBytes>() { }, new TypeReference<DynamicBytes>() { }, new TypeReference<DynamicBytes>() { }

                )));

        if (resultSignature == null || resultSignature.size() != 6)
            throw new InterruptedException("getting failed. unknown reason.");

        if (((Uint8)resultSignature.get(0)).getValue().intValue() != 0)
            throw new InterruptedException(((Utf8String)resultSignature.get(1)).getValue());

        FactoringPrepare ret = new FactoringPrepare();
        ret.setTid(tid);
        ret.setContent(resultContent.get(6).getValue().toString());
        ret.setExpireDate(resultContent.get(7).getValue().toString());
        ret.setBuyerSignature(Hex.toHexString(((DynamicBytes) resultSignature.get(3)).getValue()));
        ret.setSellerSignature(Hex.toHexString(((DynamicBytes) resultSignature.get(4)).getValue()));
        ret.setFactorSignature(Hex.toHexString(((DynamicBytes) resultSignature.get(5)).getValue()));

        return ret;
    }

    /**
     * 获取保理准备数量
     * @return 返回保理准备数量
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public BigInteger getContractCount()
            throws InterruptedException, ExecutionException {
        Uint256 result = executeCallSingleValueReturn(new Function(
                "getContractCount", Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {})
        ));
        return result.getValue();
    }

    /**
     * 供应商签名
     * @param tid 保理准备ID
     * @param contractContent 保理准备合同内容
     * @param expireTime 到期日
     * @throws IOException
     */
    public void sellerSign(Integer tid, String contractContent, String expireTime) throws IOException  {
        sign("sellerSign", tid, contractContent, expireTime);
    }

    /**
     * 保理商签名
     * @param tid 保理准备ID
     * @param contractContent 保理准备合同内容
     * @param expireTime 到期日
     * @throws IOException
     */
    public void factorSign(Integer tid, String contractContent, String expireTime) throws IOException  {
        sign("factorSign", tid, contractContent, expireTime);
    }

    /**
     * 供应商签名
     * @param funcName 保理准备签名合约方法名
     * @param tid 保理准备ID
     * @param contractContent 保理准备合同内容
     * @param expireTime 到期日
     * @throws IOException
     */
    private void sign(String funcName, Integer tid, String contractContent, String expireTime) throws IOException {
        byte[] signature = super.sign(contractContent.concat(expireTime).getBytes(), super.getCredentials());
        List<Type> result = super.executeContractApplyTransactionWithMultipleReply(funcName
                , Arrays.<Type>asList(new Uint256(BigInteger.valueOf(tid)), new DynamicBytes(signature))
                , Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() { }, new TypeReference<Utf8String>() { })
        );

        if (result == null || result.size() != 2)
            throw new IOException("signing failed. unknown reason.");

        if (((Uint8)result.get(0)).getValue().intValue() != 0)
            throw new IOException(((Utf8String)result.get(1)).getValue());
    }
}
