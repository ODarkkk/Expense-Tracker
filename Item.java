import java.util.Objects;

public class Item {
    private Integer id;
    private String date;
    private String description;
    private Integer amount;

    public Item(Integer id, String date, String description, Integer amount) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
    }

    public Integer GetId() {
        return id;
    }

    public String Date() {
        return date;
    }

    public String GetDescription() {
        return description;
    }

    public Integer GetAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Item))
            return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id) &&
                Objects.equals(date, item.date) &&
                Objects.equals(description, item.description) &&
                Objects.equals(amount, item.amount);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                '}';
    }
}
