package io.javaoperatorsdk.webhook.admission.sample.quarkus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class AdmissionEndpointTest {

  public static final String MUTATION_RESPONSE =
      "{\"apiVersion\":\"admission.k8s.io/v1\",\"kind\":\"AdmissionReview\",\"response\":{\"allowed\":true,\"patch\":\"W3sib3AiOiJhZGQiLCJwYXRoIjoiL21ldGFkYXRhL2xhYmVscy9hcHAua3ViZXJuZXRlcy5pb34xbmFtZSIsInZhbHVlIjoibXV0YXRpb24tdGVzdCJ9XQ==\",\"patchType\":\"JSONPatch\",\"uid\":\"0df28fbd-5f5f-11e8-bc74-36e6bb280816\"}}";
  public static final String VALIDATE_RESPONSE =
      "{\"apiVersion\":\"admission.k8s.io/v1\",\"kind\":\"AdmissionReview\",\"response\":{\"allowed\":false,\"status\":{\"apiVersion\":\"v1\",\"kind\":\"Status\",\"code\":403,\"message\":\"Missing label: app.kubernetes.io/name\"},\"uid\":\"0df28fbd-5f5f-11e8-bc74-36e6bb280816\"}}";

  @Test
  void mutates() {
    testMutate(AdmissionEndpoint.MUTATE_PATH);
  }

  @Test
  void validates() {
    testValidate(AdmissionEndpoint.VALIDATE_PATH);
  }

  @Test
  void errorMutates() {
    testServerErrorOnPath(AdmissionEndpoint.ERROR_MUTATE_PATH);
  }

  @Test
  void errorValidates() {
    testServerErrorOnPath(AdmissionEndpoint.ERROR_VALIDATE_PATH);
  }

  @Test
  void asyncMutates() {
    testMutate(AdmissionEndpoint.ASYNC_MUTATE_PATH);
  }

  @Test
  void asyncValidates() {
    testValidate(AdmissionEndpoint.ASYNC_VALIDATE_PATH);
  }

  @Test
  void errorAsyncValidation() {
    testServerErrorOnPath(AdmissionEndpoint.ERROR_ASYNC_VALIDATE_PATH);
  }

  @Test
  void errorAsyncMutation() {
    testServerErrorOnPath(AdmissionEndpoint.ERROR_ASYNC_MUTATE_PATH);
  }

  private void testServerErrorOnPath(String path) {
    given().contentType(ContentType.JSON)
        .body(jsonRequest())
        .when().post("/" + path)
        .then()
        .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
  }

  public void testMutate(String path) {
    given().contentType(ContentType.JSON)
        .body(jsonRequest())
        .when().post("/" + path)
        .then()
        .statusCode(200)
        .body(is(MUTATION_RESPONSE));
  }

  public void testValidate(String path) {
    given().contentType(ContentType.JSON)
        .body(jsonRequest())
        .when().post("/" + path)
        .then()
        .statusCode(200)
        .body(is(
            VALIDATE_RESPONSE));
  }

  private String jsonRequest() {
    try (InputStream is = this.getClass().getResourceAsStream("/admission-request.json")) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
