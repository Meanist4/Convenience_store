package dto;

public enum SystemFilter {
    ALL("Tất cả dữ liệu", false, "ALL"),
    ACTIVE("Đang hoạt động", false, "active"),
    INACTIVE("Ngừng hoạt động", false, "inactive"),
    TRASH("Đã xóa (Thùng rác)", true, "ALL");

    private final String label;
    private final boolean isDeleted;
    private final String status;

    SystemFilter(String label, boolean isDeleted, String status) {
        this.label = label;
        this.isDeleted = isDeleted;
        this.status = status;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return label;
    }
}
