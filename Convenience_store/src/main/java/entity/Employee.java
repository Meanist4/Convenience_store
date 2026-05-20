package entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Employee {
    private int id;
    private String fullName;
    private Date birthday;
    private String gender; // 'Nam' | 'Nu' | 'Khac'
    private String idCard;
    private String employeeBarcode;
    private String phone;
    private String email;
    private String address;
    private Integer storeId; // nullable
    private BigDecimal hourlyRate;
    private String status; // 'active' | 'inactive' | 'on_leave'
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isDeleted;
    private Timestamp deletedAt; // nullable

    public Employee() {
    }

    public Employee(int id, String fullName, Date birthday, String gender,
            String idCard, String employeeBarcode, String phone, String email,
            String address, Integer storeId, BigDecimal hourlyRate, String status,
            Timestamp createdAt, Timestamp updatedAt, boolean isDeleted, Timestamp deletedAt) {
        this.id = id;
        this.fullName = fullName;
        this.birthday = birthday;
        this.gender = gender;
        this.idCard = idCard;
        this.employeeBarcode = employeeBarcode;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.storeId = storeId;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getEmployeeBarcode() {
        return employeeBarcode;
    }

    public void setEmployeeBarcode(String employeeBarcode) {
        this.employeeBarcode = employeeBarcode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", birthday=" + birthday +
                ", gender='" + gender + '\'' +
                ", idCard='" + idCard + '\'' +
                ", employeeBarcode='" + employeeBarcode + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", storeId=" + storeId +
                ", hourlyRate=" + hourlyRate +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
