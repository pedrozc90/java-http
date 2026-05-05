# java-http

A simple java http client abstraction to wrapper other clients

## Requirements

- Java 8

## Usage

```java
import com.pedrozc90.http.exceptions.HttpResponseException;

public class ExternalClient {

    private final HttpClient client = new NativesHttpClient();

    public Result<Payload> integrate(final String url, final String token, final String value) {
        final Payload payload = new Payload(value);
        
        final Request<Payload> req = Request.builder()
            .url(url)
            .bearer(token)
            .contentType("application/json")
            .body(payload)
            .build();
        
        final Response res = client.execute(request);
        
        return new Result<>(req, res);
    }
    
    public static void main(final String[] args) {
        final String url = "http://localhost:9000/path/to/resource";
        
        try {
            // build and send request. if reponse is 400, throws HttpResponseException
            final Result<Payload> result = integrat(url, "token", "none");
            
            // handle response
            final Response response = result.getResponse();
            
            // get response body as bytes
            final byte[] bytes = response.getBody();
            
            // get response body as string
            final String payload = response.asString();
            
            // get response body as object
            final File file = response.asFile();
            
            // deserialize response json body as object
            final MyObject object = response.as(MyObject.class);
        } catch (HttpResponseException e) {
            final Request<?> req = e.getRequest();
            final Response res = e.getResponse();
            throw new RuntimeException("Integrate request failed with status " + res.getStatus(), e);
        }
    }
    
    @Data
    private static class Payload {
        private final String value;
    }

}
```

## License

See [LICENSE](./LICENSE) file.
