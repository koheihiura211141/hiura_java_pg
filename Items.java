public class Items {
    public static void main(String[] args) { // プログラムを実行したときに最初に動く部分です
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする" }; // 3件の文字列をtodosという配列（複数の値を入れる箱）に入れます
        boolean[] done = { true, false, false, false };
        for (int i = 0; i < todos.length; i++) { // iを0から始め、todosの件数より小さい間、1ずつ増やします
            if (!todos[i].isEmpty()) { // 空ではないなら
                String mark = done[i] ? "[済] " : "";
                System.out.println("<li>" + mark + todos[i] + "</li>"); // i番目のTodoを<li>と</li>で囲み、1行出力します
            }
        } // for文（繰り返し処理）の終わりです
    } // main部分の終わりです
} // Itemsクラスの終わりです
