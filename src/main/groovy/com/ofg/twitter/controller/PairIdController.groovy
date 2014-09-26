package com.ofg.twitter.controller

import com.ofg.twitter.controller.place.extractor.PropagationWorker
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.validation.constraints.NotNull
import java.util.concurrent.Callable

import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Slf4j
@RestController
@RequestMapping('/api/loanApplication')
@Api(value = "pairId", description = "Collects places from tweets and propagates them to Collerators")
class PairIdController {

    @Autowired private PropagationWorker propagationWorker


    @RequestMapping(
            value = '/{loanApplicationId}',
            method = PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Async collecting and propagating of tweets for a given pairId",
            notes = "This will asynchronously call tweet collecting, place extracting and their propagation to Collerators")
    Callable<Void> getPlacesFromTweets(@PathVariable("loanApplicationId") @NotNull long loanApplicationId,
                                       @RequestBody @NotNull LoanApplication loanApplication) {

//        propagationWorker.collectAndPropagate(pairId, tweets)
        log.info("Loan application request: {}, id: {}", loanApplication.toString(), loanApplicationId)
    }
}
@ToString
class LoanApplication {
    String firstName;
    String lastName;
    String job;
    BigDecimal amount;
}

