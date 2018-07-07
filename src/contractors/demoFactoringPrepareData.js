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
;
var filename="FactoringPrepareData";
var address= fs.readFileSync(config.Ouputpath+filename+'.address','utf-8');
var abi= JSON.parse(fs.readFileSync(config.Ouputpath/*+filename+".sol:"*/+filename+'.abi', 'utf-8'));
var contract = web3.eth.contract(abi);
var instance = contract.at(address);

console.log(filename+"合约address:"+address);



(async function(){
  var func = "setSignatureAddress(address)";
  var params = ["0xe59d2acdd929b63777290b1d4cd793830d9c77c2"];
  //var receipt = await web3sync.sendRawTransaction(config.account, config.privKey, address, func, params);
  //console.log("已设置验签合约地址");

  var prefixBuffer = new Buffer("\x19Ethereum Signed Message:\n32");

for (var i = 0; i < 1; i++) {
  var contractContent = "This is a Contract";
//var contractContent = fs.readFileSync('/home/md07/allinpay/code/goodboss/biz/src/main/resources/com/allinpay/gb/mapping/DmfLoanApplicationMapper.xml','utf-8');
  var expireTime = "2019-12-01";
  var plainBuffer = new Buffer(contractContent + expireTime);
  //var hash = web3sync.sha3(Buffer.concat([prefixBuffer, web3sync.sha3(plainBuffer)]));
  var hash = web3sync.sha3(plainBuffer);
  var sign = web3sync.ecsign(hash, new Buffer(config.privKey, "hex")); 
  var signature = sign.v.toString(16) + sign.r.toString("hex") + sign.s.toString("hex");
  
console.log(hash);
console.log(sign);

  func = "create(address,address,string,string,bytes)";
  params = ["0x82a060a33d6178872cee3c7e453aabbe476751df", "0xfdcfaca7e7bbcfa831f2da45fae2830a4f79c085", contractContent, expireTime, "0x" + signature];
  console.log("step0");
  receipt = await web3sync.sendRawTransaction(config.account, config.privKey, address, func, params);
  console.log("已添加保理准备");

  console.log("step1");
  var index = instance.getContractCount();
  console.log(index);

  console.log("step3");
var s = (index - 1).toString(16);
if (s.length % 2 == 1) {
    s = "0" + s;
}

  plainBuffer = new Buffer(contractContent + expireTime);
  //var hash = web3sync.sha3(Buffer.concat([prefixBuffer, web3sync.sha3(plainBuffer)]));
  hash = web3sync.sha3(plainBuffer);
  sign = web3sync.ecsign(hash, new Buffer("d38d7924a1e3ebb0bf6495459f53a692b1adf2638214e21b175bc82f813a0194", "hex")); 
  signature = sign.v.toString(16) + sign.r.toString("hex") + sign.s.toString("hex");
 
 
  func = "sellerSign(uint256,bytes)";
  params = [index - 1, "0x" + signature];
  console.log("step4");
  receipt = await web3sync.sendRawTransaction("0x82a060a33d6178872cee3c7e453aabbe476751df", "d38d7924a1e3ebb0bf6495459f53a692b1adf2638214e21b175bc82f813a0194", address, func, params);
  console.log("供应商已签名");

  plainBuffer = new Buffer(contractContent + expireTime);
  //var hash = web3sync.sha3(Buffer.concat([prefixBuffer, web3sync.sha3(plainBuffer)]));
  hash = web3sync.sha3(plainBuffer);
  sign = web3sync.ecsign(hash, new Buffer("722ed9f6967e322ab35f19c05ab4a626cdf33289cc3cc12057903ab3bdf812f3", "hex")); 
  signature = sign.v.toString(16) + sign.r.toString("hex") + sign.s.toString("hex");
  
  func = "factorSign(uint256,bytes)";
  params = [index - 1, "0x" + signature];
  console.log("step5");
  //receipt = await web3sync.sendRawTransaction("0xfdcfaca7e7bbcfa831f2da45fae2830a4f79c085", "722ed9f6967e322ab35f19c05ab4a626cdf33289cc3cc12057903ab3bdf812f3", address, func, params);
  //console.log("保理商已签名");

  plainBuffer = Buffer.alloc(32);
  var tempBuffer = Buffer.from(s, "hex");
  tempBuffer.copy(plainBuffer, 32 - tempBuffer.length);
  hash = web3sync.sha3(plainBuffer);
  sign = web3sync.ecsign(hash, new Buffer(config.privKey, "hex")); 
  signature = sign.v.toString(16) + sign.r.toString("hex") + sign.s.toString("hex");
  console.log(instance.getContractContent("0x" + plainBuffer.toString("hex"), "0x" + signature));
  console.log(instance.getContractSignature("0x" + plainBuffer.toString("hex"), "0x" + signature));
}
})()
