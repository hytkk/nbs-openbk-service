package com.hyt.application.api.request;


import com.hyt.application.api.entity.Bills;
import com.hyt.application.api.entity.CustomsDeclaration;
import com.hyt.application.api.entity.FileInfo;
import com.hyt.application.api.entity.RestoreDeclaration;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * 汇出汇款请求体
 *
 * @author Heck H
 * date 20220614
 */
public class OutwardRemittanceRequest {

    /**
     * 前台流水号
     */
    private String gwFlwNo;

    /**
     * 请求时间
     */
    private String gwReqDate;

    /**
     * 交易时间
     */
    private String gwReqTime;

    /**
     * 交易发起方
     */
    private String channel;

    /**
     * 交易流水号
     */
    private String orderFlowNo;

    /**
     * 证件类型
     */
    private String certType;

    /**
     * 证件号码
     */
    private String certNo;

    /**
     * 付汇类型
     */
    private String imType;

    /**
     * 汇款类型
     */
    private String remtType;

    /**
     * 汇款途径
     */
    private String remtRoute;

    /**
     * 汇款币种
     */
    private String remtCcy;

    /**
     * 汇款金额
     */
    private String remtAmt;

    /**
     * 现汇账号
     */
    private String cashAccountNo;

    /**
     * 现汇币种
     */
    private String cashAccountCcy;

    /**
     * 现汇金额
     */
    private String cashAccountSum;

    /**
     * 购汇账号
     */
    private String purchaseAccountNo;

    /**
     * 购汇币种
     */
    private String purchaseCcy;

    /**
     * 购汇金额
     */
    private String purchaseAmt;

    /**
     * 套汇账号
     */
    private String otherAccountNo;

    /**
     * 套汇币种
     */
    private String otherCcy;

    /**
     * 套汇金额
     */
    private String otherAmt;

    /**
     * 汇款人名称
     */
    private String applicantDetail1;

    /**
     * 汇款人地址
     */
    private String applicantDetail2;

    /**
     * 收款人开户银行代码类型
     */
    private String clsFlag;

    /**
     * 收款人开户银行代码
     */
    private String recvrBankBic;

    /**
     * 收款人开户银行名称
     */
    private String recvrBankNm;

    /**
     * 收款人开户行地址
     */
    private String recOpenBankDetail;

    /**
     * 收款人开户行分支行机构
     */
    private String recOpenBank;

    /**
     * 收款银行代理行Swift Code
     */
    private String agentBankBic;

    /**
     * 收款银行代理行名称
     */
    private String agentBankName;

    /**
     * 收款银行代理行地址
     */
    private String agentBankDetail;

    /**
     * 收款银行代理行账号
     */
    private String agentBankAcc;

    /**
     * 收款人名称
     */
    private String benificiaryDetail1;

    /**
     * 收款人地址
     */
    private String benificiaryDetail2;

    /**
     * 收款人账号
     */
    private String recAccount;

    /**
     * 报文附言
     */
    private String postScript;

    /**
     * 汇款指示
     */
    private String remtRemark;

    /**
     * 收款人国别
     */
    private String country;

    /**
     * 本笔款项是否为保税货物下付款
     */
    private String baosuiPay;

    /**
     * 资金性质
     */
    private String payProp;

    /**
     * 联系人
     */
    private String linkMan;

    /**
     * 联系电话
     */
    private String linkPhone;

    /**
     * 费用承担方
     */
    private String feeBy;

    /**
     * 全额到帐标志
     */
    private String fullFlag;

    /**
     * 扣费账户
     */
    private String feeAcc;

    /**
     * 扣费账户币种
     */
    private String feeAccCcy;

    /**
     * 交易编码1
     */
    private String transCode;

    /**
     * 交易编码2
     */
    private String transCodeTwo;

    /**
     * 交易附言1
     */
    private String remark;

    /**
     * 交易附言2
     */
    private String remarkTwo;

    /**
     * 汇款金额1
     */
    private String remtAmtOne;

    /**
     * 汇款金额2
     */
    private String remtAmtTwo;

    /**
     * 预计报关单日期
     */
    private String billVerDt;

    /**
     * 品名1
     */
    private String itemNoOne;

    /**
     * 品名2
     */
    private String itemNoTwo;

    /**
     * 关联企业标志
     */
    private String relCompanyFlag;

    /**
     * 外汇局批件号
     */
    private String busiNo;

    /**
     * 所属行业
     */
    private String sector;

    /**
     * 支付用途
     */
    private String paymentPurpose;

    /**
     * 详细用途
     */
    private String detailPurpose;

    /**
     * 已阅字段
     */
    private String readFlg;

    /**
     * 单据信息
     */
    private List<Bills> list1;

    /**
     * 报关单信息
     */
    private List<CustomsDeclaration> list2;

    /**
     * 还原申报信息
     */
    private List<RestoreDeclaration> list3;

    /**
     * 附件文件信息
     */
    private List<FileInfo> fileList;

    /**
     * serviceId
     */
    private String serviceId;

    /**
     * 加签后的密文
     */
    private String signData;


}
