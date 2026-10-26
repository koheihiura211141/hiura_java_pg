import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定する機能を読み込みます
import java.net.URLDecoder; // 追加：%から始まるURLエンコード文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // 追加：文字コードにUTF-8を指定する機能を読み込みます
import java.util.ArrayList; // 追加：Todoを入れるArrayListを使えるようにします
import java.util.List; // 追加：Todoの一覧をListとして扱えるようにします

public class App { // Appという名前のプログラムを定義します
    public static void main(String[] args) throws Exception { // プログラムの開始地点を定義します
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでWebサーバーを用意します
        List<String> todos = new ArrayList<>(); // 追加：Todoを保存する空の一覧を一度だけ作ります
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理を定義します
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String method = exchange.getRequestMethod(); // 追加：GETかPOSTかを取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 追加：通常のパスはUTF-8のテキストとして返します
            // パスを比べて、返す文字を振り分けます
            if (path.equals("/add") && method.equals("POST")) { // 追加：Todo追加のPOSTを受け取ります
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 追加：送られた内容をUTF-8で受け取ります
                String value = body.substring(5); // 追加：「todo=」の5文字を除きます
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8); // 追加：日本語を元に戻します
                if (!todo.isEmpty()) { // 追加：空の入力ではない場合だけ追加します
                    System.out.println("todo = " + todo);
                    todos.add(todo); // 追加：Todoを一覧に足します
                } // 追加：空の入力かどうかの確認はここまでです
                exchange.getResponseHeaders().set("Location", "/"); // 追加：追加後の戻り先を「/」にします
                exchange.sendResponseHeaders(303, -1); // 追加：「/」へ移動する応答を送ります
                exchange.close(); // 追加：今回の通信を閉じます
                return; // 追加：この分岐を抜けます
            } else if (path.equals("/")) { // 追加：「/」にフォームとTodo一覧を表示します
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 追加：入力フォームと箇条書きのHTMLを作り始めます
                for (String todo : todos) { // 移動：Todoを1件ずつ順番に取り出します
                    html += "<li>" + todo + "</li>"; // 移動：取り出したTodoをHTMLに足します
                } // 移動：Todoを順番に取り出す処理はここまでです
                html += "</ul>"; // 移動：箇条書きのHTMLを閉じます
                message = html; // 移動：組み立てたHTMLを返す内容にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 移動：「/」をUTF-8のHTMLとして返します
            } else if (path.equals("/hello")) {
                String query = exchange.getRequestURI().getRawQuery(); // 追加：URLのクエリを取り出します
                System.out.println("query = " + query);
                String encodedName = query == null ? null : query.substring(5); // 変更：デコード前の名前を取り出します
                String name = encodedName == null ? "ゲスト" : URLDecoder.decode(encodedName, StandardCharsets.UTF_8); // 追加：%形式を日本語に戻します
                message = "こんにちは、" + name + "さん！"; // 変更：切り出した名前を応答に混ぜます
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
/*
 * メインは起動時一度だけ動き、ハンドラ（createContext に登録した { } の中）はリクエストが来るたび何度でも動く
 * 足すtodos.add(todo);はリクエストのたびにやるから中にあるのが正しい
 * 新品の空のリストを作るList<String> todos = new
 * ArrayList<>();はリクエストのたびに空のリストを作ってしまうのでcreateContextの前に置く（メイン）
 */
