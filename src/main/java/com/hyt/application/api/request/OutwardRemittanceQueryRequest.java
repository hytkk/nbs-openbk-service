package com.hyt.application.api.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

/**
 * 汇出汇款查询请求体
 *
 * @author Heck H
 * date 20220614
 */
public class OutwardRemittanceQueryRequest {

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
     * 国金交易流水号
     */
    private String serviceFlowNo;

    /**
     * 交易流水号
     */
    private String orderFlowNo;

    /**
     * 交易日期
     */
    private String txDt;

    /**
     * 交易类型
     */
    private String txType;

}
