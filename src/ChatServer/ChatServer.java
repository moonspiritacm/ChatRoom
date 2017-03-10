/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 服务器 主类
 *
 * @author fanye
 * @version 2016-7-20
 */
public class ChatServer {

    private static final int PORT = 12345;  //全局变量 服务器监听端口

    private ServerSocket serverSocket;  //服务器监听套接字
    private ArrayList<Client> clientList;  //线程共享 客户端列表
    private ArrayList<String> msgList;  //线程共享 消息列表

    /**
     * 服务器 启动 初始化
     *
     * @return true 启动成功 false 启动失败
     * @author fanye
     * @version 2016-7-20
     */
    public boolean start() {
        try {
            serverSocket = new ServerSocket(PORT);
            clientList = new ArrayList<>();
            msgList = new ArrayList<>();
            System.out.println("服务器启动成功");
            return true;
        } catch (Exception err) {
            //err.printStackTrace();
            System.err.println("ERROR: 服务器启动故障");
            return false;
        }
    }

    /**
     * 服务器 运行
     *
     * @author fanye
     * @version 2016-7-20
     */
    public void fun() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                ServerThread th = new ServerThread(socket, clientList, msgList);
                new Thread(th).start();
                //System.out.println("创建连接成功");
            }
        } catch (Exception err) {
            //err.printStackTrace();
            System.err.println("ERROR:ChatServer 创建连接故障");
        }
    }

    public static void main(String[] srgs) throws Exception {
        ChatServer server = new ChatServer();
        if (server.start()) {
            server.fun();
        }
    }
}
