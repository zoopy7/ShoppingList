package deleon.cj.shoppinglist;

public class ShoppingItem {
    public int id;
    public String item;

    public ShoppingItem() {
    }

    public ShoppingItem(int id, String item) {
        this.id = id;
        this.item = item;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
}
