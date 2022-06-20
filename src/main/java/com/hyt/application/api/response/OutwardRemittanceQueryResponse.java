package com.hyt.application.api.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

/**
 * 汇款查询返回体
 *
 * @author Heck H
 * date 20220614
 */
public class OutwardRemittanceQueryResponse {

    /**
     * 应答码
     */
    private String errorCode;

    /**
     * 错误描述
     */
    private String errorMsg;

    /**
     * 交易状态
     */
    private String txStatus;

    /**
     * 交易流水号
     */
    private String orderFlowNo;

    /**
     * GPI状态
     */
    private String gpiStatus;

    /**
     * 回单生成时间（用于sdk下载回单拼接下载路径）
     */
    private String replyFileTime;
}
