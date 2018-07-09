# 创世纪 demo
在学习区块链开发之初，就有意记录下自己的学习，但是当真正完成了这个demo，又觉得可写的内容乏善可陈。不过无论如何，我还是会记录下这次的学习过程。

这次的学习，目的很明确，`基于BCOS通过web3j完成区块链项目的开发`。<br/>
**BCOS和FISCO-BCOS的简介：**<br/>
- **BCOS** Block Chain Open Source，是基于以太坊的区块链底层技术平台，应用于联盟链或者私链。和以太坊相比，去掉了代币和钱包，增加了节点的CA认证，共识机制支持PBFT和RAFT，出块速度为秒出块。
- **FISCO-BCOS**，基于BCOS，增加了权限体系等深度优化，有交易时出块。其自我评价为：达到了金融级的安全和性能，工业级的运维。<br/>

BCOS地址：https://github.com/bcosorg/bcos <br/>
FISCO-BCOS地址：https://github.com/FISCO-BCOS/FISCO-BCOS

本次只是针对了BCOS学习，尚不包括FISCO-BCOS。以下内容均来自自己的理解，可能存在偏差，若有发现，烦请指出，共同进步。<br/>
为方便说明，BCOS所在的路径标识为`$BCOS`，节点所在路径标识为`$BCOS-DATA`

