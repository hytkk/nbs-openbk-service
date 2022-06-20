package com.hyt.application.api.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

/**
 * 还原申报实体
 *
 * @author Heck H
 * date 20220614
 */
public class RestoreDeclaration {

    /**
     * 企业名称
     */
    private String coName;

    /**
     * 交易编码
     */
    private String tradeCode;

    /**
     * 币种
     */
    private String tradeCcy;

    /**
     * 金额
     */
    private String tradeAmt;

    /**
     * 公司组织机构号
     */
    private String kernelCredNo;

    /**
     * 帐号
     */
    private String acc;

}
