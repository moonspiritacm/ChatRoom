/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ServerThread implements Runnable {

    private ArrayList<Client> clientList;  //线程共享 客户端列表
    private ArrayList<String> msgList;  //线程共享 消息列表

    private Client client;  //客户端
    private boolean flag;
    private boolean alreadLogin;
    private Socket clientSocket;  //套接字
    private BufferedReader socketIn;  //socket输入流 上行
    private PrintWriter socketOut;  //socket输出流 下行
    private String msgIn;  //待处理消息
    private StringTokenizer msg;  //单词化处理后消息

    /**
     * 构造 初始化
     *
     * @param s 套接字 Type：Socket
     * @param c 客户端列表 Type：ArrayList
     * @param m 消息列表 Type：ArrayList
     * @author fanye
     * @version 2016-7-20
     */
    public ServerThread(Socket s, ArrayList<Client> c, ArrayList<String> m) {
        try {
            this.clientList = c;
            this.msgList = m;
            this.clientSocket = s;
            socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            socketOut = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            flag = true;
            alreadLogin = false;
        } catch (Exception err) {
            //err.printStackTrace();
            System.err.println("ERROR: 线程初始化故障");
        }
        //System.out.println("Debug:线程已启动");
    }

    /**
     * 判断字符串是否全为数字
     *
     * @param s 字符串 Type：String
     * @return true 全是数字 false 不全是数字
     * @author fanye
     * @version 2016-7-20
     */
    public boolean isDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 发送消息 向客户端发送消息
     *
     * @param s 消息内容 Type：String
     * @author fanye
     * @version 2016-7-20
     */
    public void sendMSG(String s) {
        socketOut.println(s);
        socketOut.flush();
    }

    /**
     * 打印历史消息 向客户端打印历史消息 存在同步处理
     *
     * @param s 开始位置（从1开始） Type：int
     * @param e 结束位置（从1开始） Type：int
     * @author fanye
     * @version 2016-7-20
     */
    public void printMSG(int s, int e) {
        //处理越下界的情况
        if (s < 1) {
            s = 1;
        }
        //遍历打印
        for (int i = s; i <= e; i++) {
            synchronized (this) {
                sendMSG(i + ": " + msgList.get(i - 1));
            }
        }
    }

    /**
     * 判断用户名是否重复 存在同步处理
     *
     * @param n 用户名 Type：String
     * @return true 重复 false 无重复
     * @author fanye
     * @version 2016-7-20
     */
    public synchronized boolean isRepeatName(String n) {
        for (int i = 0; i < clientList.size(); i++) {
            if (clientList.get(i).getName().equals(n)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 广播消息 存在同步处理
     *
     * @param m 消息内容 Type：String
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public synchronized void callAll(String m) throws Exception {
        for (int i = 0; i < clientList.size(); i++) {
            Client temp = clientList.get(i);
            if (!temp.getSocket().equals(clientSocket)) {
                PrintWriter outTemp = new PrintWriter(temp.getSocket().getOutputStream());
                outTemp.println(m);
                msgList.add("(" + client.getName() + " say to " + temp.getName() + ") " + m);
                outTemp.flush();
            }
        }
    }

    /**
     * 组播消息 存在同步处理
     *
     * @param m 消息内容 Type：String
     * @param n 排除的用户名 Type：String
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public synchronized void callOther(String m, String n) throws Exception {
        for (int i = 0; i < clientList.size(); i++) {
            Client temp = clientList.get(i);
            if (!(temp.getSocket().equals(clientSocket) || temp.getName().equals(n))) {
                PrintWriter outTemp = new PrintWriter(temp.getSocket().getOutputStream());
                outTemp.println(m);
                msgList.add("(" + client.getName() + " say to " + temp.getName() + ") " + m);
                outTemp.flush();
            }
        }
    }

    /**
     * 单播消息 存在同步处理
     *
     * @param m 消息内容 Type：String
     * @param n 发送对象用户名 Type：String
     * @return true 成功 false 失败
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public synchronized boolean callSimple(String m, String n) throws Exception {
        for (int i = 0; i < clientList.size(); i++) {
            Client temp = clientList.get(i);
            if (temp.getName().equals(n)) {
                PrintWriter outTemp = new PrintWriter(temp.getSocket().getOutputStream());
                outTemp.println(m);
                msgList.add("(" + client.getName() + " say to " + temp.getName() + ") " + m);
                outTemp.flush();
                return true;
            }
        }
        return false;
    }

    /**
     * login
     *
     * @return true 成功 false 失败
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public boolean login() throws Exception {
        msgIn = socketIn.readLine().trim();
        msg = new StringTokenizer(msgIn);
        while (msg.hasMoreTokens() && flag) {
            if (msgIn.equals("/quit")) {
                close();
                return false;
            } else if (msg.nextToken().equals("/login") && msg.hasMoreTokens()) {
                String name = msg.nextToken();
                if (isRepeatName(name)) {
                    sendMSG("Name exist, please choose anthoer name.");
                } else {
                    client = new Client(name, clientSocket);
                    synchronized (this) {
                        clientList.add(client);
                    }
                    alreadLogin = true;
                    callAll(name + " has logined");
                    sendMSG("Welcome " + name);
                    System.out.println(name + " has logined");
                    synchronized (this) {
                        System.out.println("Total online user: " + clientList.size());
                    }
                    msgIn = socketIn.readLine().trim();
                    msg = new StringTokenizer(msgIn);
                    return true;
                }
            } else {
                sendMSG("Invalid command");
            }
            msgIn = socketIn.readLine().trim();
            msg = new StringTokenizer(msgIn);
        }
        return false;
    }

    /**
     * hi
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void hi() throws Exception {
        if (msg.hasMoreTokens()) {
            String name = msg.nextToken();
            if (client.getName().equals(name)) {
                sendMSG("Stop talking to yourself!");
            } else if (callSimple(client.getName() + "向你打招呼：“Hi，你好啊~”", name)) {
                callOther(client.getName() + "向" + name + "打招呼：“Hi，你好啊~”", name);
                sendMSG("你向" + name + "打招呼：“Hi，你好啊~”");
            } else {
                sendMSG(name + " is not online");
            }
        } else {
            callAll(client.getName() + "向大家打招呼，“Hi，大家好！我来咯~”");
            sendMSG("你向大家打招呼：“Hi，你好啊~”");
        }
        msgIn = socketIn.readLine().trim();
        msg = new StringTokenizer(msgIn);
    }

    /**
     * to
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void to() throws Exception {
        if (msg.hasMoreTokens()) {
            String name = msg.nextToken();
            if (msg.hasMoreTokens()) {
                String temp = msgIn.substring(msgIn.indexOf(name) + name.length()).trim();
                if (client.getName().equals(name)) {
                    sendMSG("Stop talking to yourself!");
                } else if (!callSimple(client.getName() + "对你说：" + temp, name)) {
                    sendMSG(name + " is not online");
                } else {
                    sendMSG("你对" + name + "说：" + temp);
                }
            } else {
                sendMSG("Empty message");
            }
        } else {
            sendMSG("Invalid command");
        }
        msgIn = socketIn.readLine().trim();
        msg = new StringTokenizer(msgIn);
    }

    /**
     * to 存在同步问题
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public synchronized void who() throws Exception {
        for (int i = 0; i < clientList.size(); i++) {
            String temp = clientList.get(i).getName();
            sendMSG(temp);
        }
        sendMSG("Total online user: " + clientList.size());
        msgIn = socketIn.readLine().trim();
        msg = new StringTokenizer(msgIn);
    }

    /**
     * history 存在同步问题
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void history() throws Exception {
        int startIndex = 1;  //起始位置
        int count = 1;  //计数
        int f = 0;  //flag 0 1 2
        //处理命令
        if (msg.hasMoreTokens()) {
            String temp = msg.nextToken();
            if (isDigit(temp)) {
                startIndex = Integer.parseInt(temp);
                f++;
            } else {
                f = -99;
            }
            if (msg.hasMoreTokens()) {
                String t = msg.nextToken();
                if (isDigit(t)) {
                    count = Integer.parseInt(t);
                    f++;
                } else {
                    f = -99;
                }
            }
        }
        //System.out.println("startIndex:" + startIndex);
        //System.out.println("count:" + count);
        synchronized (this) {
            if (count < 1 || startIndex < 1 || startIndex > msgList.size() || startIndex - count + 1 < 1) {
                sendMSG("wrong param");
            } else {
                switch (f) {
                    case 0:  //默认后50条消息
                        printMSG(msgList.size() - 49, msgList.size());
                        break;
                    case 1:  //参数错误
                        sendMSG("wrong param");
                        break;
                    case 2:  //全部指定
                        printMSG(startIndex - count + 1, startIndex);
                        break;
                    default:
                        sendMSG("Invalid command");
                }
            }
        }
        msgIn = socketIn.readLine().trim();
        msg = new StringTokenizer(msgIn);
    }

    /**
     * smile
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void smile() throws Exception {
        callAll(client.getName() + "脸上泛起无邪的笑容");
        msgIn = socketIn.readLine().trim();
        msg = new StringTokenizer(msgIn);
    }

    /**
     * defaulted 错误指令或广播消息
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void defaulted() throws Exception {
        if (msgIn.startsWith("/")) {
            sendMSG("Invalid command");
        } else {
            callAll(client.getName() + "说：" + msgIn);
            sendMSG("你说" + msgIn);
        }
        msgIn = socketIn.readLine().trim();
        msg = new StringTokenizer(msgIn);
    }

    /**
     * 登陆后退出 存在同步问题
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void quit() throws Exception {
        sendMSG("/close");
        callAll(client.getName() + " has quit.");
        System.out.println(client.getName() + " has quitted");
        socketIn.close();
        socketOut.close();
        clientSocket.close();
        synchronized (this) {
            clientList.remove(client);
            System.out.println("Total online user: " + clientList.size());
        }
        flag = false;
        alreadLogin = false;
    }

    /**
     * 未登陆退出 存在同步问题
     *
     * @exception Exception
     * @author fanye
     * @version 2016-7-20
     */
    public void close() throws Exception {
        sendMSG("/close");
        socketIn.close();
        socketOut.close();
        clientSocket.close();
        flag = false;
        alreadLogin = false;
    }

    public void kick() throws Exception {
        if (msg.hasMoreTokens()) {
            if (client.getName().equals("admin")) {
                String temp = msg.nextToken();
                callOther(temp + " was kicked by admin", temp);
                callSimple("you was kicked by admin", temp);
                callSimple("/close", temp);
                for (int i = 0; i < clientList.size(); i++) {
                    if (clientList.get(i).getName().equals(temp)) {
                        clientList.remove(clientList.get(i));
                        break;
                    }
                }
            } else {
                sendMSG("Invalid command");
            }
        }
    }

    @Override
    public void run() {
        try {
            login();
            while (msg.hasMoreTokens() && flag) {
                synchronized (this) {
                    msgList.add("(" + client.getName() + " say to server ) " + msgIn);
                }
                switch (msg.nextToken()) {
                    case "//hi":
                        hi();
                        break;
                    case "/to":
                        to();
                        break;
                    case "/who":
                        who();
                        break;
                    case "/history":
                        history();
                        break;
                    case "//smile":
                        smile();
                        break;
                    case "/quit":
                        quit();
                        break;
                    case "/kick":
                        kick();
                        break;
                    default:
                        defaulted();
                }
            }
        } catch (Exception err) {
            //err.printStackTrace();
            //System.err.println("ERROR:线程运行故障");
        } finally {
            if (alreadLogin) {
                try {
                    callAll(client.getName() + " has quit.");
                } catch (Exception err) {
                }
                System.out.println(client.getName() + " has quitted unnormlly");
                synchronized (this) {
                    clientList.remove(client);
                    System.out.println("Total online user: " + clientList.size());
                }
            }
        }
        //System.out.println("Debug: " + Thread.currentThread().getName() + "  finish");
    }
}
