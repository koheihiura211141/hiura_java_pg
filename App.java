import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定する機能を読み込みます

public class App { // Appという名前のプログラムを定義します
    public static void main(String[] args) throws Exception { // プログラムの開始地点を定義します
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでWebサーバーを用意します
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理を定義します
            String message = "こんにちは"; // 返す文字をmessageに入れます
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // UTF-8の文字として返すことを伝えます
            byte[] body = message.getBytes("UTF-8"); // 文字を送信できるデータに変換します
            exchange.sendResponseHeaders(200, body.length); // 正常終了を示す200とデータの長さを送ります
            exchange.getResponseBody().write(body); // ブラウザへデータを送ります
            exchange.getResponseBody().close(); // データの送信を終了します
        }); // 「/」への処理の定義を終了します
        server.start(); // Webサーバーの待ち受けを開始します
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことをターミナルに表示します
    } // mainの定義を終了します
} // Appの定義を終了します