pragma solidity ^0.4.23;
pragma experimental ABIEncoderV2;
import "Signature.sol";

/**
* 保理准备数据合约
*/
contract FactoringPrepareData{

    struct PrepareContract {
        address buyer; //买方。核心企业
        address seller; //卖方。供应商
        address factor; //保理方
        string contractContent; //保理准备合同内容
        bytes32 contractHash; //保理准备合同内容Hash值（合同内容 + 到期日）
        string expireTime; //到期日
        bytes buyerSignature; //核心企业签名
        bytes sellerSignature; //供应商签名
        bytes factorSignature; //保理方签名
    }

    PrepareContract[] contractList; //保理准备合同列表
    address owner; //合约创建者
    address addrSignature; //签名校验合约地址

    /**
    * 构造函数
    */
    constructor() {
        owner = msg.sender;
    }

    /**
    * 创建保理准备
    */
    function create(address seller, address factor, string contractContent, string expireTime, bytes buyerSignature) {
        //数据校验
        if (!(msg.sender != seller && msg.sender != factor && seller != factor)) {
            emit onContractCreated(101, "交易方重复", 0);
            return;
        }

        //验签
        bytes32 contractHash = sha3(contractContent, expireTime);
        if (!(msg.sender == ecrevoverAccount(contractHash, buyerSignature))) {
            emit onContractCreated(101, "验签失败", 0);
            return;
        }

        contractList.push(PrepareContract(msg.sender, seller, factor, contractContent, contractHash, expireTime, buyerSignature, new bytes(0), new bytes(0)));
        emit onContractCreated(0, "", contractList.length);
    }

    /**
    * 获取保理准备内容。只有保理准备参与方和合约owner能够读取
    */
    function getContractContent(bytes contractIdBytes, bytes signature) constant returns(uint8, string, uint256, address, address, address, string, string) {
        uint256 contractId = calcContractId(contractIdBytes);
        if (!(contractId >= 0 && contractId < contractList.length)) {
            return (101, "合同编号错误", 0, 0x00, 0x00, 0x00, "", "");
        }

        bytes32 hash = sha3(contractIdBytes);
        address sender = ecrevoverAccount(hash, signature);
        PrepareContract ret = contractList[contractId];
        if (!(sender == ret.buyer || sender == ret.seller || sender == ret.factor || sender == owner)) {
            return (101, "不是合同指定的交易方", 0, 0x00, 0x00, 0x00, "", "");
        }

        return (0, "", contractId, ret.buyer, ret.seller, ret.factor, ret.contractContent, ret.expireTime);
    }

    /**
    * 获取保理准备签名。只有保理准备参与方和合约创建者能够读取
    */
    function getContractSignature(bytes contractIdBytes, bytes signature) constant returns(uint8, string, uint256, bytes, bytes, bytes) {
        uint256 contractId = calcContractId(contractIdBytes);
        bytes memory emptyBytes = new bytes(0);
        if (!(contractId >= 0 && contractId < contractList.length)) {
            return (101, "合同编号错误", 0, emptyBytes, emptyBytes, emptyBytes);
        }

        bytes32 hash = sha3(contractIdBytes);
        address sender = ecrevoverAccount(hash, signature);
        PrepareContract ret = contractList[contractId];
        if (!(sender == ret.buyer || sender == ret.seller || sender == ret.factor || sender == owner)) {
            return (101, "不是合同指定的交易方", 0, emptyBytes, emptyBytes, emptyBytes);
        }

        return (0, "", contractId, ret.buyerSignature, ret.sellerSignature, ret.factorSignature);
    }

    /**
    * 获取保理准备数量
    */
    function getContractCount() constant returns(uint256) {
        return contractList.length;
    }

    /**
    * 供应商签名
    */
    function sellerSign(uint contractId, bytes sellerSignature) {
        if (!(contractId >= 0 && contractId < contractList.length)) {
            emit onContractSigned(101, "合同编号错误");
            return;
        }

        PrepareContract result = contractList[contractId];
        bytes32 contractHash = sha3(result.contractContent, result.expireTime);
        if(!(msg.sender == ecrevoverAccount(contractHash, sellerSignature))) {
            emit onContractSigned(101, "验签失败");
            return;
        }

        if(!(msg.sender == result.seller)) {
            emit onContractSigned(101, "不是合同指定的供应商");
            return;
        }

        result.sellerSignature = sellerSignature;
        emit onContractSigned(0, "");
    }

    /**
    * 保理方签名
    */
    function factorSign(uint contractId, bytes factorSignature) {
        if (!(contractId >= 0 && contractId < contractList.length)) {
            emit onContractSigned(101, "合同编号错误");
            return;
        }

        PrepareContract result = contractList[contractId];
        bytes32 contractHash = sha3(result.contractContent, result.expireTime);
        if(!(msg.sender == ecrevoverAccount(contractHash, factorSignature))) {
            emit onContractSigned(101, "验签失败");
            return;
        }

        if(!(msg.sender == result.factor)) {
            emit onContractSigned(101, "不是合同指定的保理商");
            return;
        }

        result.factorSignature = factorSignature;
        emit onContractSigned(0, "");
    }

    /**
    * 设置调用的签名校验合约地址
    */
    function setSignatureAddress(address addr) {
        require(owner == msg.sender, "不是合约创建的账户");
        addrSignature = addr;
    }

    /**
    * 保理准备创建完成事件
    */
    event onContractCreated(uint8, string, uint256);

    /**
    * 保理准备签名完成事件
    */
    event onContractSigned(uint8, string);

    function calcContractId(bytes contractIdBytes) private constant returns(uint256) {
        Signature signController = Signature(addrSignature);
        return uint256(signController.bytesToBytes32(contractIdBytes));
    }

    function ecrevoverAccount(bytes32 hash, bytes signature) private constant returns(address) {
        //验签
        Signature signController = Signature(addrSignature);
        return signController.ecrecoverAccount(hash, signature);
    }
}

