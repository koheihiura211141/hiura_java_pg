import java.util.ArrayList; // ArrayList（データを順番に保存する入れ物）を使えるようにします。
import java.util.List; // List（複数のデータを順番に扱う仕組み）を使えるようにします。

class MainTodo { // MainTodo（Main.javaで使うTodoの設計図）を定義します。
    String title; // やることのタイトルを保存します。
    boolean done; // 済んだかどうかを true（済）または false（未済）で保存します。

    MainTodo(String title, boolean done) { // MainTodoを作るときにタイトルと状態を受け取ります。
        this.title = title; // 受け取ったタイトルを、このTodoのtitleに保存します。
        this.done = done; // 受け取った状態を、このTodoのdoneに保存します。
    } // Todoを作る処理はここまでです。

    String toItem() { // TodoをHTMLのli形式（一覧の1行を表す形）に変えます。
        if (done) { // doneがtrue（済）かどうかを調べます。
            return "<li>[OK] " + title + "</li>"; // 済の場合はタイトルの前に[済]を付けて返します。
        } // 済の場合の処理はここまでです。
        return "<li>" + title + "</li>"; // 未済の場合はタイトルだけをli形式にして返します。
    } // toItemメソッド（ひとまとまりの処理）はここまでです。
} // Todoクラスはここまでです。

public class Main { // プログラム全体を表すMainクラスを定義します。
    public static void main(String[] args) { // プログラムを実行したときに最初に動く場所です。
        List<MainTodo> todos = new ArrayList<>(); // MainTodoを複数保存する空のリストを作ります。
        todos.add(new MainTodo("牛乳を買う", false)); // 未済の「牛乳を買う」をリストに追加します。
        todos.add(new MainTodo("ゴミを出す", true)); // 済の「ゴミを出す」をリストに追加します。
        todos.add(new MainTodo("洗濯物干す", true));

        for (MainTodo todo : todos) { // リストのMainTodoを先頭から1件ずつ取り出します。
            System.out.println(todo.toItem()); // Todoをli形式に変えてターミナルへ1行出力します。
        } // 繰り返し処理はここまでです。
    } // mainメソッドはここまでです。
} // Mainクラスはここまでです。
