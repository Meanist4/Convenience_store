package entity;

public class Store {

    public enum Status {
        ACTIVE,
        INACTIVE,
        TEMPORARILY_CLOSED
    }

    private int id;
    private String storeName;
    private String location;
    private int managerId;
    private Status status;

    public Store() {
    }

    public Store(int id, String storeName, String location, int managerId, Status status) {
        this.id = id;
        this.storeName = storeName;
        this.location = location;
        this.managerId = managerId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Store{id=" + id + ", storeName='" + storeName + "', location='" + location + "', status=" + status + "}";
    }
}
