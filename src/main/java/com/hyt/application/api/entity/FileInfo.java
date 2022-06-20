package com.hyt.application.api.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

/**
 * 文件信息实体
 *
 * @author Heck H
 * date 20220614
 */
public class FileInfo {

    /**
     * 附件文件名
     */
    private String fileName;
}
