package com.ofg.microservice.fraud

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import java.security.Provider

/**
 * Created by dst on 26.09.14.
 */
@Service
class FraudService {

    public ClientType determineClientType(LoanApplication loanApplication) {
        if (loanApplication.job == "OTHER" ||
                loanApplication.amount > 2000 ||
                loanApplication.lastName?.length() < 2) {
            return ClientType.FRAUD
        } else if ("FINANCE SECTOR" == loanApplication.job ||
                (loanApplication.amount > 1000 && loanApplication.amount < 2000) ||
                loanApplication.lastName?.length() > 25) {
            return ClientType.FISHY
        } else if ("IT" == loanApplication.job ||
                loanApplication.amount < 2000 ||
                loanApplication.lastName?.length() < 2) {
            return ClientType.GOOD
        }
    }
}