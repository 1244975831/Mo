package mo.zucc.edu.cn.face.NetWork;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2018/3/22.
 */

public class HttpUrlConnection {

    public static String readParse(String urlPath) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        InputStream inStream = conn.getInputStream();
        while ((len = inStream.read(data)) != -1) {
            outStream.write(data, 0, len);
        }
        inStream.close();
        return new String(outStream.toByteArray());//通过out.Stream.toByteArray获取到写的数据
    }
}
