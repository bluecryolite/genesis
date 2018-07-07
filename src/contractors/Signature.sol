pragma solidity ^0.4.23;

//签名相关方法
//引用自：https://blog.csdn.net/code_segment/article/details/79952722
contract Signature{
    /**
    * 验证签名是否与指定账户匹配
    */
    function  verify(address account, bytes32 sourceHash, bytes signature) constant returns(bool) {
        return account == ecrecoverAccount(sourceHash, signature);
    }

    /**
    * 从签名中获取签名账户
    */
    function  ecrecoverAccount(bytes32 sourceHash, bytes signature) constant returns(address) {
        bytes memory signedBytes = signature;

        bytes32 r = bytesToBytes32(slice(signedBytes, 1, 32));
        bytes32 s = bytesToBytes32(slice(signedBytes, 33, 32));
        byte v1 = slice(signedBytes, 0, 1)[0];
        uint8 v = uint8(v1);
        if (v < 27) v += 27; //在ecrecover源码中，专门判断了v是否为27或者28，之后再减去的27。所以，这里判断了是否需要加27。

        return ecrecover(sourceHash, v, r, s);
    }

    /**
    * 将原始数据按段切割出来指定长度
    */
    function slice(bytes memory data, uint256 start, uint256 len) constant returns (bytes){
        bytes memory b = new bytes(len);

        for(uint i = 0; i < len; i++) {
            b[i] = data[i + start];
        }

        return b;
    }

    /**
    * bytes转换为bytes32
    */
    function bytesToBytes32(bytes memory source) constant returns (bytes32 result) {
        assembly {
            result := mload(add(source, 32))
        }
    }
}

