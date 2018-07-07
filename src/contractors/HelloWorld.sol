pragma solidity ^0.4.23;

/**
* 你好世界合约
*/
contract HelloWorld{
    string name; //名字

    /**
    * 构造函数
    */
    function HelloWorld(){
       name = "Hi,Welcome!";
    }

    /**
    * 获取名字
    */
    function get()constant returns(string){
        return name;
    }

    /**
    * 设置名字
    */
    function set(string n){
    	name=n;
    }
}