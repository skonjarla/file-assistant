package net.konjarla.aifilemonitor.transport;

import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.util.ThinkTagUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class RestClientInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.debug(request.getURI().toString());
        log.debug(Arrays.toString(new String[]{new String(body, StandardCharsets.UTF_8)}));
        ClientHttpResponse response = execution.execute(request, body);
        // Copy the response body to a byte array for logging
        byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());

        log.debug("Response Before Think: " + new String(responseBody));
        String responseBodyString = new String(responseBody);
        responseBodyString = ThinkTagUtil.removeThinkTags(responseBodyString);
        responseBody = responseBodyString.getBytes(StandardCharsets.UTF_8);
        // Print the response
        log.debug("Response After Think: " + new String(responseBody));

        // Create a new response with the copied body
        return new BufferedClientHttpResponseWrapper(response, responseBody);
        // return execution.execute(request, body);
    }
}
