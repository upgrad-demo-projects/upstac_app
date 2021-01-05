package org.upgrad.upstac.testrequests.consultation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

  Logger log = LoggerFactory.getLogger(ConsultationController.class);
  @Autowired TestRequestFlowService testRequestFlowService;
  @Autowired private TestRequestUpdateService testRequestUpdateService;
  @Autowired private TestRequestQueryService testRequestQueryService;
  @Autowired private UserLoggedInService userLoggedInService;

  @GetMapping("/in-queue")
  @PreAuthorize("hasAnyRole('DOCTOR')")
  public List<TestRequest> getForConsultations() {
    return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('DOCTOR')")
  public List<TestRequest> getForDoctor() {
    return testRequestQueryService.findByDoctor(userLoggedInService.getLoggedInUser());
  }

  @PreAuthorize("hasAnyRole('DOCTOR')")
  @PutMapping("/assign/{id}")
  public TestRequest assignForConsultation(@PathVariable Long id) {
    try {
      return testRequestUpdateService.assignForConsultation(
          id, userLoggedInService.getLoggedInUser());
    } catch (AppException e) {
      throw asBadRequest(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyRole('DOCTOR')")
  @PutMapping("/update/{id}")
  public TestRequest updateConsultation(
      @PathVariable Long id, @RequestBody CreateConsultationRequest testResult) {
    try {
      return testRequestUpdateService.updateConsultation(
          id, testResult, userLoggedInService.getLoggedInUser());
    } catch (ConstraintViolationException e) {
      throw asConstraintViolation(e);
    } catch (AppException e) {
      throw asBadRequest(e.getMessage());
    }
  }
}
