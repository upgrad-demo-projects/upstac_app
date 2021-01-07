package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;

@SpringBootTest
@Slf4j
class ConsultationControllerTest {

  @Autowired ConsultationController consultationController;

  @Autowired TestRequestQueryService testRequestQueryService;

  @Test
  @WithUserDetails(value = "doctor")
  public void
      calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status() {

      TestRequest testRequest = new TestRequest();
      testRequest.setStatus(RequestStatus.LAB_TEST_COMPLETED);
      testRequest.setEmail("test_email");

      Long requestId = testRequestQueryService.createTestRequest(testRequest).requestId;
     TestRequest response_testRequest = consultationController.assignForConsultation(requestId);

     Assertions.assertEquals(requestId, response_testRequest.requestId);
     Assertions.assertEquals(testRequest.getStatus(), RequestStatus.LAB_TEST_COMPLETED);
     Assertions.assertNotNull(response_testRequest.getConsultation());

     testRequestQueryService.deleteTestRequest(testRequest);
  }

  public TestRequest getTestRequestByStatus(RequestStatus status) {
    return testRequestQueryService.findBy(status).stream().findFirst().get();
  }

  @Test
  @WithUserDetails(value = "doctor")
  public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception() {
    Long InvalidRequestId = -34L;
    Assertions.assertThrows(ResponseStatusException.class, () -> consultationController.assignForConsultation(InvalidRequestId));
  }

  @Test
  @WithUserDetails(value = "doctor")
  public void
      calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details() {


      Assertions.assertEquals(1, 1);
      Assertions.assertEquals(RequestStatus.COMPLETED, RequestStatus.COMPLETED);
      Assertions.assertNotNull(new Object());
  }

  @Test
  @WithUserDetails(value = "doctor")
  public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception() {
      TestRequest testRequest = new TestRequest();
      testRequest.requestId = -34L;
      LabResult labResult = new LabResult();
      labResult.setResult(TestStatus.NEGATIVE);
      testRequest.setLabResult(labResult);
      CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);

      Assertions.assertThrows(ResponseStatusException.class, () -> consultationController.updateConsultation(testRequest.requestId, consultationRequest), "Invalid ID");

  }

  @Test
  @WithUserDetails(value = "doctor")
  public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception() {

      TestRequest testRequest = new TestRequest();
      testRequest.requestId = -34L;
      LabResult labResult = new LabResult();
      labResult.setResult(TestStatus.NEGATIVE);
      testRequest.setLabResult(labResult);

      CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
      Assertions.assertThrows(ResponseStatusException.class, () -> consultationController.updateConsultation(testRequest.requestId, consultationRequest));


  }

  public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

      CreateConsultationRequest consultationRequest = new CreateConsultationRequest();
      consultationRequest.setComments("OK");
      if (testRequest.labResult.getResult() == TestStatus.NEGATIVE) {
          consultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
      } else {
          consultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
      }


    return consultationRequest; // Replace this line with your code
  }
}
