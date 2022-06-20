package com.hyt.application.api.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

/**
 * 报关单实体
 *
 * @author Heck H
 * date 20220614
 */
public class CustomsDeclaration {

    /**
     * 报关单号
     */
    private String customsNo;

    /**
     * 报关单总金额
     */
    private String totalAmt;

    /**
     * 报关单金额
     */
    private String customsAmt;

    /**
     * 报关单币种
     */
    private String currencyType;

    /**
     * 报关单日期
     */
    private String customsDate;

    /**
     * 启运国
     */
    private String loadingCountry;

    /**
     * 占用金额
     */
    private String occuAmt;

}
