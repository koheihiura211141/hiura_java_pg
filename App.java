import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定する機能を読み込みます
import java.net.URLDecoder; // 追加：%から始まるURLエンコード文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // 追加：文字コードにUTF-8を指定する機能を読み込みます
import java.util.ArrayList; // 追加：Todoを入れるArrayListを使えるようにします
import java.util.List; // 追加：Todoの一覧をListとして扱えるようにします

class Todo { // ★変更：Todo（やること）を表すクラス
    private final int id; // ★変更：何番かを保存する
    private final String title; // ★変更：やることを保存する
    private boolean done; // ★変更：終わったかを保存する

    Todo(int id, String title) { // ★変更：番号とやることを受け取るコンストラクタ
        this.id = id;
        this.title = title;
        this.done = false; // ★変更：最初は未完了にする
    }

    int getId() { // ★変更：番号を読み出す
        return id;
    }

    String getTitle() { // ★変更：やることを読み出す
        return title;
    }

    boolean isDone() { // ★変更：終わったかを読み出す
        return done;
    }

    void setDone(boolean done) { // ★変更：終わったかを書き換える
        this.done = done;
    }
}

public class App { // Appという名前のプログラムを定義します
    static List<Todo> todos = new ArrayList<>(); // ★変更：Todoを貯めるリスト（一覧）
    static int nextId = 1; // ★変更：次に振る番号

    public static void main(String[] args) throws Exception { // プログラムの開始地点を定義します
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでWebサーバーを用意します
        todos.add(new Todo(nextId++, "牛乳を買う")); // ★変更：サンプルの1件目を追加する
        Todo egg = new Todo(nextId++, "卵を買う"); // ★変更：サンプルの2件目を作る
        egg.setDone(true); // ★変更：卵を買うTodoを完了にする
        todos.add(egg); // ★変更：サンプルの2件目を追加する
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理を定義します
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String method = exchange.getRequestMethod(); // 追加：GETかPOSTかを取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 追加：通常のパスはUTF-8のテキストとして返します
            // パスを比べて、返す文字を振り分けます
            if (path.equals("/add") && method.equals("POST")) { // 追加：Todo追加のPOSTを受け取ります
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 追加：送られた内容をUTF-8で受け取ります
                String value = body.substring(5); // 追加：「todo=」の5文字を除きます
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // ★変更：フォームの文字をtitleに入れる
                if (!title.isEmpty()) {
                    System.out.println("todo = " + title);
                    todos.add(new Todo(nextId, title)); // ★変更：次の番号でTodoを作って追加する
                    nextId++; // ★変更：次に振る番号を1つ増やす
                } // 追加：空の入力かどうかの確認はここまでです
                exchange.getResponseHeaders().set("Location", "/"); // 追加：追加後の戻り先を「/」にします
                exchange.sendResponseHeaders(303, -1); // 追加：「/」へ移動する応答を送ります
                exchange.close(); // 追加：今回の通信を閉じます
                return; // 追加：この分岐を抜けます
            } else if (path.equals("/done") && method.equals("GET")) { // ★追加：完了のGETを受け取ります
                String query = exchange.getRequestURI().getQuery(); // ★追加：idを受け取ります
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加：idがあるか確認します
                    try { // ★追加：数字に変えられるか試します
                        int id = Integer.parseInt(query.substring(3)); // ★追加：idを数字に変えます
                        for (Todo todo : todos) { // ★追加：Todoを1件ずつ見ます
                            if (todo.getId() == id) { // ★追加：idが一致するか確認します
                                todo.setDone(true); // ★追加：完了にします
                                break; // ★追加：一致した1件で繰り返しを抜けます
                            } // ★追加
                        } // ★追加
                    } catch (NumberFormatException e) { // ★追加：idが数字でない場合を受け止めます
                        // 何もしません ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加：完了後の戻り先を「/」にします
                exchange.sendResponseHeaders(303, -1); // ★追加：「/」へ移動する応答を送ります
                return; // ★追加：この分岐を抜けます
            } else if (path.equals("/delete") && method.equals("GET")) { // ★追加：削除のGETを受け取ります
                String query = exchange.getRequestURI().getQuery(); // ★追加：idを受け取ります
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加：idがあるか確認します
                    try { // ★追加：数字に変えられるか試します
                        int id = Integer.parseInt(query.substring(3)); // ★追加：idを数字に変えます
                        todos.removeIf(todo -> todo.getId() == id); // ★追加：idが一致するTodoを削除します
                    } catch (NumberFormatException e) { // ★追加：idが数字でない場合を受け止めます
                        // 何もしません ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加：削除後の戻り先を「/」にします
                exchange.sendResponseHeaders(303, -1); // ★追加：「/」へ移動する応答を送ります
                return; // ★追加：この分岐を抜けます
            } else if (path.equals("/")) { // 追加：「/」にフォームとTodo一覧を表示します
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 追加：入力フォームと箇条書きのHTMLを作り始めます
                for (Todo todo : todos) { // ★変更：Todoを1件ずつ取り出す
                    String title = todo.getTitle(); // ★変更：表示するTodoのタイトルを用意する
                    if (todo.isDone()) { // ★変更：終わっているTodoだけ取り消し線を付ける
                        title = "<span style='text-decoration: line-through;'>" + title + "</span>"; // ★変更：タイトルを取り消し線付きにする
                    }
                    html += "<li>" + title + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // ★追加：id付きの完了・削除リンクを一覧に足す
                } // 移動：Todoを順番に取り出す処理はここまでです
                html += "</ul>"; // 移動：箇条書きのHTMLを閉じます
                message = html; // 移動：組み立てたHTMLを返す内容にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 移動：「/」をUTF-8のHTMLとして返します
            } else { // ★変更：使わないパスの分岐を削除し、最後を受け皿にする
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
