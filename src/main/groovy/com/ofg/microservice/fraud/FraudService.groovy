package com.ofg.microservice.fraud

import org.springframework.stereotype.Service

@Service
class FraudService {

    public FraudStatus determineClientFraudStatus(LoanApplication loanApplication) {
        if (loanApplication.job == "OTHER" ||
                loanApplication.amount > 2000 ||
                loanApplication.lastName?.length() < 2) {
            return FraudStatus.FRAUD
        } else if ("FINANCE SECTOR" == loanApplication.job ||
                (loanApplication.amount > 1000 && loanApplication.amount < 2000) ||
                loanApplication.lastName?.length() > 25) {
            return FraudStatus.FISHY
        } else if ("IT" == loanApplication.job ||
                loanApplication.amount < 2000 ||
                loanApplication.lastName?.length() < 2) {
            return FraudStatus.GOOD
        }
    }
}
