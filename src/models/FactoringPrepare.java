package models;

/**
 * 保理准备
 */
public class FactoringPrepare {
    public Integer getTid() {
        return tid;
    }

    public void setTid(Integer tid) {
        this.tid = tid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getBuyerSignature() {
        return BuyerSignature;
    }

    public void setBuyerSignature(String buyerSignature) {
        BuyerSignature = buyerSignature;
    }

    public String getSellerSignature() {
        return SellerSignature;
    }

    public void setSellerSignature(String sellerSignature) {
        SellerSignature = sellerSignature;
    }

    public String getFactorSignature() {
        return FactorSignature;
    }

    public void setFactorSignature(String factorSignature) {
        FactorSignature = factorSignature;
    }

    private Integer tid;
    private String content; //保理准备合同内容
    private String expireDate; //到期日
    private String BuyerSignature; //核心企业签名
    private String SellerSignature; //供应商签名
    private String FactorSignature; //保理方签名
}
