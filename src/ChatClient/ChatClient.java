/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 客户端 主类
 *
 * @author fanye
 * @version 2016-7-20
 */
public class ChatClient {

    private static final String IP = "127.0.0.1";  //全局变量 服务器IP地址
    private static final int PORT = 12345;  //全局变量 服务器监听端口 

    private Socket socket;  //套接字
    private String sendMessage;  //待发送消息
    private String receiveMessage;  //已接收消息
    private BufferedReader socketIn;  //socket输入流 下行
    private PrintWriter socketOut;  //socket输出流 上行
    private BufferedReader sysIn;  //标准输入流 从本地键盘输入

    /**
     * 下行接收 线程类 ： 接收来自服务器的消息并输出到屏幕
     *
     * @author fanye
     * @version 2016-7-20
     */
    class ReceiveThread extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    receiveMessage = socketIn.readLine().trim();
                    if (!receiveMessage.equals("/close")) {
                        System.out.println(receiveMessage);
                    } else {
                        System.out.println("收到关闭指令");
                        break;
                    }
                }
            } catch (Exception err) {
                //err.printStackTrace();
                System.err.println("ERROR:服务器故障，连接中断");
                System.exit(0);
            }
        }
    }

    /**
     * 客户端 启动 初始化变量
     *
     * @return true 启动成功 false 启动失败
     * @author fanye
     * @version 2016-7-20
     */
    public boolean start() {
        try {
            socket = new Socket(IP, PORT);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            sysIn = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            sendMessage = "xxx";
            receiveMessage = "xxx";
            System.out.println("客户端已启动");
            System.out.println("请登录/login user_name");
            return true;
        } catch (ConnectException err) {
            //err.printStackTrace();
            System.err.println("ERROR:服务器IP地址不正确");
            return false;
        } catch (IOException err) {
            //err.printStackTrace();
            System.err.println("ERROR:服务器故障，无法建立连接");
            return false;
        }
    }

    /**
     * 客户端 运行
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void fun() throws Exception {
        new ReceiveThread().start();
        while (!receiveMessage.equals("/close")) {
            if (sysIn.ready()) {
                sendMessage = sysIn.readLine().trim();
                socketOut.println(sendMessage);
                socketOut.flush();
                //System.out.println("Debug:Client.fun() client send：" + sendMessage);
            }
        }
        System.out.println("收到关闭指令");
    }

    /**
     * 客户端 关闭
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void close() throws Exception {
        //System.out.println("exit");
        socketIn.close();
        socketOut.close();
        socket.close();
        System.out.println("客户端已关闭");
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        if (client.start()) {
            try {
                client.fun();
                client.close();
            } catch (Exception err) {
                //err.printStackTrace();
                System.err.println("ERROR:Client 服务器故障，连接中断");
            }
        }
    }
}
