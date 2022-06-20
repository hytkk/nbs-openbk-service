import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

/**
 * EMPHttpClient.java<br>
 * Copyright (c) 2000, 2006 e-Channels Corporation <br>
 *
 * @author zhongmc<br>
 * @version 1.0.0<br>
 * @since 2006-11-29<br>
 */
public class LogonTest {
    private String cookies = null;
    private String retCode;
    private String errorMessage;
    private char delim = '|';
    private String hostAddr;
    private String sessionId = "-1";
    private String reqURI;

    public LogonTest() {
    }

    public String getRetCode()
    {
        return retCode;
    }

    public void setRetCode( String retCode )
    {
        this.retCode = retCode;
    }

    public String getHostAddr()
    {
        return hostAddr;
    }

    public void setHostAddr( String hostAddr )
    {
        this.hostAddr = hostAddr;
    }

    public String getReqURI()
    {
        return reqURI;
    }

    public void setReqURI( String reqURI )
    {
        this.reqURI = reqURI;
    }

    public void establishSession() throws Exception
    {
        this.executeServerHttpService( "srv001_signOn" );
    }

    public void executeServerHttpService( String serviceId ) throws Exception
    {
        URL url = new URL( hostAddr + reqURI );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if ( cookies != null )
            connection.setRequestProperty( "cookie", cookies );
        connection.setRequestProperty( "content-type", "text/xml; charset=utf-8" );
        connection.setDoInput( true );
        connection.setDoOutput( true );
        connection.setRequestMethod( "POST" );
        OutputStream out = connection.getOutputStream();
        String reqData = getRequestData( serviceId );
        System.out.println( "【发送报文】Request Data:\n " + reqData );
        out.write( reqData.getBytes( "UTF-8" ) );
        int retCode = connection.getResponseCode();
        if ( retCode != 200 )
        {
            throw new Exception( "Server not available! retCode=" + retCode );
        }
        // do process the server's cookie settings such as session trace
        Map heads = connection.getHeaderFields();
        Object[] headNames = heads.keySet().toArray();
        for ( int i = 0; i < headNames.length; i++ )
        {
            if ( headNames[i] == null )
                continue;
            if ( "Set-Cookie".equalsIgnoreCase( (String) headNames[i] ) )
                this.cookies = connection.getHeaderField( (headNames[i]).toString() );
            // System.out.println( headNames[i] + "=" + connection.getHeaderField( (headNames[i]).toString() ) );
        }
        int len = connection.getContentLength();
        byte[] retData = readContent( connection.getInputStream(), len );
        String retStr = new String( retData, "UTF-8" );
        System.out.println( "【发送报文】Ret Data:\n " + retStr );
        parseReturnValue( retStr );
    }

    // 处理返回
    private void parseReturnValue( String src ) throws Exception
    {
        int beginIndex = src.indexOf( "<sessionId>" );
        int endIndex = src.indexOf( "</sessionId>" );
        String sessionIdRep = "";
        if ( beginIndex != -1 && endIndex != -1 )
        {
            sessionIdRep = src.substring( beginIndex + 11, endIndex );
        }
        if ( !sessionIdRep.equals( "-1" ) && !sessionId.equals( "-1" ) && !sessionId.equals( sessionIdRep ) )
            throw new Exception( "上送的sessionId和返回的sessionId不一致! " + src );
        if ( sessionId.equals( "-1" ) )
            sessionId = sessionIdRep;
        retCode = src.substring( src.indexOf( "<retCode>" ) + 9, src.indexOf( "</retCode>" ) );
        try
        {
            errorMessage = src.substring( src.indexOf( "<errorMsg>" ) + 10, src.indexOf( "</errorMsg>" ) );
        }
        catch ( Exception e )
        {
        }
        if ( retCode == null || !retCode.equals( "0000" ) )
        {
            throw new Exception( "ServerException: ErrorCode=" + retCode + " ErrorMessage=" + errorMessage );
        }
    }

    private byte[] readContent( InputStream in, int len ) throws IOException
    {
        if ( len <= 0 )
        {
            byte[] buf = new byte[2048];
            byte[] readBuf = new byte[1024];
            int totalLen = 0;
            while ( true )
            {
                int readLen = in.read( readBuf );
                if ( readLen <= 0 )
                    break;
                if ( totalLen + readLen > buf.length )
                {
                    byte[] tmp = new byte[buf.length + readLen + 1024];
                    System.arraycopy( buf, 0, tmp, 0, totalLen );
                    buf = tmp;
                }
                System.arraycopy( readBuf, 0, buf, totalLen, readLen );
                totalLen = totalLen + readLen;
            }
            byte[] retBuf = new byte[totalLen];
            System.arraycopy( buf, 0, retBuf, 0, totalLen );
            return retBuf;
        }
        else
        {
            int totalLen = 0;
            byte[] buf = new byte[len];
            while ( totalLen < len )
            {
                int readLen = in.read( buf, totalLen, len - totalLen );
                if ( readLen <= 0 )
                    break;
                totalLen = totalLen + readLen;
            }
            return buf;
        }
    }

