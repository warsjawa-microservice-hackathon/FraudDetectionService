package com.ofg.microservice.fraud

import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import groovy.json.JsonOutput
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.constraints.NotNull

import static com.ofg.microservice.Collaborators.DECISION_MAKER

/**
 * Created by mihn on 26.09.14.
 */
@Slf4j
@RestController
@RequestMapping('/api/loanApplication')
@Api(value = "pairId", description = "Collects places from tweets and propagates them to Collerators")
class FraudController {

    private static final DECISION_MAKER_URL_PREFIX = "/api/loanApplication/"

    @Autowired
    private ServiceRestClient serviceRestClient

    @RequestMapping(
            value = '/{loanApplicationId}',
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Async collecting and propagating of tweets for a given pairId",
            notes = "This will asynchronously call tweet collecting, place extracting and their propagation to Collerators")
    ResponseEntity<Object> analyzeLoanApplication(
            @PathVariable("loanApplicationId") @NotNull long loanApplicationId,
            @RequestBody @NotNull LoanApplication loanApplication) {

        log.info("Loan application request: {}, id: {}", loanApplication.toString(), loanApplicationId)

        loanApplication.fraudStatus = determineClientType(loanApplication)
        log.info("client status is {}", loanApplication.fraudStatus)

        if (loanApplication.fraudStatus == ClientType.FISHY) {
            log.info("client is fishy, reporting to decisionmaker")
            serviceRestClient.forService(DECISION_MAKER)
                    .put()
                    .onUrl(DECISION_MAKER_URL_PREFIX + loanApplicationId)
                    .body(JsonOutput.toJson(loanApplication))
                    .withHeaders()
                    .contentTypeJson()
                    .andExecuteFor()
                    .anObject()
                    .ofType(String)
        }
        return new ResponseEntity<Object>(HttpStatus.OK)
    }

    private ClientType determineClientType(LoanApplication loanApplication) {
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

@ToString
class LoanApplication {
    String firstName
    String lastName
    String job
    BigDecimal amount
    ClientType fraudStatus
}

enum ClientType {
    FRAUD,
    FISHY,
    GOOD;
}