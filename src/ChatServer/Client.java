/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import java.net.Socket;
import java.util.ArrayList;

/**
 * 服务器 用户类
 *
 * @author fanye
 * @version 2016-7-20
 */
public class Client {

    private String name;  //用户名
    private Socket socket;  //套接字
    private ArrayList<String> msg;  //消息列表

    /**
     * 用户类 构造 初始化
     *
     * @param n 用户名 Type:String
     * @param s 套接字 Type:Socket
     * @author fanye
     * @version 2016-7-20
     */
    public Client(String n, Socket s) {
        this.name = n;
        this.socket = s;
        msg = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ArrayList<String> getMsg() {
        return msg;
    }

    public void setMsg(ArrayList<String> msg) {
        this.msg = msg;
    }
}
