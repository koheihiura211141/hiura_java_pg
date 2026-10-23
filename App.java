import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定する機能を読み込みます
import java.net.URLDecoder; // 追加：%から始まるURLエンコード文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // 追加：文字コードにUTF-8を指定する機能を読み込みます
import java.util.ArrayList; // 追加：Todoを入れるArrayListを使えるようにします
import java.util.List; // 追加：Todoの一覧をListとして扱えるようにします

public class App { // Appという名前のプログラムを定義します
    public static void main(String[] args) throws Exception { // プログラムの開始地点を定義します
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでWebサーバーを用意します
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理を定義します
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 追加：通常のパスはUTF-8のテキストとして返します
            // パスを比べて、返す文字を振り分けます
            if (path.equals("/hello")) {
                String query = exchange.getRequestURI().getRawQuery(); // 追加：URLのクエリを取り出します
                System.out.println("query = " + query);
                String encodedName = query == null ? null : query.substring(5); // 変更：デコード前の名前を取り出します
                String name = encodedName == null ? "ゲスト" : URLDecoder.decode(encodedName, StandardCharsets.UTF_8); // 追加：%形式を日本語に戻します
                message = "こんにちは、" + name + "さん！"; // 変更：切り出した名前を応答に混ぜます
            } else if (path.equals("/todos")) { // 追加：/todosへのアクセスを振り分けます
                List<String> todos = new ArrayList<>(); // 追加：Todoの文字列を入れる一覧を作ります
                todos.add("今日の自分のtodo");
                todos.add("牛乳を買う"); // 追加：1件目のTodoを入れます
                todos.add("卵を買う"); // 追加：2件目のTodoを入れます
                todos.add("パンを買う"); // 追加：3件目のTodoを入れます
                String html = "<ul>"; // 追加：箇条書きのHTMLを作り始めます
                for (String todo : todos) { // 追加：Todoを1件ずつ順番に取り出します
                    html += "<li>" + todo + "</li>"; // 追加：取り出したTodoをHTMLに足します
                } // 追加：Todoを順番に取り出す処理はここまでです
                html += "</ul>"; // 追加：箇条書きのHTMLを閉じます
                message = html; // 追加：組み立てたHTMLを返す内容にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 追加：/todosだけUTF-8のHTMLとして返します
            } else if (path.equals("/bye")) {
                message = "さようなら！";
            } else if (path.equals("/main")) {
                message = " 腹減った。。。";
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8"); // 文字を送信できるデータに変換します
            exchange.sendResponseHeaders(200, body.length); // 正常終了を示す200とデータの長さを送ります
            exchange.getResponseBody().write(body); // ブラウザへデータを送ります
            exchange.getResponseBody().close(); // データの送信を終了します
        }); // 「/」への処理の定義を終了します
        server.start(); // Webサーバーの待ち受けを開始します
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことをターミナルに表示します
    } // mainの定義を終了します
} // Appの定義を終了します
