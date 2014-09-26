package com.ofg.microservice.fraud

import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.constraints.NotNull

import static com.ofg.microservice.Collaborators.DECISION_MAKER

@Slf4j
@RestController
@RequestMapping('/api/loanApplication')
@Api(value = "pairId", description = "Collects places from tweets and propagates them to Collerators")
class FraudController {

    private static final DECISION_MAKER_URL_PREFIX = "/api/loanApplication/"

    @Autowired
    private ServiceRestClient serviceRestClient

    @Autowired
    private FraudService fraudService

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

        log.info("Analyzing loan application (id={}): {}", loanApplicationId, loanApplication)

        loanApplication.fraudStatus = fraudService.determineClientType(loanApplication)
        log.info("Client status is {}", loanApplication.fraudStatus)

        if (loanApplication.fraudStatus == ClientType.FISHY) {
            log.info("Client is fishy, reporting to decision maker")
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

}


