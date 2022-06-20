package com.hyt.application.service;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 登录前置机服务
 *
 * @author Heck H
 * date 20220614
 */

@Slf4j
public class DirectlinkService {

    /**
     * 登录报文原文
     */
    private static final String SIGN_MESSAGE = "<opReq><serialNo>${serialNo}</serialNo><reqTime>${reqTime}</reqTime><ReqParam><userID>0000005332</userID><userPWD>123456</userPWD></ReqParam></opReq>";
    /**
     * 转账报文原文
     */
    private static final String TRADE_MESSAGE = "<opReq><serialNo>${serialNo}</serialNo><reqTime>${reqTime}</reqTime><ReqParam><FKZH>82910120102027288</FKZH><SKZH>6228481060643871117</SKZH><SKHM>王苏平</SKHM><SKYH>中国农业银行舟山市定海支行营业中心</SKYH><SKSH>浙江省</SKSH><SKSI>舟山市</SKSI><JYJE>0.01</JYJE><BIZH>01</BIZH><ZZLX>02</ZZLX><ZZLB>0</ZZLB><YOTU>银企接口测试</YOTU><SKHH>103342040518</SKHH></ReqParam></opReq>";
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
     * 返回码
     */
    private String retCode;
    /**
     * 返回信息
     */
    private String errorMessage;

    /**
     * 连接至前置机
     */
    public void linkToOpenBK(){
        DirectlinkService service = new DirectlinkService();
        try {
            log.info("发送登录请求：");
            service.executeServerHttpService("srv001_signOn");
            log.info("发送跨行转账请求：");
            service.executeServerHttpService("srv007_singleOuterTransfer");
        }catch (Exception e) {
            log.error(e.toString());
        }


    }

    /**
     * 执行交易
     *
     * @param serviceId
     * @throws Exception
     */
    public void executeServerHttpService(String serviceId) throws Exception{

        // 处理交易的url
        java.net.URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if(cookies != null){
            // 如果已登录，设置coikie
            connection.setRequestProperty("cokie", cookies);
        }
        // 请求以utf-8格式编码
        connection.setRequestProperty("content-type", "text/xml;charset=utf-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        // post方式发送
        connection.setRequestMethod("POST");
        OutputStream out = connection.getOutputStream();
        // 组装完整的交易报文
        String reqData = getRequestData(serviceId);
    }
    /**
     * 组装完整的报文
     *
     * @param serviceId
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String getRequestData(String serviceId) throws FileNotFoundException, IOException{
        //Todo
        //需要把这部分报文修改成json
        StringBuffer buf = new StringBuffer();
        // 报文编码格式utf-8
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><NBCBEBankData>");
        buf.append("<sessionId>");
        buf.append(sessionId);
        buf.append("</sessionId>");
        buf.append("<serviceId>");
        buf.append(serviceId);
        buf.append("</serviceId>");
        String reqData = "";
        if (serviceId.equals("srv001_signOn")) {
            reqData = SIGN_MESSAGE;
        } else if (serviceId.equals("srv007_singleOuterTransfer")) {
            reqData = TRADE_MESSAGE;
        }
        //序列号
        String serialNo = String.valueOf(Math.round(Math.random() * 10000))
                + System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String reqTime = sdf.format(date);
        reqData = reqData.replaceFirst("\\$\\{serialNo\\}", serialNo);
        reqData = reqData.replaceFirst("\\$\\{reqTime\\}", reqTime);
        String signData = null;
        try{
            signData = signData(reqData);
            //获取密文
            signData = getNodeValue(signData, "signed_data");
        }catch (Exception e){
            log.error(e.toString());
        }
        buf.append("<customerId>");
        buf.append("0000005332");
        buf.append("</customerId>");
        buf.append("<softwareId>");
        buf.append("002");
        buf.append("</softwareId>");
        buf.append("<functionId>");
        buf.append(serviceId.substring(0, 6));
        buf.append("</functionId>");
        buf.append("<functionName>");
        buf.append("测试功能测试功能测试");
        buf.append("</functionName>");
        buf.append(reqData + "<signData>" + signData
                + "</signData></NBCBEBankData>");
        return buf.toString();

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
     * </br>
     * 3.要注意请求报文中不得包含GBK之外的字符，否则会在银企处验签失败
     * </br>
     *
     * @param orignalData 原报文体
     * @return
     * @throws UnsupportedEncodingException
     */
    public String signData(String orignalData) throws UnsupportedEncodingException {
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
            log.error(e.toString());
        }
        return "";
    }

    /**
     * 从加密返回密文中取出signData
     * 也即获取密文
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


}
