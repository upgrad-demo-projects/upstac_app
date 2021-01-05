package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.models.Gender;

import java.util.UUID;

@SpringBootTest
@Slf4j
@ExtendWith(MockitoExtension.class)
class LabRequestControllerTest {


    @Autowired
    LabRequestController labRequestController;

    @Autowired
    TestRequestQueryService testRequestQueryService;

    @Autowired
    TestRequestController testRequestController;


    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status() {
        CreateTestRequest createTestRequest = new CreateTestRequest();
        createTestRequest.setName("Test_Name");
        createTestRequest.setGender(Gender.MALE);
        createTestRequest.setPhoneNumber(UUID.randomUUID().toString());
        createTestRequest.setEmail(UUID.randomUUID().toString());
        testRequestController.createRequest(createTestRequest);
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);
        TestRequest testRequest2 = new TestRequest();
        testRequest2.requestId = testRequest.requestId;
        TestRequest response =  labRequestController.assignForLabTest(testRequest2.requestId);
        Assertions.assertEquals(response.requestId, testRequest2.requestId);
        Assertions.assertEquals(testRequest2.getStatus(), RequestStatus.INITIATED);
        Assertions.assertNotNull(response.getLabResult());
    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception() {
        Long InvalidRequestId = -34L;
        Assertions.assertThrows(ResponseStatusException.class, () -> labRequestController.assignForLabTest(InvalidRequestId));
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details() {
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        TestRequest tr1 = new TestRequest();
        tr1.requestId = testRequest.requestId;
        tr1.setStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(tr1);
        TestRequest response =  labRequestController.updateLabTest(tr1.requestId, labResult);
        Assertions.assertEquals(response.requestId, tr1.getRequestId());
        Assertions.assertEquals(response.labResult.getResult(), TestStatus.NEGATIVE);
        Assertions.assertEquals(response.labResult, response.getLabResult());
    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception() {

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        CreateLabResult createLabResult = getCreateLabResult(testRequest);
        Assertions.assertThrows(ResponseStatusException.class, () -> labRequestController.updateLabTest(-1L, createLabResult), "Invalid ID");
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception() {


        TestRequest testRequest = new TestRequest();
        testRequest.requestId = 2L;

        CreateLabResult createLabResult = getCreateLabResult(testRequest);
        Assertions.assertThrows(ResponseStatusException.class, () -> labRequestController.updateLabTest(testRequest.requestId, createLabResult)) ;

    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

        LabResult result = new LabResult();
        result.setResult(TestStatus.NEGATIVE);
        CreateLabResult labResult = new CreateLabResult();
        labResult.setBloodPressure("test_bloodPressure");
        labResult.setResult(result.getResult());
        labResult.setTemperature("test_temp");
        labResult.setHeartBeat("test_heart");
        return labResult;
    }

}