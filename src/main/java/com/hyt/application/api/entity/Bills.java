package com.hyt.application.api.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

/**
 * 单据实体
 *
 * @author Heck H
 * date 20220614
 */
public class Bills {

    /**
     * 合同号
     */
    private String contractNo;

    /**
     * 发票号
     */
    private String invoiceNo;


}
