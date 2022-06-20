import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 银企直连报文测试
 * </br>
 * utf-8程序
 *
 * @author liuhao
 * @date 20161121
 */
public class DirectlinkTest {
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

    public static void main(String[] args) {
        DirectlinkTest test = new DirectlinkTest();
        try {
            System.out.println("发送登录请求：");
            test.executeServerHttpService("srv001_signOn");
            System.out.println("\n发送跨行转账请求：");
            test.executeServerHttpService("srv007_singleOuterTransfer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行交易
     *
     * @param serviceId
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public void executeServerHttpService(String serviceId) throws Exception {
        // 处理交易的url
        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (cookies != null) {
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
        parseReturnValue(retStr);
    }

    /**
     * 组装完整的报文
     *
     * @param serviceId
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String getRequestData(String serviceId)
            throws FileNotFoundException, IOException {
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
        String serialNo = String.valueOf(Math.round(Math.random() * 10000))
                + System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        String reqTime = sdf.format(date);
        reqData = reqData.replaceFirst("\\$\\{serialNo\\}", serialNo);
        reqData = reqData.replaceFirst("\\$\\{reqTime\\}", reqTime);
        String signData = null;
        try {
            signData = signData(reqData);
            signData = getNodeValue(signData, "signed_data");
        } catch (Exception e) {
            e.printStackTrace();
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
     * 处理返回信息
     *
     * @param src
     * @throws Exception
     */
    private void parseReturnValue(String src) throws Exception {
        int beginIndex = src.indexOf("<sessionId>");
        int endIndex = src.indexOf("</sessionId>");
        String sessionIdRep = "";
        if (beginIndex != -1 && endIndex != -1) {
            sessionIdRep = src.substring(beginIndex + 11, endIndex);
        }
        if (!sessionIdRep.equals("-1") && !sessionId.equals("-1")
                && !sessionId.equals(sessionIdRep))
            throw new Exception("上送的sessionId和返回的sessionId不一致! " + src);
        if (sessionId.equals("-1"))
            sessionId = sessionIdRep;
        retCode = src.substring(src.indexOf("<retCode>") + 9,
                src.indexOf("</retCode>"));
        try {
            errorMessage = src.substring(src.indexOf("<errorMsg>") + 10,
                    src.indexOf("</errorMsg>"));
        } catch (Exception e) {
        }
        if (retCode == null || !retCode.equals("0000")) {
            throw new Exception("ServerException: ErrorCode=" + retCode
                    + " ErrorMessage=" + errorMessage);
        }
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

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }
}
