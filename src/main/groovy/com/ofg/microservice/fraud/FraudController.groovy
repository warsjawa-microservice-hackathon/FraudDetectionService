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
@Api(value = "Fraud detection", description = "Detects frouds")
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
    @ApiOperation(value = "Analyzes loan application",
            notes = "Verifies if client is good, fishy or fraud")
    ResponseEntity<Object> analyzeLoanApplication(
            @PathVariable("loanApplicationId") @NotNull long loanApplicationId,
            @RequestBody @NotNull LoanApplication loanApplication) {

        log.info("Analyzing loan application (id={}): {}", loanApplicationId, loanApplication)

        FraudStatus fraudStatus = fraudService.determineClientFraudStatus(loanApplication)
        log.info("Client status is {}", fraudStatus)

        if (fraudStatus == FraudStatus.FISHY) {
            log.info("Client is fishy, reporting to decision maker")
            LoanAppForDecisionService loanAppForDecisionService = new LoanAppForDecisionService(loanApplication, fraudStatus)
            sendFraudStatusToDecisionMaker(loanApplicationId, loanAppForDecisionService)

        }
        return new ResponseEntity<Object>(HttpStatus.OK)
    }

    private void sendFraudStatusToDecisionMaker(
            long loanApplicationId,
            LoanAppForDecisionService loanAppForDecisionService) {

        serviceRestClient.forService(DECISION_MAKER)
                .put()
                .onUrl(DECISION_MAKER_URL_PREFIX + loanApplicationId)
                .body(JsonOutput.toJson(loanAppForDecisionService))
                .withHeaders()
                    .contentTypeJson()
                .andExecuteFor()
                .anObject()
                .ofType(String)
    }
}


