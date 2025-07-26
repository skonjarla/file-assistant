package net.konjarla.aifilemonitor.transport;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse originalResponse;
    private final byte[] responseBody;

    public BufferedClientHttpResponseWrapper(ClientHttpResponse originalResponse, byte[] responseBody) {
        this.originalResponse = originalResponse;
        this.responseBody = responseBody;
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return originalResponse.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return originalResponse.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return originalResponse.getStatusText();
    }

    @Override
    public void close() {
        originalResponse.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(responseBody);
    }

    @Override
    public HttpHeaders getHeaders() {
        return originalResponse.getHeaders();
    }
}
