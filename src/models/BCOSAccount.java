package models;

import adapter.FactoringPrepareDataContract;
import adapter.HelloWorldContract;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import util.Config;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * BCOS账户的封装。包括账户本身以及合约相关的方法调用
 */
public class BCOSAccount {

    /**
     * 构造函数
     * @param web3j web3j JDK实例。通过Web3Adapter.getCurrent().getWeb3j()获取。
     * @param credentials 使用合约的账户（包含Private Key和Public Key）
     */
    public BCOSAccount(Web3j web3j, Credentials credentials) {
        _helloWorldContract = new HelloWorldContract(web3j, credentials, Config.getCurrent().getAddressHelloWorld());
        _factoringPrepareDataContract = new FactoringPrepareDataContract(web3j, credentials, Config.getCurrent().getAddressFPData());
        this.credentials = credentials;
        buyerContractList = new ArrayList<Integer>();
        sellerContractList = new ArrayList<Integer>();
        factorContractList = new ArrayList<Integer>();
        needSellerSignContractList = new ArrayList<Integer>();
        needFactorSignContractList = new ArrayList<Integer>();
    }

    /**
     * 设置你好世界合约的名字
     * @param newName 新名字
     * @throws Exception
     */
    public void setHelloName(String newName) throws Exception {
        _helloWorldContract.setName(newName);
    }

    /**
     * 获取你好世界合约的名字
     * @return 返回名字
     * @throws Exception
     */
    public String getHelloName() throws Exception {
        return _helloWorldContract.getName();
    }

    /**
     * 创建保理准备
     * @param seller 供应商账户
     * @param factor 保理商账户
     * @param contractContent 保理准备合同内容
     * @param expireTime 保理合同到期日
     * @return 返回保理准备ID
     * @throws Exception
     */
    public BigInteger createFactoringPrepare(Address seller, Address factor, String contractContent, String expireTime)
        throws Exception {
        return _factoringPrepareDataContract.create(seller, factor, contractContent, expireTime);
    }

    /**
     * 获取保理准备数量
     * @return 返回保理准备数量
     * @throws Exception
     */
    public BigInteger getFactoringPrepareCount() throws Exception {
        return _factoringPrepareDataContract.getContractCount();
    }

    /**
     * 获取保理准备详情
     * @param tid 保理准备ID
     * @return 返回保理准备详情，包括：保理准备合同内容、到期日、签名
     * @throws Exception
     */
    public FactoringPrepare getFactoringPrepare(Integer tid) throws Exception {
        return _factoringPrepareDataContract.getContract(tid);
    }

    /**
     * 供应商或者保理商签名
     * @param tid 保理准备ID
     * @param content 保理准备合同内容
     * @param expireDate 到期日
     * @throws IOException
     */
    public void sign(Integer tid, String content, String expireDate) throws IOException {
        if (needFactorSignContractList.indexOf(tid) >= 0) {
            _factoringPrepareDataContract.factorSign(tid, content, expireDate);
            needFactorSignContractList.remove(tid);
            factorContractList.add(tid);
        }

        if (needSellerSignContractList.indexOf(tid) >= 0) {
            _factoringPrepareDataContract.sellerSign(tid, content, expireDate);
            needSellerSignContractList.remove(tid);
            sellerContractList.add(tid);
        }
    }

    /**
     * 获取账户名称。用于账户的识别
     * @return 账户名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置账户名称。用户账户的识别
     * @param name 账户名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取作为核心企业时的保理准备列表
     * @return 作为核心企业时的保理准备列表
     */
    public List<Integer> getBuyerContractList() {
        return buyerContractList;
    }

    /**
     * 获取作为供应商时的保理准备列表
     * @return 作为供应商时的保理准备列表
     */
    public List<Integer> getSellerContractList() {
        return sellerContractList;
    }

    /**
     * 获取作为保理方时的保理准备列表
     * @return 作为保理方时的保理准备列表
     */
    public List<Integer> getFactorContractList() {
        return factorContractList;
    }

    /**
     * 获取作为供应商时，待签名的保理准备列表
     * @return 作为供应商时，待签名的保理准备列表
     */
    public List<Integer> getNeedSellerSignContractList() {
        return needSellerSignContractList;
    }

    /**
     * 获取作为保理方时，待签名的保理准备列表
     * @return 作为保理方时，待签名的保理准备列表
     */
    public List<Integer> getNeedFactorSignContractList() {
        return needFactorSignContractList;
    }

    /**
     * 获取账户的KEY信息
     * @return 账户的KEY信息
     */
    public Credentials getCredendials() {
        return credentials;
    }

    @Override
    public String toString () {
        return getName();
    }

    private HelloWorldContract _helloWorldContract;
    private FactoringPrepareDataContract _factoringPrepareDataContract;
    private String name;
    private Credentials credentials;
    private List<Integer> buyerContractList;
    private List<Integer> sellerContractList;
    private List<Integer> factorContractList;
    private List<Integer> needSellerSignContractList;
    private List<Integer> needFactorSignContractList;
}
