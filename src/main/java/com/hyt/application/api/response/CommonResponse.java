package com.hyt.application.api.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

/**
 * 公共返回体
 *
 * @author Heck H
 * date 20220614
 */
@Data
public class CommonResponse extends HashMap<String,Object> {

    /**
     * 返回的状态码
     *
     * @param code 状态码
     * @return CommonResponse
     */
    public  CommonResponse code(String code){
        this.put("errCode",code);
        return this;
    }

    /**
     * 返回的状态码
     *
     * @param ok 状态码
     * @return CommonResponse
     */
    public  CommonResponse code(HttpStatus ok){
        this.put("errCode",ok.value());
        return this;
    }

    /**
     * 返回成功的构造方法
     *
     * @return CommonResponse
     */
    public CommonResponse success(){
        this.code("000000");
        return this;
    }

    /**
     * 返回请求备注信息
     *
     * @param message 信息
     * @return CommonResponse
     */
    public CommonResponse message(String message){
        this.put("errMessage",message);
        return this;
    }

    /**
     * 返回数据
     *
     * @param data 数据信息
     * @return CommonResponse
     */
    public CommonResponse data(Object data){
        this.put("data",data);
        return this;
    }

    /**
     * 失败返回码
     *
     * @return CommonResponse
     */
    public CommonResponse fail(){
        this.code("999999");
        return this;
    }

}
