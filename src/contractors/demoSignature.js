/*
privKey : 0xd38d7924a1e3ebb0bf6495459f53a692b1adf2638214e21b175bc82f813a0194
pubKey : 0x8223e08f75cb25d50e498a77283858197a36343f142c1099b6c54f45fa2cad9f3e37e2cd3e4c0459290b090e7c97728ed9623cf24c4d588ae909da70a0d7a594
address : 0x82a060a33d6178872cee3c7e453aabbe476751df

privKey : 0x722ed9f6967e322ab35f19c05ab4a626cdf33289cc3cc12057903ab3bdf812f3
pubKey : 0xdf0435a29dbb39b71f014aa0318ee8cab79725281a68ef6ab495d009984fbe9d9e58bfd77397564b6155dfab0ba30945cef8a8c7b4a7608a3b3b42a4aa9fe2da
address : 0xfdcfaca7e7bbcfa831f2da45fae2830a4f79c085
*/

var Web3= require('web3');
var config=require('./config');
var fs=require('fs');
var execSync =require('child_process').execSync;
var web3sync = require('./web3sync');
var BigNumber = require('bignumber.js');


if (typeof web3 !== 'undefined') {
  web3 = new Web3(web3.currentProvider);
} else {
  web3 = new Web3(new Web3.providers.HttpProvider(config.HttpProvider));
}

console.log(config);

var filename="Signature";
var address= fs.readFileSync(config.Ouputpath+filename+'.address','utf-8');
var abi= JSON.parse(fs.readFileSync(config.Ouputpath/*+filename+".sol:"*/+filename+'.abi', 'utf-8'));
var contract = web3.eth.contract(abi);
var instance = contract.at(address);

console.log(filename+"合约address:"+address);



(async function(){
  //var name=instance.testQuery(0);
  //console.log("接口调用后读取接口返回:"+name.toString());
  var privateKey = new Buffer("d38d7924a1e3ebb0bf6495459f53a692b1adf2638214e21b175bc82f813a0194", 'hex');
  var plainText = "HelloWorld";
console.log(plainText);
  var plainBuffer = new Buffer(plainText);
  var prefixBuffer = new Buffer("\x19Ethereum Signed Message:\n32");
//  console.log("0x" + plainBuffer.toString("hex"));
//  var hash = web3sync.sha3(Buffer.concat([prefixBuffer, web3sync.sha3(plainBuffer)]));
  var hash = web3sync.sha3(plainBuffer);
  var sign = web3sync.ecsign(hash, privateKey); 
  var signature = sign.v.toString(16) + sign.r.toString("hex") + sign.s.toString("hex");

console.log(sign);
 //var addr = web3sync.ecrecover(hash, sign.r, sign.s, sign.v);

console.log(hash.toString("hex"));
console.log(signature);

//  var func = "ecrecoverAccount(bytes32,bytes)";
//  var params = ["0x" + hash.toString("hex"), "0x" + signature];
//  var receipt = await web3sync.sendRawTransaction(config.account, config.privKey, address, func, params);
//  console.log("调用更新接口"+'(交易哈希：'+receipt.transactionHash+')');
  name=instance.ecrecoverAccount("0x" + hash.toString("hex"), "0x" + signature);
  console.log(name.toString());

})()
