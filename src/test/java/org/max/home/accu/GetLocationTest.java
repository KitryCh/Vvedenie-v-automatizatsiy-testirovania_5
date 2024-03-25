package org.max.home.accu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.max.home.accu.location.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetLocationTest extends AbstractTest {

    private static final Logger logger = LoggerFactory.getLogger(GetLocationTest.class);

    @Test
    void get_shouldReturn200WithValidLocation() throws IOException, URISyntaxException {
        logger.info("Тест код ответ 200 с валидным местоположением запущен");

        // Создание тела успешного ответа
        ObjectMapper mapper = new ObjectMapper();
        Location location = new Location();
        location.setKey("306729");
        location.setLocalizedName("Granada");

        // Формирование мока для успешного запроса
        stubFor(get(urlPathEqualTo("/locations/v1/cities/autocomplete"))
                .withQueryParam("q", equalTo("Granada"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(mapper.writeValueAsString(new Location[]{location}))));

        // Отправка запроса на сервер WireMock
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(getBaseUrl() + "/locations/v1/cities/autocomplete?q=Granada");
        HttpResponse response = httpClient.execute(request);

        // Проверка статуса ответа и тела ответа
        assertEquals(200, response.getStatusLine().getStatusCode());
        Location[] responseBody = mapper.readValue(response.getEntity().getContent(), Location[].class);
        assertEquals("Granada", responseBody[0].getLocalizedName());
    }

    @Test
    void get_shouldReturn400WithErrorMessage() throws IOException, URISyntaxException {
        logger.info("Тест код ответ 400 с сообщением об ошибке запущен");

        // Создание тела ответа с ошибкой 400
        ObjectMapper mapper = new ObjectMapper();
        String errorMessage = "Invalid input";
        String responseBody = "{\"message\": \"" + errorMessage + "\"}";

        // Формирование мока для запроса, который приводит к ошибке 400
        stubFor(get(urlPathEqualTo("/locations/v1/cities/autocomplete"))
                .withQueryParam("q", equalTo("InvalidCity"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody(responseBody)));

        // Отправка запроса на сервер WireMock
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(getBaseUrl() + "/locations/v1/cities/autocomplete?q=InvalidCity");
        HttpResponse response = httpClient.execute(request);

        // Проверка статуса ответа и тела ответа
        assertEquals(400, response.getStatusLine().getStatusCode());
        String responseMessage = convertResponseToString(response);
        assertTrue(responseMessage.contains(errorMessage));
    }
}
