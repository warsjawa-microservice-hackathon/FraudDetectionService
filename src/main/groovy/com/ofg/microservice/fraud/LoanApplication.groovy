package com.ofg.microservice.fraud

import groovy.transform.ToString

/**
 * Created by dst on 26.09.14.
 */
@ToString
class LoanApplication {
    String firstName
    String lastName
    String job
    BigDecimal amount
    ClientType fraudStatus
}
