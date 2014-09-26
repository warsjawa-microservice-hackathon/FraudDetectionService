package com.ofg.twitter.controller

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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.validation.constraints.NotNull

import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
@RequestMapping('/api/loanApplication')
@Api(value = "pairId", description = "Collects places from tweets and propagates them to Collerators")
class PairIdController {
    @Autowired
    private ServiceRestClient serviceRestClient


    private static final DECISION_MAKER = "decision-make"
    private static final DECISION_MAKER_URL = "/api/loanApplication/"

    @RequestMapping(
            value = '/{loanApplicationId}',
            method = PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Async collecting and propagating of tweets for a given pairId",
            notes = "This will asynchronously call tweet collecting, place extracting and their propagation to Collerators")
    ResponseEntity<Object> getPlacesFromTweets(@PathVariable("loanApplicationId") @NotNull long loanApplicationId,
                                               @RequestBody @NotNull LoanApplication loanApplication) {

        log.info("Loan application request: {}, id: {}", loanApplication.toString(), loanApplicationId)

        // TODO(dst): add logic here
        loanApplication.fraudStatus = determineClientType(loanApplication)
        log.info("client status is {}", loanApplication.fraudStatus)

        serviceRestClient.forService(DECISION_MAKER)
                .post()
                .onUrl(DECISION_MAKER_URL + loanApplicationId)
                .body(JsonOutput.toJson(loanApplication))
                .withHeaders()
                .contentTypeJson()
                .andExecuteFor()
                .anObject()
                .ofType(String)
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