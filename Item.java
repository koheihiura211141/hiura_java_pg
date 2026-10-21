public class Item { // Itemという名前のクラス（処理をまとめる入れ物）を作ります。
    public static void main(String[] args) { // プログラムを実行したとき、最初に動く部分です。
        String title = "かつ丼を買う"; // String型（文字を入れる種類）の変数titleに文字を入れます。
        String html = "<li> + title + "</li>"; // +で文字をつなぎ、HTMLの1行を作ります。
        System.out.println(html); // 作った文字列をターミナル（文字で結果を見る画面）に1行表示します。
        boolean done = false;
        System.out.println(done);
        int count = 3;
        System.out.println("今" + count + "件");
    } // 最初に動く部分はここまでです。
} // Itemクラスはここまでです。