分享的内容包括：
## 账户
1. 账户的本质就是一组非对称密钥，包括私有密钥和公开密钥，其中公钥可以生成一个位数更短的账户地址。
2. 密钥和链没有必然联系，同一密钥可以用于任何链
3. 密钥和节点没有必然联系，同一密钥在同一链的任何节点均可以使用
4. 有两种生成的方式，并对应各自不同的管理方式：<br/>
  - 进入到`$BCOS/tool`目录下，运行`babel-node accountManager.js`，即可生成私钥、公钥和账户地址。<br/>
  ![Github](https://raw.githubusercontent.com/bluecryolite/genesis/master/doc/images/02account.png) <br/>
        特征：
    - 私钥为明文，不过，还是可以在自己的应用中运用加密技术对其加密；
    - 在需要签名的时候才使用；
    - 可以用于制作为USBKEY；
    - 不受节点的限制
  - 使用SDK，或者命令行工具，运行`personal.newAccount`，即可在该节点的`$BCOS-DATA/keystore`生成一个有用户密码保护的账户文件，其本质还是非对称密钥对。在公开链中，节点关联的第一个账户，即为挖矿的受益账户。<br/>
  ![Github](https://raw.githubusercontent.com/bluecryolite/genesis/master/doc/images/03account.png) <br/>
  [账户文件示例](https://raw.githubusercontent.com/bluecryolite/genesis/master/doc/images/account.json) <br/>
        特征：
    - 必须和节点建立关系才能使用（拷贝文件到$BCOS-DATA/keystore后重启节点，或者使用SDK或命令行工具，运行`personal.importAccount`建立联系）；
    - 必须通过用户密码，让账户处于解锁状态后（`personal.unlockAccount`）才能使用；
    - 因而，带来的风险为：在账户再次锁定前，任何知道账户地址的人，都可以使用该账户发起交易

**这里强调一下：** 公钥用于加密和验签，私钥用于解密和签名。如果有谁说用私钥加密公钥解密，那就是胡说八道，小心辨析其说的另外的观点。

## 数据保存
1. 物理数据库为key-value的`LevelDB`。无论是区块、账户、合约中的状态（本地变量），均采用key-value形式组织
2. 区块上保留有每次交易的信息，以及每次交易时生成的数据。因此针对账户余额，或者合约状态，均可追踪到变更历史
3. 每个智能合约都关联着一个能容纳整个宇宙的数组（2^256）来保存合约的状态（本地变量）。 参看：https://segmentfault.com/a/1190000013791133

## 链上数据的读写
1. 可以理解为：链提供了若干微服务，SDK（支持Node JS的`web3`或者支持JAVA的`web3j`）通过RPC实现微服务的调用
2. 一个交易的过程：
  - 在节点上发起一个交易，该交易经过节点的验证后，成为pending transaction，并产生一个Transaction Hash，并向全链广播
  - 等待区块产生
  - 区块产生后，记录该交易（若是合约调用，则同时运行合约方法），并生成一个交易收据（Transaction Receipt），可以由前述HASH查询到。如果合约方法触发了事件，则事件参数会记录在交易数据的Log中。
  ![Github](https://raw.githubusercontent.com/bluecryolite/genesis/master/doc/images/04transaction.png)
  ![Github](https://raw.githubusercontent.com/bluecryolite/genesis/master/doc/images/05log.png)

3. SDK中和读写数据相关的方法：
  - `eth_SendRawTransaction`，通过该方法提交的交易数据，节点不会做检查。其传递的参数为合约方法、调用参数及类型、返回值类型的编码以及请求账户对前述编码的签名组成的编码。适用于第一种方法生成的账户。
  - `eth_SendTransaction`，通过该方法提交的交易数据，节点会检查请求账户（from）是否与节点关联并已解锁。适用于第二种方法生成的账户。
  - `eth_Call`，通过该方法可以查询与账户权限无关的数据。该方法的请求账户（from）会默认为节点关联的第一个账户，如果该节点没有关联账户，则请求账户为：0x00000000000000000000000000000000，因此，该方法无法针对请求账户做权限控制。该方法查询的是本节点上链的数据，不会进行广播。对应的是合约中采用constant returns返回数据的方法。
  - `eth_GetTransactionReceipt`，查询交易数据（Transaction Receipt），并通过收据中Log获取交易触发的事件

## 合约的升级
  - 合约需要区分为：业务合约、数据合约、业务路由合约、数据路由合约
  - 数据合约中，仅完成数据的存取，最多加上访问权限判断（也可以不加，因为直接在链上查询历史数据，从而绕开合约中的权限判断）。数据需要带上版本号。当数据发生了扩展，则生成新的数据合约，并把数据从旧合约迁移到新合约
  - 访问业务合约前，先访问业务路由合约从而得到业务合约的地址。如果业务合约升级生成了新合约，则更新路由合约中的值
  - 在业务合约访问数据前，先访问数据路由合约，得到数据合约的地址。如果数据扩展生成了新合约，则更新路由合约中的值

参看：https://github.com/FISCO-BCOS/Wiki/tree/master/%E6%B5%85%E8%B0%88%E4%BB%A5%E5%A4%AA%E5%9D%8A%E6%99%BA%E8%83%BD%E5%90%88%E7%BA%A6%E7%9A%84%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B8%8E%E5%8D%87%E7%BA%A7%E6%96%B9%E6%B3%95%EF%BB%BF

## Demo的介绍
1. 环境搭建
  - 完成BCOS的安装，参看：<br/>
        https://github.com/bcosorg/bcos/blob/master/doc/manual/manual.md </br>
        https://github.com/bcosorg/bcos/wiki/%E4%B8%80%E9%94%AE%E5%BF%AB%E9%80%9F%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2 <br/>
  - 拷贝项目的Solidity目录下的文件到`$BCOS/tool`目录下。其中：
    - `deploy.js`中，增加了一个编译参数。这是因为BCOS的链比较老，`solidity 0.4.22`以上版本编译的结果，无法调用需要预编译的几个方法，如用于验签的`ecrecover`。参看：https://github.com/ethereum/solidity/issues/3687
    - `web3sync.js`中，修复了一个如果合约参数为空时，序列化合约参数时抛异常的bug（未判断参数数组是否为空）
  - 运行`babel-node deploy.js xxxx`，xxxx包括：HelloWorld、Signature、FactoringPrepareData 
  - 运行`babel-node demoHelloWorld.js`和`babel-node demoSignature.js`，确认合约已部署成功
  - 修改`demoFactoringPrepare.js`，**输入已部署好的Signature的合约地址**。运行 `babel-node demoFactoringPrepare.js`，确认合约已部署成功

2. 异常处理
   Solidity中，有触发异常的代码，如`require`、`assert`，但是还不被web3j支持。<br/>
   目前的解决方法是，出现异常的时候触发事件，在事件中增加code和errorMessage的返回。

3. 开发环境
  - JAVA 1.8
  - 把`dependence`目录下的jar加入到项目中

4. 代码介绍
  - `BCOSContract`，继承自`Contract`。根据Node JS版的SDK重写了发送交易的方法，因为`Contract.executeTransaction`方法提交的交易，始终无法执行合约。同时封装了提交交易和事件触发，使一个完整的交易流程看起来是个同步的流程。修复了`Contract.executeCallSingleValueReturn`的一个bug，在未获取到返回值时，抛出的异常由数组越界改成了未获取到数据。
  - `BCOSAccount`，封装了账户，包括密钥对，以及调用的合约方法。
  - `HelloWorldContract`，封装了你好世界合约的调用。
  - `FactoringPrepareDataContract`，封装了保理准备数据合约的调用。模拟了核心企业、供应商和保理方的三方签名。
  ![Github](https://raw.githubusercontent.com/bluecryolite/genesis/master/doc/images/01home.png)

## 未决事项
  - 合约方法中，参数变量过多，或者合约定义的struct参数过多，会导致编译失败，所以Demo中会切分成getContractContent和getContractSignature两个方法
  - 合约方法直接返回值，SDK无法捕获到，目前采用事件的方式来返回值
  - 区块大小为2.1M，实测只能对20K（准确的阈值待定）以内的文本数据做验签

## 致谢
在学习过程中，得到了库神的`郑凯鸿`先生和币成的`曹银敏`先生在区块链的基础知识方面的帮助，以及`............`在JAVA开发方面的帮助
