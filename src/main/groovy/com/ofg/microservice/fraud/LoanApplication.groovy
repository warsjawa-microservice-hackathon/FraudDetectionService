package com.ofg.microservice.fraud

class LoanApplication {
    String firstName
    String lastName
    String job
    BigDecimal amount


    @Override
    public String toString() {
        return "LoanApplication{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", job='" + job + '\'' +
                ", amount=" + amount +
                '}';
    }
}
