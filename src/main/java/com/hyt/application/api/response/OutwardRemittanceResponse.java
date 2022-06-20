package com.hyt.application.api.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

/**
 * 汇款申请返回体
 *
 * @author Heck H
 * date 20220614
 */
public class OutwardRemittanceResponse {

    /**
     * 应答码
     */
    private String errorCode;

    /**
     * 错误描述
     */
    private String errorMsg;

    /**
     * 国金交易流水号
     */
    private String serviceFlowNo;


}
