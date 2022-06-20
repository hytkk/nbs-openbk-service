//
//Copyright (C) 2005 ECC CORPORATION
//
//ALL RIGHTS RESERVED BY ECC CORPORATION, THIS PROGRAM
//MUST BE USED SOLELY FOR THE PURPOSE FOR WHICH IT WAS
//FURNISHED BY ECC CORPORATION, NO PART OF THIS PROGRAM
//MAY BE REPRODUCED OR DISCLOSED TO OTHERS, IN ANY FORM
//WITHOUT THE PRIOR WRITTEN PERMISSION OF ECC CORPORATION.
//USE OF COPYRIGHT NOTICE DOES NOT EVIDENCE PUBLICATION
//OF THE PROGRAM
//
//       ECC CONFIDENTIAL AND PROPRIETARY
//
////////////////////////////////////////////////////////////////////////////
//
//$Log: SignOpStep.java,v $
//Revision 1.3  2008/09/05 03:02:48  zhougy
//*** empty log message ***
//
//Revision 1.2  2007/11/30 06:07:24  zhougy
//修改示例程序
//


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import sun.misc.BASE64Decoder;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;

public class SignOpStep  {
    String verifyValue=null;


    SignOpStep(String verifyValue) {

        this.verifyValue=verifyValue;
        // TODO Auto-generated constructor stub
    }

    public String sign(){
        // TODO Auto-generated method stub
        InetAddress addr = null;
        Socket socket = null;
        String verifyheader="<?xml version=\"1.0\" encoding=\"gbk\"?>\n"
                +"<msg>\n"
                +"<msg_head>\n"
                +"<msg_type>0</msg_type>\n"
                +"<msg_id>1005</msg_id>\n"
                +"<msg_sn>0</msg_sn>\n"
                +"<version>1</version>\n"
                +"</msg_head>\n"
                +"<msg_body>\n"
                +"<origin_data_len>"+this.verifyValue.getBytes().length+"</origin_data_len>\n"
                +"<origin_data>"+this.verifyValue+"</origin_data>\n"
                +"</msg_body>\n"
                +"</msg>";
        try {

            String verifyIp ="127.0.0.1";
            int verifyPort =8010;
            //verifyValue=getSendStr(verifyValue);
            //System.out.println("发送的原文包："+verifyValue);
            addr = InetAddress.getByName(verifyIp);
            socket = new Socket(addr, verifyPort);
            BufferedWriter wr =
                    new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()));
            wr.write(verifyheader);
            wr.flush();
            //接收
            BufferedReader rd =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                // System.out.println(line);
                sb.append(line);
            }
            //处理返回
            String returnStr = sb.toString();
            System.out.println("返回的密文："+returnStr);


            wr.close();
            rd.close();
            socket.close();
            return returnStr;
        }catch(Exception e){
            return "";
        }
    }




    public static String getNodeValue(String returnStr, String tagName) {
        int startIdx, endIdx;
        String beginTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        startIdx = returnStr.indexOf(beginTag) + beginTag.length();
        endIdx = returnStr.indexOf(endTag, startIdx);
        return returnStr.substring(startIdx, endIdx);
    }


    public static byte[] base64Decoder(String bytes)
    {
        if(bytes==null||bytes.length()<1)
            return "".getBytes();
        byte[] base64Str=null;
        try
        {
            base64Str=new BASE64Decoder().decodeBuffer(bytes);
        }
        catch(Exception e)
        {
            return "".getBytes();
        }
        return base64Str;
    }
    /**
     * <b>功能描述: </b><br>
     * <p></p>
     *
     * @param args
     *
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        // String otest=args[0];
        String otest="要签名的数据";
        SignOpStep ss=new SignOpStep(otest);
        PKCS7 p7 ;
        ContentInfo info;
        try {
            String returnStr=ss.sign();
            String miwen=getNodeValue(returnStr,"signed_data");
            System.out.println("返回的密文："+miwen);
            System.out.println("返回的密文长度："+miwen.length());

            p7 = new PKCS7(SignOpStep.base64Decoder(miwen));
            info = p7.getContentInfo();

            String minwen=new String(info.getContentBytes(),"gbk");
            System.out.println("返回的明文："+minwen);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
