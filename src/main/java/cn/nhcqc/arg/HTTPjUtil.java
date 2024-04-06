package cn.nhcqc.arg;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.Charset;
import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.lang3.stream.Streams;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

/**
 * Java 内置 HTTP 相关例程库
 * @author 陈庆灿
 */
public class HTTPjUtil {

    //------------------------------------------------------------------------
    public static final int
        STATUS_OK               = 200,
        STATUS_MOVED_PERMANENTLY= 301,
        STATUS_FOUND            = 302,
        STATUS_BAD_REQUEST      = 400,
        STATUS_FORBIDDEN        = 403,
        STATUS_NOT_FOUND        = 404,
        STATUS_SERVER_ERROR     = 500;

    public static final String
        CONTENT_TYPE_JSON_UTF8  = "application/json; charset=UTF-8",
        CONTENT_TYPE_OCTET      = "application/octet-stream";

    //------------------------------------------------------------------------
    public static final Gson GSON = new Gson();

    //------------------------------------------------------------------------
    /**
     * JSON 字串转为 Map，如输入空白则返回空集。
     */
    public static Map <String, Object> mapJSON (final String json) {
        if (json == null || json.isBlank ()) return new HashMap <String, Object> (0);
        @SuppressWarnings ("unchecked")
        Map <String, Object> map = GSON.fromJson (json, Map.class);
        return map;
    }

    //------------------------------------------------------------------------
    /**
     * HTTP 返回二进制内容
     */
    public static HttpExchange resHTTP (
    final HttpExchange http,
    final int status,
    final String content_type,
    final byte[] body)
    throws IOException {

        http.getResponseHeaders ().add ("Content-Type", content_type);
        http.sendResponseHeaders (status, /* chunked transfer encoding */ 0);
        try (OutputStream res = http.getResponseBody()) {
            res.write (body);
        }
        return http;
    }

    /**
     * HTTP 返回文字内容
     */
    public static HttpExchange resHTTP (
    final HttpExchange http,
    final int status,
    final String content_type,
    final String body,
    final Charset cs)
    throws IOException {

        String type = content_type.contains ("charset=") ?
            content_type :
            content_type + ";charset=" + cs.name ();
        http.getResponseHeaders ().add ("Content-Type", type);
        http.sendResponseHeaders (status, /* chunked transfer encoding */ 0);
        try (OutputStream res = http.getResponseBody()) {
            res.write (body.getBytes (cs));
        }
        return http;
    }

    /**
     * HTTP 返回文字内容、字符集为 UTF-8.
     */
    public static HttpExchange resHTTP (
    final HttpExchange http,
    final int status,
    final String content_type,
    final String body)
    throws IOException {
        return resHTTP (http, status, content_type, body, UTF_8);
    }

    /**
     * HTTP 返回纯文本
     */
    public static HttpExchange resHTTPplain (
    final HttpExchange http,
    final int status,
    final String body,
    final Charset cs)
    throws IOException {
        return resHTTP (http, status, "text/plain", body, cs);
    }

    /**
     * HTTP 返回纯文本、字符集为 UTF-8.
     */
    public static HttpExchange resHTTPplain (
    final HttpExchange http,
    final int status,
    final String body)
    throws IOException {
        return resHTTPplain (http, status, body, UTF_8);
    }

    /**
     * 转为 JSON 后 HTTP、字符集为 UTF-8.
     */
    public static HttpExchange resHTTPtoJSON (
    final HttpExchange http,
    final Object resObj)
    throws IOException {
        return resHTTP (http, 200, CONTENT_TYPE_JSON_UTF8, GSON.toJson (resObj));
    }

    //------------------------------------------------------------------------
    public static String toString (final HttpRequest req, final String body) {
        return toString (req, body, 0);
    }

    public static String toString (final HttpRequest req, final String body, int limitBodyDump) {
        if (limitBodyDump <= 0) limitBodyDump = Integer.MAX_VALUE;
        var s = new StringBuilder ();
        s.append (req.method ()).append (' ').append (req.uri ()).append ("\r\n");

        req.headers ().map ().forEach ((k, vl) -> {
            vl.forEach (v1 -> s.append (k).append (": ").append (v1).append ("\r\n"));
        });

        s.append ("\r\n").append (left (body, limitBodyDump));
        return s.toString ();
    }

    //------------------------------------------------------------------------
    public static <T> String toString (final HttpResponse<T> res, final String body) {
        return toString (res, body, 0);
    }

    public static <T> String toString (final HttpResponse<T> res, final String body, int limitBodyDump) {
        if (limitBodyDump <= 0) limitBodyDump = Integer.MAX_VALUE;
        return new StringBuilder ()
            .append (res.statusCode ()).append ("\r\n")
            .append (left (body, limitBodyDump))
            .toString ();
    }

    public static String toString (final HttpResponse<String> res, final int limitBodyDump) {
        return new StringBuilder ()
            .append (res.statusCode ()).append ("\r\n")
            .append (left (res.body (), limitBodyDump))
            .toString ();
    }

    //------------------------------------------------------------------------
    /** 返回左侧最多 len 个字符。如入参字串为空指针，返回空指针。 */
    public static String left (final String s, final int len) {
        return s == null ?
            null :
            s.substring (0, Math.min (s.length (), len));
    }

    //------------------------------------------------------------------------
    /** 本机 IPv4 地址列表，不含 "127.0.0.1". */
    public static List<String> getLocalIPv4()
    throws UnknownHostException, SocketException {
        var ipv4 = new LinkedList<String>();
        Streams.of (NetworkInterface.getNetworkInterfaces()).forEach (interface1 -> {
            Streams.of (interface1.getInetAddresses ())
                .filter  (inet1 -> inet1 instanceof Inet4Address)
                .map     (inet1 -> inet1.getHostAddress())
                .filter  (addr1 -> ! "127.0.0.1".equals (addr1))
                .forEach (addr1 -> ipv4.add(addr1));
        });
        return ipv4;
    }

}
