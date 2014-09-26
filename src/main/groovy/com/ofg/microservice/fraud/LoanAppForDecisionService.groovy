package com.ofg.microservice.fraud

class LoanAppForDecisionService {
    String firstName
    String lastName
    String job
    BigDecimal amount
    FraudStatus fraudStatus

    LoanAppForDecisionService(LoanApplication loanApplication,
                              FraudStatus fraudStatus) {
        this.fraudStatus = fraudStatus
        this.firstName = loanApplication.firstName
        this.lastName = loanApplication.lastName
        this.job = loanApplication.job
        this.amount = loanApplication.amount
    }
}
