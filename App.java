import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます
import java.io.IOException; // ★変更：HTTP処理中のDBエラーを扱えるようにします
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定する機能を読み込みます
import java.net.URLDecoder; // 追加：%から始まるURLエンコード文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // 追加：文字コードにUTF-8を指定する機能を読み込みます
import java.sql.Connection; // ★変更：SQLiteへの接続を扱います
import java.sql.DriverManager; // ★変更：SQLiteへ接続する機能を読み込みます
import java.sql.PreparedStatement; // ★変更：値を安全にSQLへ渡す機能を読み込みます
import java.sql.ResultSet; // ★変更：SELECTの結果を扱います
import java.sql.SQLException; // ★変更：データベースのエラーを扱います
import java.sql.Statement; // ★変更：表を作るSQLを実行します
import java.util.ArrayList; // ★変更：SELECTしたTodoを入れる一覧に使います
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
    static final String DB_URL = "jdbc:sqlite:todos.db"; // ★変更：SQLiteの保存先を指定します

    static Connection connect() throws SQLException { // ★変更：SQLiteへの接続を作ります
        return DriverManager.getConnection(DB_URL); // ★変更：todos.dbへ接続します
    }

    static void createTable() throws SQLException { // ★変更：Todoを保存する表を用意します
        String sql = "CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"; // ★変更
        try (Connection connection = connect(); Statement statement = connection.createStatement()) { // ★変更
            statement.executeUpdate(sql); // ★変更：表がなければ作ります
        }
    }

    static void addTodo(String title) throws SQLException { // ★変更：TodoをDBへ追加します
        String sql = "INSERT INTO todos (title, done) VALUES (?, 0)"; // ★変更
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) { // ★変更
            statement.setString(1, title); // ★変更：1つ目の?へタイトルを安全に渡します
            statement.executeUpdate(); // ★変更：INSERTを実行します
        }
    }

    static void completeTodo(int id) throws SQLException { // ★変更：指定されたTodoを完了にします
        String sql = "UPDATE todos SET done = 1 WHERE id = ?"; // ★変更
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) { // ★変更
            statement.setInt(1, id); // ★変更：?へidを安全に渡します
            statement.executeUpdate(); // ★変更：UPDATEを実行します
        }
    }

    static void deleteTodo(int id) throws SQLException { // ★変更：指定されたTodoを削除します
        String sql = "DELETE FROM todos WHERE id = ?"; // ★変更
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) { // ★変更
            statement.setInt(1, id); // ★変更：?へidを安全に渡します
            statement.executeUpdate(); // ★変更：DELETEを実行します
        }
    }

    static List<Todo> findAllTodos() throws SQLException { // ★変更：Todo一覧をDBから読み出します
        List<Todo> todos = new ArrayList<>(); // ★変更：読み出したTodoを入れる一覧です
        String sql = "SELECT id, title, done FROM todos ORDER BY id"; // ★変更
        try (Connection connection = connect(); // ★変更
             PreparedStatement statement = connection.prepareStatement(sql); // ★変更
             ResultSet results = statement.executeQuery()) { // ★変更：SELECTを実行します
            while (results.next()) { // ★変更：結果を1件ずつ読みます
                Todo todo = new Todo(results.getInt("id"), results.getString("title")); // ★変更
                todo.setDone(results.getInt("done") != 0); // ★変更：0以外なら完了にします
                todos.add(todo); // ★変更：表示用の一覧へ加えます
            }
        }
        return todos; // ★変更：DBから読んだ一覧を返します
    }

    public static void main(String[] args) throws Exception { // プログラムの開始地点を定義します
        createTable(); // ★変更：起動時にtodos表がなければ作ります
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでWebサーバーを用意します
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
                    try { // ★変更：DBへの追加を試します
                        addTodo(title); // ★変更：INSERTでTodoを追加します
                    } catch (SQLException e) { // ★変更：DBエラーをHTTP処理で扱える形にします
                        throw new IOException("Todoの追加に失敗しました", e); // ★変更
                    }
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
                        completeTodo(id); // ★変更：UPDATEで指定したTodoを完了にします
                    } catch (NumberFormatException e) { // ★追加：idが数字でない場合を受け止めます
                        // 何もしません ★追加
                    } catch (SQLException e) { // ★変更：DBエラーをHTTP処理で扱える形にします
                        throw new IOException("Todoの完了に失敗しました", e); // ★変更
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
                        deleteTodo(id); // ★変更：DELETEで指定したTodoを削除します
                    } catch (NumberFormatException e) { // ★追加：idが数字でない場合を受け止めます
                        // 何もしません ★追加
                    } catch (SQLException e) { // ★変更：DBエラーをHTTP処理で扱える形にします
                        throw new IOException("Todoの削除に失敗しました", e); // ★変更
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加：削除後の戻り先を「/」にします
                exchange.sendResponseHeaders(303, -1); // ★追加：「/」へ移動する応答を送ります
                return; // ★追加：この分岐を抜けます
            } else if (path.equals("/")) { // 追加：「/」にフォームとTodo一覧を表示します
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 追加：入力フォームと箇条書きのHTMLを作り始めます
                try { // ★変更：DBから一覧を読む処理を試します
                    for (Todo todo : findAllTodos()) { // ★変更：SELECTしたTodoを1件ずつ取り出す
                        String title = todo.getTitle(); // ★変更：表示するTodoのタイトルを用意する
                        if (todo.isDone()) { // ★変更：終わっているTodoだけ取り消し線を付ける
                            title = "<span style='text-decoration: line-through;'>" + title + "</span>"; // ★変更：タイトルを取り消し線付きにする
                        }
                        html += "<li>" + title + " <a href='/done?id=" + todo.getId()
                                + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // ★追加：id付きの完了・削除リンクを一覧に足す
                    } // 移動：Todoを順番に取り出す処理はここまでです
                } catch (SQLException e) { // ★変更：DBエラーをHTTP処理で扱える形にします
                    throw new IOException("Todo一覧の読み出しに失敗しました", e); // ★変更
                }
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
