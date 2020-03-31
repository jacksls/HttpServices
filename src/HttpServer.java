import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Liang
 * @date 2020/3/31 11:28
 */
public class HttpServer {

    private static ServerSocket serverSocket;
    private static Socket accept;

    public static void main(String[] args) {
        serverStart();
    }

    //服务器启动
    public static void serverStart() {
        try {
            serverSocket = new ServerSocket(8888);
            System.out.println("服务器启动成功,等待8888端口的请求...");
            //获取消息
            while (true) {
                httpMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败...");
        }
    }

    //获取Http协议信息
    public static void httpMessage() {
        try {
            accept = serverSocket.accept();
            InputStream inputStream = accept.getInputStream();
            byte[] bytes = new byte[1024 * 1024];
            int len = inputStream.read(bytes);
            String message = new String(bytes, 0, len);
            System.out.println(message);
            requestHandle(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理协议信息
    public static void requestHandle(String message) {
        if (message == null || ((message = message.trim()).equals(""))) {
            return;
        }
        String url = "";
        String firstLine = message.substring(message.indexOf("/"), message.indexOf("HTTP/1.1"));
        //获得url
        url = firstLine.trim();
        System.out.println(url);
        File file = null;
        int count = 0;
        char[] chars = url.toCharArray();
        for (int i = 0; i < url.length(); i++) {
            if (chars[i] == '/') {
                count++;
            }
        }
        file = new File(url);
        if (file.exists()) {
            System.out.println("目录已经存在...先删除再创建");
            file.delete();
        }
        if (count == 1) {
            boolean mkdir = file.mkdir();
            if (mkdir){
                System.out.println("创建成功...");
            }
        } else {
            boolean mkdir = file.mkdirs();
            if (mkdir){
                System.out.println("创建成功...");
            }
        }
        String rs = execCmd(url);
        response(rs);
    }

    //执行命令返回消息
    public static String execCmd(String url) {
        StringBuilder rs = new StringBuilder();

        Runtime rt = null;
        Process exec = null;
        BufferedReader bufferedReader = null;
        String ex = "stat " + url;
        try {
            rt = Runtime.getRuntime();
            //执行命令
            exec = rt.exec(ex);
            //阻塞
            try {
                exec.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //获取输出结果
            bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream(), "UTF8"));
            //读取
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                rs.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                //销毁线程
                if (exec != null) {
                    exec.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rs.toString();
    }

    public static void response(String rs) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(accept.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rs2 = "http/1.1 200 ok\n"
                + "\n\n"
                + rs;
        out.println(rs2);
        out.close();
    }
}
