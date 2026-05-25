package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        ON_LEAVE
    }

    private int id;
    private String fullName;
    private LocalDate birthday;
    private Gender gender;
    private String idCard;
    private String employeeBarcode;
    private String phone;
    private String email;
    private String address;
    private int storeId;
    private BigDecimal hourlyRate;
    private Status status;

    public Employee() {
    }

    public Employee(int id, String fullName, LocalDate birthday, Gender gender,
            String idCard, String employeeBarcode, String phone, String email,
            String address, int storeId, BigDecimal hourlyRate, Status status) {
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

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", fullName='" + fullName + "', idCard='" + idCard + "', status=" + status + "}";
    }

}