    private String getRequestData( String serviceId ) throws FileNotFoundException, IOException
    {
        StringBuffer buf = new StringBuffer();
        // buf.append("SID=");
        buf.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NBCBEBankData>" );
        buf.append( "<sessionId>" );
        buf.append( sessionId );
        buf.append( "</sessionId>" );
        // buf.append("serviceId=");
        buf.append( "<serviceId>" );
        buf.append( serviceId );
        buf.append( "</serviceId>" );
        // String reqData = getVirtualRequestMessage( serviceId );
        String reqData = "<opReq><serialNo>69501451974936029</serialNo><reqTime>20160105142216</reqTime><ReqParam><userID>0000005332</userID><userPWD>123456</userPWD></ReqParam></opReq>";
        String signData = null;
        String customerId = null;
        if ( serviceId.equals( "srv001_signOn" ) )
        {
            try
            {
                if ( serviceId.equals( "srv001_signOn" ) )
                {
                    customerId = getNodeValue( reqData, "userID" );
                }
                signData = signData( reqData );
                signData = getNodeValue( signData, "signed_data" );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        buf.append( "<customerId>" );
        buf.append( customerId );
        buf.append( "</customerId>" );
        buf.append( "<softwareId>" );
        buf.append( "002" );
        buf.append( "</softwareId>" );
        buf.append( "<functionId>" );
        buf.append( serviceId.substring( 0, 6 ) );
        buf.append( "</functionId>" );
        buf.append( "<functionName>" );
        buf.append( "测试功能测试功能测试" );
        buf.append( "</functionName>" );
        buf.append( reqData + "<signData>" + signData + "</signData></NBCBEBankData>" );
        return buf.toString();
    }

    public static String getNodeValue( String returnStr, String tagName )
    {
        int startIdx, endIdx;
        String beginTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        startIdx = returnStr.indexOf( beginTag ) + beginTag.length();
        endIdx = returnStr.indexOf( endTag, startIdx );
        return returnStr.substring( startIdx, endIdx );
    }

    /**
     * 数据加密
     *
     * @param orignalData
     * @return
     */
    public String signData( String orignalData )
    {
        // orignalData = "nbcb";
        InetAddress addr = null;
        Socket socket = null;
        String verifyheader = "<?xml version=\"1.0\" encoding=\"gbk\"?>\n" + "<msg>\n" + "<msg_head>\n"
                + "<msg_type>0</msg_type>\n" + "<msg_id>1005</msg_id>\n" + "<msg_sn>0</msg_sn>\n"
                + "<version>1</version>\n" + "</msg_head>\n" + "<msg_body>\n" + "<origin_data_len>"
                + orignalData.getBytes().length + "</origin_data_len>\n" + "<origin_data>" + orignalData
                + "</origin_data>\n" + "</msg_body>\n" + "</msg>";
        // System.out.println("verifyheader="+verifyheader);
        try
        {
            String verifyIp = "12.99.147.153";
            int verifyPort = 8010;
            // System.out.println( "发送的原文包：" + orignalData );
            addr = InetAddress.getByName( verifyIp );
            socket = new Socket( addr, verifyPort );
            BufferedWriter wr = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
            System.out.println( "【数据加密】加密的明文：" + verifyheader );
            wr.write( verifyheader );
            wr.flush();
            // 接收
            BufferedReader rd = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ( (line = rd.readLine()) != null )
            {
                sb.append( line );
            }
            // 处理返回
            String returnStr = sb.toString();
            System.out.println( "【数据加密】返回的密文：" + returnStr );
            wr.close();
            rd.close();
            socket.close();
            return returnStr;
        }
        catch ( Exception e )
        {
            return "";
        }
    }

    public static void main( String[] arg ) throws Exception
    {
        try
        {
            LogonTest client = new LogonTest();
            client.setHostAddr( "http://12.99.147.153:7071" );
            client.setReqURI( "/directlink/httpAccess" );
            //
            System.out.println( "Execute srv001_signOn:" );
            client.establishSession();
            System.out.println( "Execute srv001_signOn finished!\r\n" );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}