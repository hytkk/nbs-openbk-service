package com.hyt.application.service;

import com.alibaba.fastjson.JSON;
import com.hyt.application.api.request.OutwardRemittanceQueryRequest;
import com.hyt.application.api.request.OutwardRemittanceRequest;
import com.hyt.application.api.response.OutwardRemittanceQueryResponse;
import com.hyt.application.api.response.OutwardRemittanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

/**
 * 汇款服务
 *
 * @author Heck H
 * date 20220614
 */

@Service
@Slf4j
public class OutwardRemittanceService {

    /**
     * 接收报文url
     */
    private static final String URL = "http://127.0.0.1:8080/directlink/httpAccess";
    /**
     * 签名ip
     */
    private static final String VERIFY_IP = "127.0.0.1";
    /**
     * 签名端口
     */
    private static final int VERIFY_PORT = 8010;

    /**
     * cookies
     */
    private String cookies = null;
    /**
     * sessionId
     */
    private String sessionId = "-1";

    /**
     * 汇出汇款方法实现
     *
     * @param outwardRemittanceRequest 汇款请求
     * @return OutwardRemittanceResponse
     */
    public OutwardRemittanceResponse outwardRemittance(OutwardRemittanceRequest outwardRemittanceRequest){
        OutwardRemittanceResponse remittanceResponse = new OutwardRemittanceResponse();
        try{
            remittanceResponse =  executeServerHttpServiceOut(outwardRemittanceRequest);
        }catch (Exception e){
            log.error(e.toString());
        }
        return remittanceResponse;
    }

    /**
     *
     * @param outwardRemittanceQueryRequest
     * @return
     */
    public OutwardRemittanceQueryResponse outwardRemittanceQuery(OutwardRemittanceQueryRequest outwardRemittanceQueryRequest){
        return null;
    }

    /**
     * 执行交易
     *
     * @param
     * @throws Exception
     */
    public OutwardRemittanceResponse executeServerHttpServiceOut(OutwardRemittanceRequest outwardRemittanceRequest) throws Exception {
        // 处理交易的url
        java.net.URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (cookies != null) {
            // 如果已登录，设置coikie
            connection.setRequestProperty("cokie", cookies);
        }
        // 请求以utf-8格式编码并且已json格式发送报文
        connection.setRequestProperty("content-type", "application/json;charset=utf-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        // post方式发送
        connection.setRequestMethod("POST");
        OutputStream out = connection.getOutputStream();
        // 组装完整的交易报文
        String reqData = getRequestDataOut(outwardRemittanceRequest);
        // utf-8 编码发送
        System.out.println("请求报文：\n" + reqData);
        out.write(reqData.getBytes("UTF-8"));
        out.flush();
        out.close();
        int retCode = connection.getResponseCode();
        if (retCode != 200) {
            throw new Exception("交易出错！retCode=" + retCode);
        }
        // 设置cookie
        Map heads = connection.getHeaderFields();
        Object[] headNames = heads.keySet().toArray();
        for (int i = 0; i < headNames.length; i++) {
            if (headNames[i] == null) {
                continue;
            }
            if ("Set-Cookie".equalsIgnoreCase((String) headNames[i])) {
                this.cookies = connection.getHeaderField(headNames[i]
                        .toString());
            }
        }
        int len = connection.getContentLength();
        byte[] retData = readContent(connection.getInputStream(), len);
        String retStr = new String(retData, "UTF-8");
        System.out.println("【返回报文】Ret Data:\n " + retStr);
        OutwardRemittanceResponse remittanceResponse = JSON.parseObject(retStr,OutwardRemittanceResponse.class);
        return remittanceResponse;
        //parseReturnValue(retStr);
    }

    /**
     * 组装完整的报文
     *
     * @param
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String getRequestDataOut(OutwardRemittanceRequest outwardRemittanceRequest)
            throws FileNotFoundException, IOException {
        outwardRemittanceRequest.setServiceId("outward_remittance");
        String reqData = JSON.toJSONString(outwardRemittanceRequest);
        String signData = null;
        try {
            signData = signData(reqData);
            signData = getNodeValue(signData, "signed_data");
            outwardRemittanceRequest.setSignData(signData);;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String requestData = JSON.toJSONString(outwardRemittanceRequest);
        return requestData;
    }

    /**
     * 读取返回信息
     *
     * @param in
     * @param len
     * @return
     * @throws IOException
     */
    private byte[] readContent(InputStream in, int len) throws IOException {
        if (len <= 0) {
            byte[] buf = new byte[2048];
            byte[] readBuf = new byte[1024];
            int totalLen = 0;
            while (true) {
                int readLen = in.read(readBuf);
                if (readLen <= 0)
                    break;
                if (totalLen + readLen > buf.length) {
                    byte[] tmp = new byte[buf.length + readLen + 1024];
                    System.arraycopy(buf, 0, tmp, 0, totalLen);
                    buf = tmp;
                }
                System.arraycopy(readBuf, 0, buf, totalLen, readLen);
                totalLen = totalLen + readLen;
            }
            byte[] retBuf = new byte[totalLen];
            System.arraycopy(buf, 0, retBuf, 0, totalLen);
            return retBuf;
        } else {
            int totalLen = 0;
            byte[] buf = new byte[len];
            while (totalLen < len) {
                int readLen = in.read(buf, totalLen, len - totalLen);
                if (readLen <= 0)
                    break;
                totalLen = totalLen + readLen;
            }
            return buf;
        }
    }

    /**
     * 从加密返回密文中取出signData
     *
     * @param returnStr
     * @param tagName
     * @return
     */
    public static String getNodeValue(String returnStr, String tagName) {
        int startIdx, endIdx;
        String beginTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        startIdx = returnStr.indexOf(beginTag) + beginTag.length();
        endIdx = returnStr.indexOf(endTag, startIdx);
        return returnStr.substring(startIdx, endIdx);
    }

    /**
     * 数据加密
     * </br>
     * 重点
     * </br>
     * 1.orignalData.getBytes("GBK").length 获取GBK编码格式的字节长度，为了通过格式验证
     * </br>
     * 2.new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"GBK")); 指定流的编码格式，使加密服务接收到正确的中文字符
     * </br>
     *
     * @param orignalData 原报文体
     * @return
     * @throws UnsupportedEncodingException
     */
    public String signData(String orignalData)
            throws UnsupportedEncodingException {
        InetAddress addr = null;
        Socket socket = null;
        // 加密报文编码格式gbk
        String verifyheader = "<?xml version=\"1.0\" encoding=\"gbk\"?>\n"
                + "<msg>\n" + "<msg_head>\n" + "<msg_type>0</msg_type>\n"
                + "<msg_id>1005</msg_id>\n" + "<msg_sn>0</msg_sn>\n"
                + "<version>1</version>\n" + "</msg_head>\n" + "<msg_body>\n"
                + "<origin_data_len>" + orignalData.getBytes("GBK").length
                + "</origin_data_len>\n" + "<origin_data>" + orignalData
                + "</origin_data>\n" + "</msg_body>\n" + "</msg>";
        try {
            addr = InetAddress.getByName(VERIFY_IP);
            socket = new Socket(addr, VERIFY_PORT);
            // 发送
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(),"GBK"));
            System.out.println("【数据加密】加密的明文：" + verifyheader);
            wr.write(verifyheader);
            wr.flush();
            // 接收
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(),"ISO8859-1"));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            // 处理返回
            String returnStr = new String(sb.toString().getBytes("ISO8859-1"),"GBK");
            System.out.println("【数据加密】返回的密文：" + returnStr);
            wr.close();
            rd.close();
            socket.close();
            return returnStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
